package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.remote.JikanCharacterData
import com.example.myapplication.data.remote.JikanReviewData
import com.example.myapplication.data.remote.JikanStreamingData
import com.example.myapplication.data.remote.JikanThemesData
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow<AnimeDetailsUiState>(AnimeDetailsUiState.Loading)
    val uiState: StateFlow<AnimeDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            try {
                // Keep existing state if it's already success, to avoid flickering during refresh
                val currentState = _uiState.value
                if (currentState !is AnimeDetailsUiState.Success) {
                    _uiState.value = AnimeDetailsUiState.Loading
                }
                
                supervisorScope {
                    val detailsDeferred = async { repository.getAnimeDetails(animeId) }
                    val charactersDeferred = async { 
                        try { repository.getAnimeCharacters(animeId).data } catch (e: Exception) { emptyList() } 
                    }
                    val themesDeferred = async { 
                        try { repository.getAnimeThemes(animeId).data } catch (e: Exception) { null } 
                    }
                    val reviewsDeferred = async { 
                        try { repository.getAnimeReviews(animeId).data } catch (e: Exception) { emptyList() } 
                    }
                    val streamingDeferred = async {
                        try { repository.getAnimeStreaming(animeId).data } catch (e: Exception) { emptyList() }
                    }
                    val airingDeferred = async {
                        try { repository.getAiringAnimeDetails(listOf(animeId)).firstOrNull() } catch (e: Exception) { null }
                    }
                    
                    val details = detailsDeferred.await()
                    val characters = charactersDeferred.await()
                    val themes = themesDeferred.await()
                    val reviews = reviewsDeferred.await()
                    val streaming = streamingDeferred.await()
                    val airingMedia = airingDeferred.await()

                    // Filter 3 reviews: Recommended, Mixed, Not Recommended based on tags
                    val recommended = reviews.find { it.tags.any { tag -> tag.contains("Recommended", ignoreCase = true) && !tag.contains("Not", ignoreCase = true) } }
                    val mixed = reviews.find { it.tags.any { tag -> tag.contains("Mixed", ignoreCase = true) } }
                    val notRecommended = reviews.find { it.tags.any { tag -> tag.contains("Not Recommended", ignoreCase = true) } }
                    
                    val topReviews = listOfNotNull(recommended, mixed, notRecommended)
                    val finalReviews = (topReviews + reviews).distinctBy { it.mal_id }.take(3)

                    _uiState.value = AnimeDetailsUiState.Success(
                        details = details,
                        characters = characters,
                        themes = themes,
                        reviews = finalReviews,
                        allReviewsCount = reviews.size,
                        streaming = streaming,
                        airingMedia = airingMedia
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = AnimeDetailsUiState.Error(e.message ?: "Failed to load details")
            }
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
                loadDetails() // Refresh to show new status
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteFromList() {
        viewModelScope.launch {
            try {
                repository.deleteMyListStatus(animeId)
                loadDetails() // Refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed interface AnimeDetailsUiState {
    data object Loading : AnimeDetailsUiState
    data class Success(
        val details: AnimeDetailsResponse,
        val characters: List<JikanCharacterData>,
        val themes: JikanThemesData?,
        val reviews: List<JikanReviewData>,
        val allReviewsCount: Int,
        val streaming: List<JikanStreamingData>,
        val airingMedia: AniListMedia?
    ) : AnimeDetailsUiState
    data class Error(val message: String) : AnimeDetailsUiState
}
