package com.example.myapplication.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.model.MyMangaListStatus
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
import javax.inject.Inject

@HiltViewModel
class MangaDetailsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val CACHE_TTL_MS = 10 * 60 * 1000L
        private val detailsCache = mutableMapOf<Int, Pair<Long, MangaDetailsUiState.Success>>()
        private val mangaCardMetaCache = mutableMapOf<Int, Pair<Long, MangaCardMeta>>()
    }

    private val mangaId: Int = checkNotNull(savedStateHandle["mangaId"])
    private var cardMetaJob: Job? = null

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
                
                val details = repository.getMangaDetails(mangaId)
                val successState = MangaDetailsUiState.Success(details)
                detailsCache[mangaId] = SystemClock.elapsedRealtime() to successState
                _uiState.value = successState
                launchCardMetaRefresh(details)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = MangaDetailsUiState.Error(e.message ?: "Failed to load details")
            }
        }
    }

    private suspend fun getMangaCardMeta(
        details: MangaDetailsResponse
    ): Map<Int, MangaCardMeta> = supervisorScope {
        val targetIds = (
            details.relatedManga.orEmpty().map { it.node.id } +
                details.recommendations.orEmpty().map { it.node.id }
            )
            .distinct()
            .take(8)
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
                        val fetched = runCatching { repository.getMangaDetails(mangaId) }.getOrNull()
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
        val cardMeta: Map<Int, MangaCardMeta> = emptyMap()
    ) : MangaDetailsUiState
    data class Error(val message: String) : MangaDetailsUiState
}

data class MangaCardMeta(
    val mean: Float? = null,
    val members: Int? = null,
    val myListStatus: MyMangaListStatus? = null
)
