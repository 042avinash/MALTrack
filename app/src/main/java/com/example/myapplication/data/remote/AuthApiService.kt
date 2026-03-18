package com.example.myapplication.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService {
    @FormUrlEncoded
    @POST("v1/oauth2/token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenResponse

    companion object {
        const val BASE_URL = "https://myanimelist.net/"
    }
}

@Serializable
data class TokenResponse(
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)
