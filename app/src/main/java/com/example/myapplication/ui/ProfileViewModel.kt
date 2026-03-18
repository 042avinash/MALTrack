package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.JikanFullUserProfile
import com.example.myapplication.data.model.UserProfile
import com.example.myapplication.data.remote.JikanFriend
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.os.SystemClock
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private var loadJob: Job? = null
    private var lastLoadedUsername: String? = null
    private var lastLoadedAtMs: Long = 0L
    private val cachedProfiles = mutableMapOf<String, ProfileUiState.Success>()

    fun getProfile(username: String? = null) {
        val targetUsername = if (username == "null") null else username
        val normalizedUsername = targetUsername ?: "__self__"
        val now = SystemClock.elapsedRealtime()
        val cachedState = cachedProfiles[normalizedUsername]

        if (_uiState.value is ProfileUiState.Loading && lastLoadedUsername == normalizedUsername) return
        if (_uiState.value is ProfileUiState.Success &&
            lastLoadedUsername == normalizedUsername &&
            now - lastLoadedAtMs < 60_000L
        ) return
        if (cachedState != null && now - lastLoadedAtMs < 60_000L && lastLoadedUsername == normalizedUsername) {
            _uiState.value = cachedState
            return
        }
        
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (cachedState == null) {
                _uiState.value = ProfileUiState.Loading
            }
            try {
                if (targetUsername == null) {
                    val malProfile = repository.getMyUserProfile()
                    val fullProfile = repository.getUserFullProfile(malProfile.name)
                    val friends = try { 
                        repository.getUserFriends(malProfile.name) 
                    } catch (_: Exception) { 
                        emptyList<JikanFriend>() 
                    }
                    val successState = ProfileUiState.Success(malProfile, fullProfile, friends, isOwnProfile = true)
                    lastLoadedUsername = normalizedUsername
                    lastLoadedAtMs = SystemClock.elapsedRealtime()
                    cachedProfiles[normalizedUsername] = successState
                    _uiState.value = successState
                } else {
                    val fullProfile = repository.getUserFullProfile(targetUsername)
                    val friends = try { 
                        repository.getUserFriends(targetUsername) 
                    } catch (_: Exception) { 
                        emptyList<JikanFriend>() 
                    }
                    val successState = ProfileUiState.Success(null, fullProfile, friends, isOwnProfile = false)
                    lastLoadedUsername = normalizedUsername
                    lastLoadedAtMs = SystemClock.elapsedRealtime()
                    cachedProfiles[normalizedUsername] = successState
                    _uiState.value = successState
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
                _uiState.value = cachedState ?: ProfileUiState.Error(e.message ?: "Failed to load profile. Please check if the username is correct.")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun getFriendlyErrorMessage(error: Throwable): String {
        return if (error is SocketTimeoutException) {
            "Request timed out. MAL or Jikan may be responding slowly, too many requests may have fired close together, a request may have stalled while switching screens, or your network may be unstable."
        } else {
            error.message ?: "Something went wrong."
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val malUser: UserProfile?, 
        val jikanUser: JikanFullUserProfile,
        val friends: List<JikanFriend>,
        val isOwnProfile: Boolean
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
