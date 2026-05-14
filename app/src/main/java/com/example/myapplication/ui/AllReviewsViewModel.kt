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
        private const val HEAVY_RETRY_COUNT = 2
        private const val HEAVY_RETRY_DELAY_MS = 800L
        private val reviewsCache = mutableMapOf<Int, Pair<Long, List<JikanReviewData>>>()
    }

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow<AllReviewsUiState>(AllReviewsUiState.Loading)
    val uiState: StateFlow<AllReviewsUiState> = _uiState.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadReviews()
    }

    fun refreshReviews() {
        loadReviews(forceRefresh = true)
    }

    private fun loadReviews(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                val now = SystemClock.elapsedRealtime()
                val cached = reviewsCache[animeId]
                if (!forceRefresh && cached != null && now - cached.first < CACHE_TTL_MS) {
                    _uiState.value = AllReviewsUiState.Success(cached.second)
                    return@launch
                }

                if (forceRefresh) {
                    _isRefreshing.value = true
                } else {
                    _uiState.value = AllReviewsUiState.Loading
                }
                val reviews = if (forceRefresh) {
                    fetchWithRetry(HEAVY_RETRY_COUNT, HEAVY_RETRY_DELAY_MS) {
                        repository.getAnimeReviews(animeId).data
                    }.getOrThrow()
                } else {
                    repository.getAnimeReviews(animeId).data
                }
                reviewsCache[animeId] = SystemClock.elapsedRealtime() to reviews
                _uiState.value = AllReviewsUiState.Success(reviews)
            } catch (e: Exception) {
                _uiState.value = AllReviewsUiState.Error(e.message ?: "Failed to load reviews")
            } finally {
                _isRefreshing.value = false
            }
        }
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
                kotlinx.coroutines.delay(baseDelayMs * (attempt + 1))
            }
        }
        return lastResult ?: runCatching { block() }
    }
}

sealed interface AllReviewsUiState {
    data object Loading : AllReviewsUiState
    data class Success(val reviews: List<JikanReviewData>) : AllReviewsUiState
    data class Error(val message: String) : AllReviewsUiState
}
