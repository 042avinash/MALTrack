package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.model.MangaRecommendation
import com.example.myapplication.data.model.MyMangaListStatus
import com.example.myapplication.data.model.Statistics
import com.example.myapplication.data.model.StatusStatistics
import com.example.myapplication.data.remote.JikanAnimeScoreBucket
import com.example.myapplication.data.remote.JikanCharacterData
import com.example.myapplication.data.remote.JikanReviewData
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MangaDetailsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val CACHE_TTL_MS = 10 * 60 * 1000L
        private const val MAX_CARD_META_FETCH = 30
        private val detailsCache = mutableMapOf<Int, Pair<Long, MangaDetailsUiState.Success>>()
        private val mangaCardMetaCache = mutableMapOf<Int, Pair<Long, MangaCardMeta>>()
    }

    private val mangaId: Int = checkNotNull(savedStateHandle["mangaId"])
    private var cardMetaJob: Job? = null
    private var delayedDetailsJob: Job? = null
    private var reviewsJob: Job? = null
    private var recommendationsJob: Job? = null
    private var charactersJob: Job? = null
    private var scoreDistributionJob: Job? = null

    private val _uiState = MutableStateFlow<MangaDetailsUiState>(MangaDetailsUiState.Loading)
    val uiState: StateFlow<MangaDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now = SystemClock.elapsedRealtime()
                if (!forceRefresh) {
                    val cached = detailsCache[mangaId]
                    if (cached != null && now - cached.first < CACHE_TTL_MS) {
                        _uiState.value = cached.second
                        if (cached.second.cardMeta.isEmpty()) {
                            launchCardMetaRefresh(cached.second.details)
                        }
                        return@launch
                    }
                }

                val currentState = _uiState.value
                if (currentState !is MangaDetailsUiState.Success) {
                    _uiState.value = MangaDetailsUiState.Loading
                }

                val staleCached = detailsCache[mangaId]?.second
                val quickDetails = withTimeoutOrNull(1_500L) { repository.getMangaDetailsLite(mangaId) }
                if (quickDetails != null) {
                    publishBaseDetails(quickDetails)
                    return@launch
                }

                // Soft-timeout fallback: keep UI usable and continue in background.
                if (staleCached != null) {
                    _uiState.value = staleCached
                    if (staleCached.cardMeta.isEmpty()) {
                        launchCardMetaRefresh(staleCached.details)
                    }
                    delayedDetailsJob?.cancel()
                    delayedDetailsJob = viewModelScope.launch {
                        val delayedDetails = runCatching { repository.getMangaDetailsLite(mangaId) }.getOrNull() ?: return@launch
                        publishBaseDetails(delayedDetails)
                    }
                    return@launch
                }

                val delayedDetails = repository.getMangaDetailsLite(mangaId)
                publishBaseDetails(delayedDetails)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = MangaDetailsUiState.Error(e.message ?: "Failed to load details")
            }
        }
    }

    private fun publishBaseDetails(details: MangaDetailsResponse) {
        val previous = _uiState.value as? MangaDetailsUiState.Success
        val baseDetails = details.copy(recommendations = previous?.details?.recommendations ?: emptyList())
        val successState = MangaDetailsUiState.Success(
            details = baseDetails,
            recommendations = previous?.recommendations ?: emptyList(),
            reviews = previous?.reviews ?: emptyList(),
            allReviewsCount = previous?.allReviewsCount ?: 0,
            isRecommendationsLoaded = previous?.isRecommendationsLoaded ?: false,
            isRecommendationsLoading = false,
            isReviewsLoaded = previous?.isReviewsLoaded ?: false,
            isReviewsLoading = false,
            recommendationsError = null,
            reviewsError = null,
            characters = previous?.characters ?: emptyList(),
            isCharactersLoaded = previous?.isCharactersLoaded ?: false,
            isCharactersLoading = false,
            charactersError = null,
            isCommunityStatsRefreshing = false,
            communityStatsError = null
        )
        detailsCache[mangaId] = SystemClock.elapsedRealtime() to successState
        _uiState.value = successState
        launchCardMetaRefresh(baseDetails)
        refreshScoreDistribution(userTriggered = false)
    }

    fun loadReviews(forceRefresh: Boolean = false) {
        val current = _uiState.value as? MangaDetailsUiState.Success ?: return
        if (current.isReviewsLoaded && !forceRefresh) return

        reviewsJob?.cancel()
        reviewsJob = viewModelScope.launch {
            val before = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isReviewsLoading = true, reviewsError = null)

            val reviews = fetchWithRetry { repository.getMangaReviews(mangaId).data }
            val recommended = reviews.find { it.tags.any { tag -> tag.contains("Recommended", ignoreCase = true) && !tag.contains("Not", ignoreCase = true) } }
            val mixed = reviews.find { it.tags.any { tag -> tag.contains("Mixed", ignoreCase = true) } }
            val notRecommended = reviews.find { it.tags.any { tag -> tag.contains("Not Recommended", ignoreCase = true) } }
            val topReviews = listOfNotNull(recommended, mixed, notRecommended)
            val finalReviews = (topReviews + reviews).distinctBy { it.mal_id }.take(3)

            val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            if (latest.details.id != mangaId) return@launch

            val updated = latest.copy(
                reviews = finalReviews,
                allReviewsCount = reviews.size,
                isReviewsLoaded = true,
                isReviewsLoading = false,
                reviewsError = null
            )
            detailsCache[mangaId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }.also { job ->
            job.invokeOnCompletion { error ->
                if (error == null) return@invokeOnCompletion
                val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@invokeOnCompletion
                _uiState.value = latest.copy(
                    isReviewsLoading = false,
                    reviewsError = "Could not load reviews right now. Pull to refresh or tap retry."
                )
            }
        }
    }

    fun loadRecommendations(forceRefresh: Boolean = false) {
        val current = _uiState.value as? MangaDetailsUiState.Success ?: return
        if (current.isRecommendationsLoaded && !forceRefresh) return

        recommendationsJob?.cancel()
        recommendationsJob = viewModelScope.launch {
            val before = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isRecommendationsLoading = true, recommendationsError = null)

            val recommendations = fetchWithRetry {
                repository.getMangaRecommendationsOnly(mangaId).recommendations.orEmpty()
            }

            val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            if (latest.details.id != mangaId) return@launch

            val updated = latest.copy(
                recommendations = recommendations,
                isRecommendationsLoaded = true,
                isRecommendationsLoading = false,
                recommendationsError = null
            )
            detailsCache[mangaId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
            launchCardMetaRefresh(latest.details.copy(recommendations = recommendations))
        }.also { job ->
            job.invokeOnCompletion { error ->
                if (error == null) return@invokeOnCompletion
                val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@invokeOnCompletion
                _uiState.value = latest.copy(
                    isRecommendationsLoading = false,
                    recommendationsError = "Could not load recommendations right now. Pull to refresh or tap retry."
                )
            }
        }
    }

    fun loadCharacters(forceRefresh: Boolean = false) {
        val current = _uiState.value as? MangaDetailsUiState.Success ?: return
        if (current.isCharactersLoaded && !forceRefresh) return

        charactersJob?.cancel()
        charactersJob = viewModelScope.launch {
            val before = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isCharactersLoading = true, charactersError = null)

            val characters = fetchWithRetry {
                repository.getMangaCharacters(mangaId).data
            }

            val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            if (latest.details.id != mangaId) return@launch
            val updated = latest.copy(
                characters = characters,
                isCharactersLoaded = true,
                isCharactersLoading = false,
                charactersError = null
            )
            detailsCache[mangaId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }.also { job ->
            job.invokeOnCompletion { error ->
                if (error == null) return@invokeOnCompletion
                val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@invokeOnCompletion
                _uiState.value = latest.copy(
                    isCharactersLoading = false,
                    charactersError = "Could not load characters right now. Pull to refresh or tap retry."
                )
            }
        }
    }

    fun refreshCommunityStats() {
        val current = _uiState.value as? MangaDetailsUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isCommunityStatsRefreshing = true, communityStatsError = null)
            val refreshed = runCatching { repository.getMangaDetailsLite(mangaId) }.getOrNull()
            val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            if (refreshed == null) {
                _uiState.value = latest.copy(
                    isCommunityStatsRefreshing = false,
                    communityStatsError = "Could not refresh community stats right now. Please try again."
                )
                return@launch
            }
            val mergedDetails = latest.details.copy(
                statistics = refreshed.statistics,
                numListUsers = refreshed.numListUsers,
                numScoringUsers = refreshed.numScoringUsers,
                mean = refreshed.mean
            )
            val updated = latest.copy(
                details = mergedDetails,
                isCommunityStatsRefreshing = false,
                communityStatsError = null
            )
            detailsCache[mangaId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
            refreshScoreDistribution(userTriggered = true)
        }
    }

    fun refreshReviews() = loadReviews(forceRefresh = true)
    fun refreshRecommendations() = loadRecommendations(forceRefresh = true)
    fun refreshCharactersSection() = loadCharacters(forceRefresh = true)
    fun refreshScoreDistribution(userTriggered: Boolean = true) {
        scoreDistributionJob?.cancel()
        scoreDistributionJob = viewModelScope.launch {
            val current = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            _uiState.value = current.copy(isScoreDistributionLoading = true, scoreDistributionError = null)
            val statsPayload = runCatching {
                if (userTriggered) fetchWithRetry { repository.getMangaStatistics(mangaId).data }
                else repository.getMangaStatistics(mangaId).data
            }.getOrNull()

            val latest = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            val buckets = statsPayload?.scores.orEmpty()
            val hasGoodData = !buckets.isNullOrEmpty() && buckets.any { it.votes > 0 || it.percentage > 0.0 }
            val jikanBackedStatus = statsPayload?.let {
                StatusStatistics(
                    watching = it.reading,
                    completed = it.completed,
                    onHold = it.on_hold,
                    dropped = it.dropped,
                    planToWatch = it.plan_to_read
                )
            }
            val mergedDetails = if (latest.details.statistics == null && jikanBackedStatus != null) {
                latest.details.copy(
                    statistics = Statistics(
                        status = jikanBackedStatus,
                        numListUsers = statsPayload.total ?: latest.details.numListUsers
                    )
                )
            } else {
                latest.details
            }
            val updated = latest.copy(
                details = mergedDetails,
                scoreDistribution = if (hasGoodData) buckets else latest.scoreDistribution,
                isScoreDistributionLoading = false,
                scoreDistributionError = if (hasGoodData) null else "Score distribution source is temporarily unavailable.",
                lastScoreDistributionUpdatedAt = if (hasGoodData) SystemClock.elapsedRealtime() else latest.lastScoreDistributionUpdatedAt
            )
            detailsCache[mangaId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    private suspend fun <T> fetchWithRetry(
        attempts: Int = 3,
        initialDelayMs: Long = 350L,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        repeat(attempts - 1) {
            runCatching { return block() }
            kotlinx.coroutines.delay(currentDelay)
            currentDelay *= 2
        }
        return block()
    }

    private suspend fun getMangaCardMeta(
        details: MangaDetailsResponse
    ): Map<Int, MangaCardMeta> = supervisorScope {
        val targetIds = (
            details.relatedManga.orEmpty().map { it.node.id } +
                details.recommendations.orEmpty().map { it.node.id }
            )
            .distinct()
            .take(MAX_CARD_META_FETCH)
        if (targetIds.isEmpty()) return@supervisorScope emptyMap()

        val now = SystemClock.elapsedRealtime()
        val result = mutableMapOf<Int, MangaCardMeta>()
        targetIds.chunked(2).forEach { batch ->
            batch
                .map { mangaId ->
                    async {
                        val cached = mangaCardMetaCache[mangaId]
                        if (cached != null && now - cached.first < CACHE_TTL_MS) {
                            return@async mangaId to cached.second
                        }
                        val fetched = runCatching { repository.getMangaDetailsLite(mangaId) }.getOrNull()
                        val meta = fetched?.let {
                            MangaCardMeta(
                                mean = it.mean,
                                members = it.numListUsers,
                                myListStatus = it.myListStatus
                            )
                        } ?: MangaCardMeta()
                        mangaCardMetaCache[mangaId] = SystemClock.elapsedRealtime() to meta
                        mangaId to meta
                    }
                }
                .forEach { deferred ->
                    val (id, meta) = deferred.await()
                    result[id] = meta
                }
        }
        result
    }

    private fun launchCardMetaRefresh(details: MangaDetailsResponse) {
        cardMetaJob?.cancel()
        cardMetaJob = viewModelScope.launch {
            val meta = runCatching { getMangaCardMeta(details) }.getOrDefault(emptyMap())
            if (meta.isEmpty()) return@launch
            val current = _uiState.value as? MangaDetailsUiState.Success ?: return@launch
            if (current.details.id != details.id) return@launch
            val merged = current.cardMeta + meta
            if (merged == current.cardMeta) return@launch
            val updated = current.copy(cardMeta = merged)
            detailsCache[mangaId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun updateListStatus(
        status: String? = null,
        isRereading: Boolean? = null,
        score: Int? = null,
        numVolumesRead: Int? = null,
        numChaptersRead: Int? = null,
        priority: Int? = null,
        numTimesReread: Int? = null,
        rereadValue: Int? = null,
        tags: String? = null,
        comments: String? = null,
        startDate: String? = null,
        finishDate: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateMyMangaListStatus(
                    mangaId = mangaId,
                    status = status,
                    isRereading = isRereading,
                    score = score,
                    numVolumesRead = numVolumesRead,
                    numChaptersRead = numChaptersRead,
                    priority = priority,
                    numTimesReread = numTimesReread,
                    rereadValue = rereadValue,
                    tags = tags,
                    comments = comments,
                    startDate = startDate,
                    finishDate = finishDate
                )
                loadDetails(forceRefresh = true)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteFromList() {
        viewModelScope.launch {
            try {
                repository.deleteMyMangaListStatus(mangaId)
                loadDetails(forceRefresh = true)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed interface MangaDetailsUiState {
    data object Loading : MangaDetailsUiState
    data class Success(
        val details: MangaDetailsResponse,
        val cardMeta: Map<Int, MangaCardMeta> = emptyMap(),
        val recommendations: List<MangaRecommendation> = emptyList(),
        val reviews: List<JikanReviewData> = emptyList(),
        val characters: List<JikanCharacterData> = emptyList(),
        val scoreDistribution: List<JikanAnimeScoreBucket> = emptyList(),
        val allReviewsCount: Int = 0,
        val isRecommendationsLoaded: Boolean = false,
        val isRecommendationsLoading: Boolean = false,
        val isReviewsLoaded: Boolean = false,
        val isReviewsLoading: Boolean = false,
        val isCharactersLoaded: Boolean = false,
        val isCharactersLoading: Boolean = false,
        val isScoreDistributionLoading: Boolean = false,
        val isCommunityStatsRefreshing: Boolean = false,
        val recommendationsError: String? = null,
        val reviewsError: String? = null,
        val charactersError: String? = null,
        val communityStatsError: String? = null,
        val scoreDistributionError: String? = null,
        val lastScoreDistributionUpdatedAt: Long = 0L
    ) : MangaDetailsUiState
    data class Error(val message: String) : MangaDetailsUiState
}

data class MangaCardMeta(
    val mean: Float? = null,
    val members: Int? = null,
    val myListStatus: MyMangaListStatus? = null
)
