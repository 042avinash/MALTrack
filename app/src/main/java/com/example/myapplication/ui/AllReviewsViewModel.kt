package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.example.myapplication.data.remote.JikanReviewData
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllReviewsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val CACHE_TTL_MS = 10 * 60 * 1000L
        private val reviewsCache = mutableMapOf<Int, Pair<Long, List<JikanReviewData>>>()
    }

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow<AllReviewsUiState>(AllReviewsUiState.Loading)
    val uiState: StateFlow<AllReviewsUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            try {
                val now = SystemClock.elapsedRealtime()
                val cached = reviewsCache[animeId]
                if (cached != null && now - cached.first < CACHE_TTL_MS) {
                    _uiState.value = AllReviewsUiState.Success(cached.second)
                    return@launch
                }

                _uiState.value = AllReviewsUiState.Loading
                val reviews = repository.getAnimeReviews(animeId).data
                reviewsCache[animeId] = SystemClock.elapsedRealtime() to reviews
                _uiState.value = AllReviewsUiState.Success(reviews)
            } catch (e: Exception) {
                _uiState.value = AllReviewsUiState.Error(e.message ?: "Failed to load reviews")
            }
        }
    }
}

sealed interface AllReviewsUiState {
    data object Loading : AllReviewsUiState
    data class Success(val reviews: List<JikanReviewData>) : AllReviewsUiState
    data class Error(val message: String) : AllReviewsUiState
}
