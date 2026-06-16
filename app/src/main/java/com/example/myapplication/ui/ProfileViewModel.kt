package com.example.myapplication.ui

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.model.JikanFullUserProfile
import com.example.myapplication.data.model.UserProfile
import com.example.myapplication.data.remote.JikanFriend
import com.example.myapplication.data.repository.AnimeRepository
import com.example.myapplication.data.repository.JikanProfileFetchException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ProfileViewModel"
        private const val PROFILE_CACHE_TTL_MS = 10 * 60 * 1000L
        private const val FAVORITE_ANIME_CACHE_TTL_MS = 24 * 60 * 60 * 1000L
        private const val VIEWER_NAME_CACHE_TTL_MS = 10 * 60 * 1000L
        private const val VIEWER_FRIENDS_CACHE_TTL_MS = 10 * 60 * 1000L
        private val globalCachedProfiles = mutableMapOf<String, ProfileUiState.Success>()
        private val globalCachedProfileTimestamps = mutableMapOf<String, Long>()
        private val globalFavoriteAnimeDetails = mutableMapOf<Int, Pair<Long, AnimeDetailsResponse>>()
        private var globalViewerName: String? = null
        private var globalViewerNameTimestamp: Long = 0L
        private val globalViewerFriends = mutableMapOf<String, Pair<Long, Set<String>>>()
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private var loadJob: Job? = null
    private var friendsJob: Job? = null

    fun retryProfile(username: String? = null) {
        val targetUsername = if (username == "null") null else username
        val normalizedUsername = targetUsername ?: "__self__"
        globalCachedProfiles.remove(normalizedUsername)
        globalCachedProfileTimestamps.remove(normalizedUsername)
        _errorMessage.value = null
        _uiState.value = ProfileUiState.Loading
        getProfile(username, forceRefresh = true)
    }

    fun getProfile(username: String? = null, forceRefresh: Boolean = false) {
        val targetUsername = if (username == "null") null else username
        val normalizedUsername = targetUsername ?: "__self__"
        if (forceRefresh) {
            globalCachedProfiles.remove(normalizedUsername)
            globalCachedProfileTimestamps.remove(normalizedUsername)
        }
        val now = SystemClock.elapsedRealtime()
        val cachedState = globalCachedProfiles[normalizedUsername]
        val cacheTs = globalCachedProfileTimestamps[normalizedUsername]
        val hasFreshCache = cachedState != null && cacheTs != null && now - cacheTs < PROFILE_CACHE_TTL_MS

        if (!forceRefresh && hasFreshCache) {
            _uiState.value = cachedState
            return
        }
        if (!forceRefresh && cachedState != null) {
            _uiState.value = cachedState
        }

        loadJob?.cancel()
        friendsJob?.cancel()
        loadJob = viewModelScope.launch {
            if (cachedState == null) {
                _uiState.value = ProfileUiState.Loading
            }
            try {
                if (targetUsername == null) {
                    val malProfile = withTimeoutRetry { repository.getMyUserProfile() }
                    globalViewerName = malProfile.name
                    globalViewerNameTimestamp = SystemClock.elapsedRealtime()
                    val fullProfile = withTimeoutRetry { repository.getUserFullProfile(malProfile.name) }
                    val successState = ProfileUiState.Success(
                        malUser = malProfile,
                        jikanUser = fullProfile,
                        friends = cachedState?.friends.orEmpty(),
                        isOwnProfile = true,
                        viewerIsFriendWithProfileOwner = null,
                        friendsLoaded = cachedState?.friendsLoaded ?: false
                    )
                    globalCachedProfiles[normalizedUsername] = successState
                    globalCachedProfileTimestamps[normalizedUsername] = SystemClock.elapsedRealtime()
                    _uiState.value = successState
                } else {
                    val fullProfile = withTimeoutRetry { repository.getUserFullProfile(targetUsername) }
                    val viewerIsFriendWithProfileOwner = resolveViewerFriendStatus(targetUsername)
                    val successState = ProfileUiState.Success(
                        malUser = null,
                        jikanUser = fullProfile,
                        friends = cachedState?.friends.orEmpty(),
                        isOwnProfile = false,
                        viewerIsFriendWithProfileOwner = viewerIsFriendWithProfileOwner,
                        friendsLoaded = cachedState?.friendsLoaded ?: false
                    )
                    globalCachedProfiles[normalizedUsername] = successState
                    globalCachedProfileTimestamps[normalizedUsername] = SystemClock.elapsedRealtime()
                    _uiState.value = successState
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val friendlyError = getFriendlyErrorMessage(e)
                Log.w(TAG, "Profile load failed for ${targetUsername ?: "@me"}: ${diagnosticErrorSummary(e)}", e)
                _errorMessage.value = friendlyError
                _uiState.value = cachedState ?: ProfileUiState.Error(
                    message = friendlyError,
                    details = diagnosticErrorSummary(e)
                )
            }
        }
    }

    fun loadFriends(username: String? = null, forceRefresh: Boolean = false) {
        val state = _uiState.value as? ProfileUiState.Success ?: return
        if (state.friendsLoading) return
        if (!forceRefresh && state.friendsLoaded && state.friends.isNotEmpty()) return
        val targetUsername = if (username == "null") null else username
        val usernameToLoad = targetUsername ?: state.jikanUser.username

        friendsJob?.cancel()
        _uiState.value = state.copy(friendsLoading = true, friendsError = null)
        friendsJob = viewModelScope.launch {
            val result = runCatching { withTimeoutRetry { repository.getUserFriends(usernameToLoad) } }
            val current = _uiState.value as? ProfileUiState.Success ?: return@launch
            if (result.isSuccess) {
                _uiState.value = current.copy(
                    friends = result.getOrDefault(emptyList()),
                    friendsLoading = false,
                    friendsLoaded = true,
                    friendsError = null
                )
            } else {
                _uiState.value = current.copy(
                    friendsLoading = false,
                    friendsLoaded = true,
                    friendsError = getFriendlyErrorMessage(result.exceptionOrNull() ?: Exception("Failed to load friends"))
                )
            }
            updateCacheForCurrentProfile()
        }
    }

    private fun updateCacheForCurrentProfile() {
        val current = _uiState.value as? ProfileUiState.Success ?: return
        val key = if (current.isOwnProfile) "__self__" else current.jikanUser.username
        globalCachedProfiles[key] = current
        globalCachedProfileTimestamps[key] = SystemClock.elapsedRealtime()
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

    private suspend fun resolveViewerFriendStatus(targetUsername: String): Boolean? {
        val viewerName = getCachedViewerName() ?: return null
        val now = SystemClock.elapsedRealtime()
        val cachedFriendUsernames = globalViewerFriends[viewerName]
        val usernames = if (cachedFriendUsernames != null && now - cachedFriendUsernames.first < VIEWER_FRIENDS_CACHE_TTL_MS) {
            cachedFriendUsernames.second
        } else {
            val freshUsernames = runCatching {
                withTimeoutRetry { repository.getUserFriends(viewerName) }
                    .map { it.user.username }
                    .toSet()
            }.getOrNull() ?: return null
            globalViewerFriends[viewerName] = SystemClock.elapsedRealtime() to freshUsernames
            freshUsernames
        }
        return usernames.any { it.equals(targetUsername, ignoreCase = true) }
    }

    private suspend fun getCachedViewerName(): String? {
        val now = SystemClock.elapsedRealtime()
        globalViewerName?.takeIf { now - globalViewerNameTimestamp < VIEWER_NAME_CACHE_TTL_MS }?.let {
            return it
        }
        return runCatching { repository.getMyUserProfile().name }.getOrNull()?.also {
            globalViewerName = it
            globalViewerNameTimestamp = SystemClock.elapsedRealtime()
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    suspend fun getFavoriteAnimeDetails(animeId: Int, forceRefresh: Boolean = false): AnimeDetailsResponse? {
        val now = SystemClock.elapsedRealtime()
        val cached = globalFavoriteAnimeDetails[animeId]
        val hasFreshCache = cached != null && now - cached.first < FAVORITE_ANIME_CACHE_TTL_MS
        if (!forceRefresh && hasFreshCache) {
            return cached?.second
        }
        return runCatching {
            withTimeoutRetry { repository.getAnimeDetails(animeId) }
        }.getOrNull()?.also {
            globalFavoriteAnimeDetails[animeId] = SystemClock.elapsedRealtime() to it
        }
    }

    private fun getFriendlyErrorMessage(error: Throwable): String {
        val status = when (error) {
            is JikanProfileFetchException -> error.status
            is HttpException -> error.code()
            else -> null
        }
        val label = when {
            status == 404 -> "Profile not found"
            status == 429 -> "Too many requests"
            status != null && status in 500..599 -> "Server issue"
            isTimeoutError(error) -> "Request timed out"
            error is UnknownHostException -> "Network unavailable"
            error is ConnectException -> "Connection failed"
            error is SocketException -> "Socket error"
            else -> "Profile data unavailable"
        }
        val detail = when {
            status == 404 -> "The user may not exist or the profile may be hidden."
            status == 429 -> "Please wait a bit and try again."
            status != null && status in 500..599 -> "Jikan is currently having issues."
            isTimeoutError(error) -> "The request took too long."
            error is UnknownHostException -> "The device could not reach the network."
            error is ConnectException -> "The connection could not be established."
            error is SocketException -> "The connection was interrupted."
            else -> "Please try again later."
        }
        return if (status != null) {
            "$label (HTTP $status). $detail"
        } else {
            "$label. $detail"
        }
    }

    private fun diagnosticErrorLabel(error: Throwable): String {
        if (isTimeoutError(error)) return "timeout"
        if (error is HttpException) return "http_${error.code()}"
        if (error is UnknownHostException) return "dns_lookup_failed"
        if (error is ConnectException) return "connection_failed"
        if (error is SocketException) return "socket_error"
        if (error is IOException) {
            val message = error.message.orEmpty().lowercase()
            return when {
                "ssl" in message || "handshake" in message -> "ssl_handshake_failed"
                "reset" in message -> "connection_reset"
                "refused" in message -> "connection_refused"
                "route to host" in message -> "no_route_to_host"
                "closed" in message -> "connection_closed"
                else -> "io_error"
            }
        }
        error.cause?.let { cause ->
            val nested = diagnosticErrorLabel(cause)
            if (nested != "unknown_error") return nested
        }
        return error::class.java.simpleName.ifBlank { "unknown_error" }
    }

    private fun diagnosticErrorSummary(error: Throwable, depth: Int = 0): String {
        if (error is JikanProfileFetchException) {
            val status = error.status?.toString() ?: "unknown"
            val responseMessage = error.responseMessage?.takeIf { it.isNotBlank() }
            val responseError = error.responseError?.takeIf { it.isNotBlank() }
            return buildString {
                append("JikanProfileFetchException(status=")
                append(status)
                append(", username=")
                append(error.username)
                append(")")
                responseMessage?.let {
                    append(": ")
                    append(it)
                }
                if (responseError != null && responseError != responseMessage) {
                    append(" | error=")
                    append(responseError)
                }
            }
        }
        val className = error::class.java.simpleName.ifBlank { "UnknownThrowable" }
        val message = error.message?.takeIf { it.isNotBlank() }?.trim()
        val current = buildString {
            append(className)
            if (message != null) {
                append(": ")
                append(message)
            }
        }
        val cause = error.cause ?: return current
        if (depth >= 3) return "$current | cause=..."
        return "$current | cause=${diagnosticErrorSummary(cause, depth + 1)}"
    }

}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val malUser: UserProfile?,
        val jikanUser: JikanFullUserProfile,
        val friends: List<JikanFriend>,
        val isOwnProfile: Boolean,
        val viewerIsFriendWithProfileOwner: Boolean? = null,
        val friendsLoading: Boolean = false,
        val friendsLoaded: Boolean = false,
        val friendsError: String? = null
    ) : ProfileUiState
    data class Error(
        val message: String,
        val details: String? = null
    ) : ProfileUiState
}
