package com.example.myapplication.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.TokenManager
import com.example.myapplication.data.remote.AuthApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val clientId = "16b21f717a3e9f733f121971c122db16"
    private var codeVerifier: String = ""

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = tokenManager.accessToken.first()
            if (token != null) {
                _uiState.value = LoginUiState.Success
            }
        }
    }

    fun startLogin(context: Context) {
        codeVerifier = generateCodeVerifier()
        val codeChallenge = codeVerifier // MAL supports plain code_challenge if it's 128 chars

        val uri = Uri.parse("https://myanimelist.net/v1/oauth2/authorize")
            .buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("state", "mystate")
            .build()

        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    }

    fun handleAuthRedirect(code: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = authApiService.getAccessToken(
                    clientId = clientId,
                    code = code,
                    codeVerifier = codeVerifier
                )
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearTokens()
            _uiState.value = LoginUiState.Idle
        }
    }

    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(64)
        secureRandom.nextBytes(code)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code)
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}
