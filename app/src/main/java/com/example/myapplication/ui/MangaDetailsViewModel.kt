package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mangaId: Int = checkNotNull(savedStateHandle["mangaId"])

    private val _uiState = MutableStateFlow<MangaDetailsUiState>(MangaDetailsUiState.Loading)
    val uiState: StateFlow<MangaDetailsUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState !is MangaDetailsUiState.Success) {
                    _uiState.value = MangaDetailsUiState.Loading
                }
                
                val details = repository.getMangaDetails(mangaId)
                _uiState.value = MangaDetailsUiState.Success(details)
            } catch (e: Exception) {
                _uiState.value = MangaDetailsUiState.Error(e.message ?: "Failed to load details")
            }
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
                loadDetails()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteFromList() {
        viewModelScope.launch {
            try {
                repository.deleteMyMangaListStatus(mangaId)
                loadDetails()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed interface MangaDetailsUiState {
    data object Loading : MangaDetailsUiState
    data class Success(val details: MangaDetailsResponse) : MangaDetailsUiState
    data class Error(val message: String) : MangaDetailsUiState
}
