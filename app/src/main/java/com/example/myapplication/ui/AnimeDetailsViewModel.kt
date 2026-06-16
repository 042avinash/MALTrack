package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AlternativeTitles
import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.Recommendation
import com.example.myapplication.data.remote.JikanCharacterData
import com.example.myapplication.data.remote.JikanReviewData
import com.example.myapplication.data.remote.JikanStaffData
import com.example.myapplication.data.remote.JikanAnimeScoreBucket
import com.example.myapplication.data.remote.JikanStreamingData
import com.example.myapplication.data.remote.JikanThemesData
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val CACHE_TTL_MS = 10 * 60 * 1000L
        private const val MAX_CARD_META_FETCH = 30
        private const val SCORE_RETRY_COUNT = 2
        private const val SCORE_RETRY_DELAY_MS = 700L
        private const val HEAVY_RETRY_COUNT = 2
        private const val HEAVY_RETRY_DELAY_MS = 800L
        private val detailsCache = mutableMapOf<Int, Pair<Long, AnimeDetailsUiState.Success>>()
        private val recommendationMetaCache = mutableMapOf<Int, Pair<Long, RecommendationCardMeta>>()
    }

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])
    private var cardMetaJob: Job? = null
    private var supplementaryJob: Job? = null
    private var reviewsJob: Job? = null
    private var recommendationsJob: Job? = null
    private var peopleJob: Job? = null
    private var charactersJob: Job? = null
    private var staffJob: Job? = null
    private var myListStatusJob: Job? = null
    private var communityStatsJob: Job? = null
    private var availabilityJob: Job? = null

    private val _uiState = MutableStateFlow<AnimeDetailsUiState>(AnimeDetailsUiState.Loading)
    val uiState: StateFlow<AnimeDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now = SystemClock.elapsedRealtime()
                if (!forceRefresh) {
                    val cached = detailsCache[animeId]
                    if (cached != null && now - cached.first < CACHE_TTL_MS) {
                        _uiState.value = cached.second
                        refreshMyListStatus()
                        if (cached.second.recommendationMeta.isEmpty()) {
                            launchCardMetaRefresh(cached.second.details)
                        }
                        if (!cached.second.isSupplementaryLoaded || cached.second.scoreDistribution.isEmpty()) {
                            launchSupplementaryRefresh(cached.second.details)
                        }
                        return@launch
                    }
                }

                // Keep existing state if it's already success, to avoid flickering during refresh
                val currentState = _uiState.value
                if (currentState !is AnimeDetailsUiState.Success) {
                    _uiState.value = AnimeDetailsUiState.Loading
                }

                val staleCached = detailsCache[animeId]?.second
                val quickDetails = withTimeoutOrNull(1_500L) { repository.getAnimeDetailsLite(animeId) }
                if (quickDetails != null) {
                    publishBaseDetails(quickDetails)
                    return@launch
                }

                // Soft-timeout fallback: keep UI useful and continue loading in background.
                if (staleCached != null) {
                    _uiState.value = staleCached
                    refreshMyListStatus()
                    if (staleCached.recommendationMeta.isEmpty()) {
                        launchCardMetaRefresh(staleCached.details)
                    }
                    if (!staleCached.isSupplementaryLoaded || staleCached.scoreDistribution.isEmpty()) {
                        launchSupplementaryRefresh(staleCached.details)
                    }
                }

                val delayedDetails = repository.getAnimeDetailsLite(animeId)
                publishBaseDetails(delayedDetails)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = AnimeDetailsUiState.Error(e.message ?: "Failed to load details")
            }
        }
    }

    private fun publishBaseDetails(details: AnimeDetailsResponse) {
        val baseDetails = details.copy(recommendations = emptyList())
        val successState = AnimeDetailsUiState.Success(
            details = baseDetails,
            characters = emptyList(),
            staff = emptyList(),
            recommendations = emptyList(),
            themes = null,
            reviews = emptyList(),
            allReviewsCount = 0,
            streaming = emptyList(),
            airingMedia = null,
            isSupplementaryLoaded = false,
            isRecommendationsLoaded = false,
            isRecommendationsLoading = false,
            isPeopleLoaded = false,
            isPeopleLoading = false
        )
        detailsCache[animeId] = SystemClock.elapsedRealtime() to successState
        _uiState.value = successState
        refreshMyListStatus()
        launchCardMetaRefresh(baseDetails)
        launchSupplementaryRefresh(baseDetails)
    }

    private fun refreshMyListStatus() {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        myListStatusJob?.cancel()
        myListStatusJob = viewModelScope.launch {
            val freshStatus = runCatching {
                repository.getAnimeMyListStatus(animeId).myListStatus
            }.getOrNull() ?: return@launch

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch
            if (latest.details.myListStatus == freshStatus) return@launch

            val updated = latest.copy(
                details = latest.details.copy(myListStatus = freshStatus)
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun refreshCommunityStats() {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isCommunityStatsRefreshing) return

        communityStatsJob?.cancel()
        communityStatsJob = viewModelScope.launch {
            _uiState.value = current.copy(
                isCommunityStatsRefreshing = true,
                isScoreDistributionLoading = true,
                scoreDistributionError = null
            )

            val freshDetails = runCatching { repository.getAnimeDetailsLite(animeId) }.getOrNull()

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updatedDetails = freshDetails?.let {
                latest.details.copy(
                    numListUsers = it.numListUsers,
                    numScoringUsers = it.numScoringUsers,
                    statistics = it.statistics
                )
            } ?: latest.details

            val expectedScoringUsers = updatedDetails.numScoringUsers ?: latest.details.numScoringUsers ?: 0
            val scoreFetch = fetchScoreDistributionWithRetry(expectedScoringUsers)
            val shouldReplaceScoreDistribution = scoreFetch.errorMessage == null
            val nextScoreDistribution = if (shouldReplaceScoreDistribution) {
                scoreFetch.data
            } else {
                latest.scoreDistribution
            }

            val updated = latest.copy(
                details = updatedDetails,
                scoreDistribution = nextScoreDistribution,
                isCommunityStatsRefreshing = false,
                isScoreDistributionLoading = false,
                scoreDistributionError = scoreFetch.errorMessage,
                lastScoreDistributionUpdatedAt = if (shouldReplaceScoreDistribution) {
                    SystemClock.elapsedRealtime()
                } else {
                    latest.lastScoreDistributionUpdatedAt
                }
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun updateListStatus(
        status: String? = null,
        isRewatching: Boolean? = null,
        score: Int? = null,
        numWatchedEpisodes: Int? = null,
        priority: Int? = null,
        numTimesRewatched: Int? = null,
        rewatchValue: Int? = null,
        tags: String? = null,
        comments: String? = null,
        startDate: String? = null,
        finishDate: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.updateMyListStatus(
                    animeId = animeId,
                    status = status,
                    isRewatching = isRewatching,
                    score = score,
                    numWatchedEpisodes = numWatchedEpisodes,
                    priority = priority,
                    numTimesRewatched = numTimesRewatched,
                    rewatchValue = rewatchValue,
                    tags = tags,
                    comments = comments,
                    startDate = startDate,
                    finishDate = finishDate
                )
                loadDetails(forceRefresh = true) // Refresh to show new status
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadReviews(forceRefresh: Boolean = false, userTriggered: Boolean = false) {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isReviewsLoaded && !forceRefresh) return

        reviewsJob?.cancel()
        reviewsJob = viewModelScope.launch {
            val before = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isReviewsLoading = true)

            val reviews = if (userTriggered) {
                fetchWithRetry(HEAVY_RETRY_COUNT, HEAVY_RETRY_DELAY_MS) {
                    repository.getAnimeReviews(animeId).data
                }.getOrDefault(emptyList())
            } else {
                runCatching { repository.getAnimeReviews(animeId).data }.getOrDefault(emptyList())
            }
            val recommended = reviews.find { it.tags.any { tag -> tag.contains("Recommended", ignoreCase = true) && !tag.contains("Not", ignoreCase = true) } }
            val mixed = reviews.find { it.tags.any { tag -> tag.contains("Mixed", ignoreCase = true) } }
            val notRecommended = reviews.find { it.tags.any { tag -> tag.contains("Not Recommended", ignoreCase = true) } }
            val topReviews = listOfNotNull(recommended, mixed, notRecommended)
            val finalReviews = (topReviews + reviews).distinctBy { it.mal_id }.take(3)

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updated = latest.copy(
                reviews = finalReviews,
                allReviewsCount = reviews.size,
                isReviewsLoaded = true,
                isReviewsLoading = false
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun loadRecommendations(forceRefresh: Boolean = false, userTriggered: Boolean = false) {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isRecommendationsLoaded && !forceRefresh) return

        recommendationsJob?.cancel()
        recommendationsJob = viewModelScope.launch {
            val before = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isRecommendationsLoading = true)

            val recommendations = if (userTriggered) {
                fetchWithRetry(HEAVY_RETRY_COUNT, HEAVY_RETRY_DELAY_MS) {
                    repository.getAnimeRecommendationsOnly(animeId).recommendations.orEmpty()
                }.getOrDefault(emptyList())
            } else {
                runCatching {
                    repository.getAnimeRecommendationsOnly(animeId).recommendations.orEmpty()
                }.getOrDefault(emptyList())
            }

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updated = latest.copy(
                recommendations = recommendations,
                isRecommendationsLoaded = true,
                isRecommendationsLoading = false
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
            launchCardMetaRefresh(latest.details.copy(recommendations = recommendations))
        }
    }

    fun loadPeople(forceRefresh: Boolean = false) {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isPeopleLoaded && !forceRefresh) return

        peopleJob?.cancel()
        peopleJob = viewModelScope.launch {
            val before = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isPeopleLoading = true)

            val staff = runCatching {
                repository.getAnimeStaff(animeId).data
            }.getOrDefault(emptyList())

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updated = latest.copy(
                staff = staff,
                isPeopleLoaded = true,
                isPeopleLoading = false
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun refreshCharactersSection() {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isCharactersRefreshing) return

        charactersJob?.cancel()
        charactersJob = viewModelScope.launch {
            _uiState.value = current.copy(
                isCharactersRefreshing = true,
                charactersError = null
            )

            val refreshedCharacters = runCatching {
                repository.getAnimeCharacters(animeId).data
            }

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updated = latest.copy(
                characters = refreshedCharacters.getOrElse { latest.characters },
                isPeopleLoaded = true,
                isPeopleLoading = false,
                isCharactersRefreshing = false,
                charactersError = refreshedCharacters.exceptionOrNull()?.message
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun refreshStaffSection() {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isStaffRefreshing) return

        staffJob?.cancel()
        staffJob = viewModelScope.launch {
            _uiState.value = current.copy(
                isStaffRefreshing = true,
                staffError = null
            )

            val refreshedStaff = fetchWithRetry(HEAVY_RETRY_COUNT, HEAVY_RETRY_DELAY_MS) {
                repository.getAnimeStaff(animeId).data
            }

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updated = latest.copy(
                staff = refreshedStaff.getOrElse { latest.staff },
                isPeopleLoaded = true,
                isPeopleLoading = false,
                isStaffRefreshing = false,
                staffError = refreshedStaff.exceptionOrNull()?.message
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun refreshAvailability() {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isAvailabilityRefreshing) return

        availabilityJob?.cancel()
        availabilityJob = viewModelScope.launch {
            _uiState.value = current.copy(isAvailabilityRefreshing = true)

            val refreshedStreaming = runCatching {
                repository.getAnimeStreaming(animeId).data
            }.getOrDefault(current.streaming)

            val latest = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (latest.details.id != animeId) return@launch

            val updated = latest.copy(
                streaming = refreshedStreaming,
                isAvailabilityRefreshing = false
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    fun deleteFromList() {
        viewModelScope.launch {
            try {
                repository.deleteMyListStatus(animeId)
                loadDetails(forceRefresh = true) // Refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun getAnimeCardMeta(
        details: AnimeDetailsResponse
    ): Map<Int, RecommendationCardMeta> = supervisorScope {
        val targetIds = (
            details.recommendations.orEmpty().map { it.node.id } +
                details.relatedAnime.orEmpty().map { it.node.id }
            )
            .distinct()
            .take(MAX_CARD_META_FETCH)
        if (targetIds.isEmpty()) return@supervisorScope emptyMap()
        val now = SystemClock.elapsedRealtime()
        val result = mutableMapOf<Int, RecommendationCardMeta>()
        targetIds.chunked(2).forEach { batch ->
            batch
                .map { targetId ->
                    async {
                        val cached = recommendationMetaCache[targetId]
                        if (cached != null && now - cached.first < CACHE_TTL_MS) {
                            return@async targetId to cached.second
                        }
                        val fetchedDetails = runCatching { repository.getAnimeDetailsLite(targetId) }.getOrNull()
                        val meta = fetchedDetails?.let {
                            RecommendationCardMeta(
                                mean = it.mean,
                                members = it.numListUsers,
                                myListStatus = it.myListStatus,
                                alternativeTitles = it.alternativeTitles
                            )
                        } ?: RecommendationCardMeta()
                        recommendationMetaCache[targetId] = SystemClock.elapsedRealtime() to meta
                        targetId to meta
                    }
                }
                .forEach { deferred ->
                    val (id, meta) = deferred.await()
                    result[id] = meta
                }
        }
        result
    }

    private fun launchCardMetaRefresh(details: AnimeDetailsResponse) {
        cardMetaJob?.cancel()
        cardMetaJob = viewModelScope.launch {
            val meta = runCatching { getAnimeCardMeta(details) }.getOrDefault(emptyMap())
            if (meta.isEmpty()) return@launch
            val current = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (current.details.id != details.id) return@launch
            val merged = current.recommendationMeta + meta
            if (merged == current.recommendationMeta) return@launch
            val updated = current.copy(recommendationMeta = merged)
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }

    private fun launchSupplementaryRefresh(details: AnimeDetailsResponse) {
        supplementaryJob?.cancel()
        supplementaryJob = viewModelScope.launch {
            val before = _uiState.value as? AnimeDetailsUiState.Success
            if (before != null && before.details.id == details.id) {
                _uiState.value = before.copy(
                    isScoreDistributionLoading = true,
                    scoreDistributionError = null
                )
            }
            val enriched = runCatching {
                supervisorScope {
                    val charactersDeferred = async {
                        runCatching { repository.getAnimeCharacters(animeId).data }.getOrDefault(emptyList())
                    }
                    val themesDeferred = async {
                        runCatching { repository.getAnimeThemes(animeId).data }.getOrNull()
                    }
                    val streamingDeferred = async {
                        runCatching { repository.getAnimeStreaming(animeId).data }.getOrDefault(emptyList())
                    }
                    val airingDeferred = async {
                        runCatching { repository.getAiringAnimeDetails(listOf(animeId)).firstOrNull() }.getOrNull()
                    }

                    SupplementaryAnimeData(
                        characters = charactersDeferred.await(),
                        themes = themesDeferred.await(),
                        streaming = streamingDeferred.await(),
                        airingMedia = airingDeferred.await()
                    )
                }
            }.getOrNull()
            if (enriched == null) {
                val latest = _uiState.value as? AnimeDetailsUiState.Success
                if (latest != null && latest.details.id == details.id) {
                    val recovered = latest.copy(
                        isScoreDistributionLoading = false,
                        scoreDistributionError = latest.scoreDistributionError ?: "Supplementary refresh failed before score distribution could be updated."
                    )
                    detailsCache[animeId] = SystemClock.elapsedRealtime() to recovered
                    _uiState.value = recovered
                }
                return@launch
            }

            val current = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (current.details.id != details.id) return@launch

            val updated = current.copy(
                characters = enriched.characters,
                themes = enriched.themes,
                streaming = enriched.streaming,
                airingMedia = enriched.airingMedia,
                isSupplementaryLoaded = true
            )
            _uiState.value = updated

            val expectedScoringUsers = updated.details.numScoringUsers ?: 0
            val scoreFetch = fetchScoreDistributionWithRetry(expectedScoringUsers)
            val shouldReplaceScoreDistribution = scoreFetch.errorMessage == null
            val finalized = updated.copy(
                scoreDistribution = if (shouldReplaceScoreDistribution) scoreFetch.data else updated.scoreDistribution,
                isScoreDistributionLoading = false,
                scoreDistributionError = scoreFetch.errorMessage,
                lastScoreDistributionUpdatedAt = if (shouldReplaceScoreDistribution) {
                    SystemClock.elapsedRealtime()
                } else {
                    updated.lastScoreDistributionUpdatedAt
                }
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to finalized
            _uiState.value = finalized
        }
    }

    private suspend fun fetchScoreDistributionWithRetry(expectedScoringUsers: Int): ScoreDistributionFetchResult {
        var lastError: String? = null
        repeat(SCORE_RETRY_COUNT + 1) { attempt ->
            val result = runCatching { repository.getAnimeStatistics(animeId).data.scores }
            if (result.isSuccess) {
                val scores = result.getOrDefault(emptyList())
                if (scores.isNotEmpty() || expectedScoringUsers <= 0) {
                    return ScoreDistributionFetchResult(data = scores, errorMessage = null)
                }
                lastError = "Score buckets returned empty even though scorers exist."
            } else {
                lastError = result.exceptionOrNull()?.message ?: "Failed to fetch score distribution."
            }

            if (attempt < SCORE_RETRY_COUNT) {
                delay(SCORE_RETRY_DELAY_MS * (attempt + 1))
            }
        }
        return ScoreDistributionFetchResult(data = emptyList(), errorMessage = lastError)
    }

    private suspend fun <T> fetchWithRetry(
        retries: Int,
        baseDelayMs: Long,
        block: suspend () -> T
    ): Result<T> {
        var lastResult: Result<T>? = null
        repeat(retries + 1) { attempt ->
            val current = runCatching { block() }
            if (current.isSuccess) return current
            lastResult = current
            if (attempt < retries) {
                delay(baseDelayMs * (attempt + 1))
            }
        }
        return lastResult ?: runCatching { block() }
    }
}

sealed interface AnimeDetailsUiState {
    data object Loading : AnimeDetailsUiState
    data class Success(
        val details: AnimeDetailsResponse,
        val characters: List<JikanCharacterData>,
        val staff: List<JikanStaffData>,
        val recommendations: List<Recommendation>,
        val themes: JikanThemesData?,
        val reviews: List<JikanReviewData>,
        val allReviewsCount: Int,
        val streaming: List<JikanStreamingData>,
        val scoreDistribution: List<JikanAnimeScoreBucket> = emptyList(),
        val airingMedia: AniListMedia?,
        val recommendationMeta: Map<Int, RecommendationCardMeta> = emptyMap(),
        val isSupplementaryLoaded: Boolean = false,
        val isReviewsLoaded: Boolean = false,
        val isReviewsLoading: Boolean = false,
        val isRecommendationsLoaded: Boolean = false,
        val isRecommendationsLoading: Boolean = false,
        val isPeopleLoaded: Boolean = false,
        val isPeopleLoading: Boolean = false,
        val isCommunityStatsRefreshing: Boolean = false,
        val isScoreDistributionLoading: Boolean = false,
        val scoreDistributionError: String? = null,
        val lastScoreDistributionUpdatedAt: Long? = null,
        val isAvailabilityRefreshing: Boolean = false,
        val isCharactersRefreshing: Boolean = false,
        val isStaffRefreshing: Boolean = false,
        val charactersError: String? = null,
        val staffError: String? = null
    ) : AnimeDetailsUiState
    data class Error(val message: String) : AnimeDetailsUiState
}

data class RecommendationCardMeta(
    val mean: Float? = null,
    val members: Int? = null,
    val myListStatus: MyListStatus? = null,
    val alternativeTitles: AlternativeTitles? = null
)

private data class SupplementaryAnimeData(
    val characters: List<JikanCharacterData>,
    val themes: JikanThemesData?,
    val streaming: List<JikanStreamingData>,
    val airingMedia: AniListMedia?
)

private data class ScoreDistributionFetchResult(
    val data: List<JikanAnimeScoreBucket>,
    val errorMessage: String?
)
