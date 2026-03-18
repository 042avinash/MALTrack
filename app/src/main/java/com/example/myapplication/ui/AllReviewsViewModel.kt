package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val animeId: Int = checkNotNull(savedStateHandle["animeId"])

    private val _uiState = MutableStateFlow<AllReviewsUiState>(AllReviewsUiState.Loading)
    val uiState: StateFlow<AllReviewsUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            try {
                _uiState.value = AllReviewsUiState.Loading
                val reviews = repository.getAnimeReviews(animeId).data
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
