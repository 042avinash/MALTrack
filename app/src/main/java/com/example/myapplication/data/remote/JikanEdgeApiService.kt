package com.example.myapplication.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface JikanEdgeApiService {
    @GET("users/{username}/favorites")
    suspend fun getUserFavorites(@Path("username") username: String): JikanEdgeFavoritesResponse

    companion object {
        const val BASE_URL = "https://jikan.lucashdo.com/v1/"
    }
}

@Serializable
data class JikanEdgeFavoritesResponse(
    val data: JikanEdgeFavoritesData
)

@Serializable
data class JikanEdgeFavoritesData(
    val anime: List<JikanEdgeFavoriteItem> = emptyList(),
    val manga: List<JikanEdgeFavoriteItem> = emptyList()
)

@Serializable
data class JikanEdgeFavoriteItem(
    val malId: Int,
    val title: String? = null,
    val name: String? = null,
    val url: String? = null,
    val imageUrl: String? = null
)
