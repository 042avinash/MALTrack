package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.Recommendation
import com.example.myapplication.data.remote.JikanCharacterData
import com.example.myapplication.data.remote.JikanReviewData
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
        private val detailsCache = mutableMapOf<Int, Pair<Long, AnimeDetailsUiState.Success>>()
        private val recommendationMetaCache = mutableMapOf<Int, Pair<Long, RecommendationCardMeta>>()
    }

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])
    private var cardMetaJob: Job? = null
    private var supplementaryJob: Job? = null
    private var reviewsJob: Job? = null
    private var recommendationsJob: Job? = null

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
                        if (cached.second.recommendationMeta.isEmpty()) {
                            launchCardMetaRefresh(cached.second.details)
                        }
                        if (!cached.second.isSupplementaryLoaded) {
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
                    if (staleCached.recommendationMeta.isEmpty()) {
                        launchCardMetaRefresh(staleCached.details)
                    }
                    if (!staleCached.isSupplementaryLoaded) {
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
            recommendations = emptyList(),
            themes = null,
            reviews = emptyList(),
            allReviewsCount = 0,
            streaming = emptyList(),
            airingMedia = null,
            isSupplementaryLoaded = false,
            isRecommendationsLoaded = false,
            isRecommendationsLoading = false
        )
        detailsCache[animeId] = SystemClock.elapsedRealtime() to successState
        _uiState.value = successState
        launchCardMetaRefresh(baseDetails)
        launchSupplementaryRefresh(baseDetails)
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

    fun loadReviews(forceRefresh: Boolean = false) {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isReviewsLoaded && !forceRefresh) return

        reviewsJob?.cancel()
        reviewsJob = viewModelScope.launch {
            val before = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isReviewsLoading = true)

            val reviews = runCatching { repository.getAnimeReviews(animeId).data }.getOrDefault(emptyList())
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

    fun loadRecommendations(forceRefresh: Boolean = false) {
        val current = _uiState.value as? AnimeDetailsUiState.Success ?: return
        if (current.isRecommendationsLoaded && !forceRefresh) return

        recommendationsJob?.cancel()
        recommendationsJob = viewModelScope.launch {
            val before = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            _uiState.value = before.copy(isRecommendationsLoading = true)

            val recommendations = runCatching {
                repository.getAnimeRecommendationsOnly(animeId).recommendations.orEmpty()
            }.getOrDefault(emptyList())

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
                                myListStatus = it.myListStatus
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
            }.getOrNull() ?: return@launch

            val current = _uiState.value as? AnimeDetailsUiState.Success ?: return@launch
            if (current.details.id != details.id) return@launch

            val updated = current.copy(
                characters = enriched.characters,
                themes = enriched.themes,
                streaming = enriched.streaming,
                airingMedia = enriched.airingMedia,
                isSupplementaryLoaded = true
            )
            detailsCache[animeId] = SystemClock.elapsedRealtime() to updated
            _uiState.value = updated
        }
    }
}

sealed interface AnimeDetailsUiState {
    data object Loading : AnimeDetailsUiState
    data class Success(
        val details: AnimeDetailsResponse,
        val characters: List<JikanCharacterData>,
        val recommendations: List<Recommendation>,
        val themes: JikanThemesData?,
        val reviews: List<JikanReviewData>,
        val allReviewsCount: Int,
        val streaming: List<JikanStreamingData>,
        val airingMedia: AniListMedia?,
        val recommendationMeta: Map<Int, RecommendationCardMeta> = emptyMap(),
        val isSupplementaryLoaded: Boolean = false,
        val isReviewsLoaded: Boolean = false,
        val isReviewsLoading: Boolean = false,
        val isRecommendationsLoaded: Boolean = false,
        val isRecommendationsLoading: Boolean = false
    ) : AnimeDetailsUiState
    data class Error(val message: String) : AnimeDetailsUiState
}

data class RecommendationCardMeta(
    val mean: Float? = null,
    val members: Int? = null,
    val myListStatus: MyListStatus? = null
)

private data class SupplementaryAnimeData(
    val characters: List<JikanCharacterData>,
    val themes: JikanThemesData?,
    val streaming: List<JikanStreamingData>,
    val airingMedia: AniListMedia?
)
