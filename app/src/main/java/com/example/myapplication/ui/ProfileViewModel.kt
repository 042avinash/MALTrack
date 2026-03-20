package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.JikanFullUserProfile
import com.example.myapplication.data.model.UserProfile
import com.example.myapplication.data.remote.JikanFriend
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import android.os.SystemClock
import java.net.SocketTimeoutException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    companion object {
        private const val PROFILE_CACHE_TTL_MS = 10 * 60 * 1000L
        private const val MAX_FAVORITE_META_FETCH = 12
        private val globalCachedProfiles = mutableMapOf<String, ProfileUiState.Success>()
        private val globalCachedProfileTimestamps = mutableMapOf<String, Long>()
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private var loadJob: Job? = null
    private var favoriteMetaJob: Job? = null

    fun getProfile(username: String? = null) {
        val targetUsername = if (username == "null") null else username
        val normalizedUsername = targetUsername ?: "__self__"
        val now = SystemClock.elapsedRealtime()
        val cachedState = globalCachedProfiles[normalizedUsername]
        val cacheTs = globalCachedProfileTimestamps[normalizedUsername]
        val hasFreshCache = cachedState != null && cacheTs != null && now - cacheTs < PROFILE_CACHE_TTL_MS

        if (hasFreshCache) {
            _uiState.value = cachedState
            return
        }
        if (cachedState != null) {
            _uiState.value = cachedState
        }
        
        loadJob?.cancel()
        favoriteMetaJob?.cancel()
        loadJob = viewModelScope.launch {
            if (cachedState == null) {
                _uiState.value = ProfileUiState.Loading
            }
            try {
                if (targetUsername == null) {
                    val malProfile = withTimeoutRetry { repository.getMyUserProfile() }
                    val fullProfile = withTimeoutRetry { repository.getUserFullProfile(malProfile.name) }
                    val friends = try { 
                        withTimeoutRetry { repository.getUserFriends(malProfile.name) }
                    } catch (_: Exception) { 
                        emptyList<JikanFriend>() 
                    }
                    val successState = ProfileUiState.Success(
                        malProfile,
                        fullProfile,
                        friends,
                        isOwnProfile = true,
                        animeFavoriteMeta = cachedState?.animeFavoriteMeta.orEmpty(),
                        mangaFavoriteMeta = cachedState?.mangaFavoriteMeta.orEmpty()
                    )
                    globalCachedProfiles[normalizedUsername] = successState
                    globalCachedProfileTimestamps[normalizedUsername] = SystemClock.elapsedRealtime()
                    _uiState.value = successState
                    loadFavoriteMetaInBackground(normalizedUsername, fullProfile, successState)
                } else {
                    val fullProfile = withTimeoutRetry { repository.getUserFullProfile(targetUsername) }
                    val friends = try { 
                        withTimeoutRetry { repository.getUserFriends(targetUsername) }
                    } catch (_: Exception) { 
                        emptyList<JikanFriend>() 
                    }
                    val successState = ProfileUiState.Success(
                        null,
                        fullProfile,
                        friends,
                        isOwnProfile = false,
                        animeFavoriteMeta = cachedState?.animeFavoriteMeta.orEmpty(),
                        mangaFavoriteMeta = cachedState?.mangaFavoriteMeta.orEmpty()
                    )
                    globalCachedProfiles[normalizedUsername] = successState
                    globalCachedProfileTimestamps[normalizedUsername] = SystemClock.elapsedRealtime()
                    _uiState.value = successState
                    loadFavoriteMetaInBackground(normalizedUsername, fullProfile, successState)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (cachedState == null) {
                    _errorMessage.value = getFriendlyErrorMessage(e)
                }
                _uiState.value = cachedState ?: ProfileUiState.Error(e.message ?: "Failed to load profile. Please check if the username is correct.")
            }
        }
    }

    private fun loadFavoriteMetaInBackground(
        cacheKey: String,
        fullProfile: JikanFullUserProfile,
        baseState: ProfileUiState.Success
    ) {
        favoriteMetaJob?.cancel()
        favoriteMetaJob = viewModelScope.launch {
            val (animeFavoriteMeta, mangaFavoriteMeta) = runCatching {
                loadFavoriteMeta(fullProfile)
            }.getOrDefault(emptyMap<Int, FavoriteMediaMeta>() to emptyMap<Int, FavoriteMediaMeta>())

            val updatedState = baseState.copy(
                animeFavoriteMeta = animeFavoriteMeta,
                mangaFavoriteMeta = mangaFavoriteMeta
            )
            globalCachedProfiles[cacheKey] = updatedState
            globalCachedProfileTimestamps[cacheKey] = SystemClock.elapsedRealtime()

            val currentSuccess = _uiState.value as? ProfileUiState.Success
            if (currentSuccess != null && currentSuccess.jikanUser.username == baseState.jikanUser.username) {
                _uiState.value = updatedState
            }
        }
    }

    private suspend fun <T> withTimeoutRetry(block: suspend () -> T): T {
        var lastError: Throwable? = null
        val delays = listOf(0L, 350L, 900L)
        repeat(delays.size) { attempt ->
            try {
                if (delays[attempt] > 0) delay(delays[attempt])
                return block()
            } catch (e: Throwable) {
                lastError = e
                if (!isTimeoutError(e) || attempt == delays.lastIndex) throw e
            }
        }
        throw lastError ?: IllegalStateException("Unknown profile fetch error")
    }

    private fun isTimeoutError(error: Throwable): Boolean {
        if (error is SocketTimeoutException) return true
        if (error is IOException && error.message?.contains("timeout", ignoreCase = true) == true) return true
        val cause = error.cause
        return cause != null && isTimeoutError(cause)
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun getFriendlyErrorMessage(error: Throwable): String {
        return if (isTimeoutError(error)) {
            "Request timed out. MAL or Jikan may be responding slowly, too many requests may have fired close together, a request may have stalled while switching screens, or your network may be unstable."
        } else {
            error.message ?: "Something went wrong."
        }
    }

    private suspend fun loadFavoriteMeta(
        fullProfile: JikanFullUserProfile
    ): Pair<Map<Int, FavoriteMediaMeta>, Map<Int, FavoriteMediaMeta>> = supervisorScope {
        val animeIds = fullProfile.favorites?.anime.orEmpty().map { it.mal_id }.distinct().take(MAX_FAVORITE_META_FETCH)
        val mangaIds = fullProfile.favorites?.manga.orEmpty().map { it.mal_id }.distinct().take(MAX_FAVORITE_META_FETCH)

        val animeMeta = mutableMapOf<Int, FavoriteMediaMeta>()
        animeIds.chunked(2).forEach { batch ->
            batch.map { id ->
                async {
                    id to runCatching { repository.getAnimeDetails(id) }
                        .getOrNull()
                        ?.let { FavoriteMediaMeta(it.mean, it.numListUsers) }
                }
            }.forEach { deferred ->
                val (id, meta) = deferred.await()
                if (meta != null) animeMeta[id] = meta
            }
        }

        val mangaMeta = mutableMapOf<Int, FavoriteMediaMeta>()
        mangaIds.chunked(2).forEach { batch ->
            batch.map { id ->
                async {
                    id to runCatching { repository.getMangaDetails(id) }
                        .getOrNull()
                        ?.let { FavoriteMediaMeta(it.mean, it.numListUsers) }
                }
            }.forEach { deferred ->
                val (id, meta) = deferred.await()
                if (meta != null) mangaMeta[id] = meta
            }
        }

        animeMeta to mangaMeta
    }
}

data class FavoriteMediaMeta(
    val mean: Float? = null,
    val numListUsers: Int? = null
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val malUser: UserProfile?, 
        val jikanUser: JikanFullUserProfile,
        val friends: List<JikanFriend>,
        val isOwnProfile: Boolean,
        val animeFavoriteMeta: Map<Int, FavoriteMediaMeta> = emptyMap(),
        val mangaFavoriteMeta: Map<Int, FavoriteMediaMeta> = emptyMap()
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
