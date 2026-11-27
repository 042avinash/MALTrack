package com.maltrack.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

/** User favorites only; the rest of the app continues to use its existing data sources. */
interface JikanEdgeApiService {
    @GET("v1/users/{username}/favorites")
    suspend fun getUserFavorites(@Path("username") username: String): JikanEdgeFavoritesResponse

    companion object {
        const val BASE_URL = "https://jikan.lucashdo.com/"
    }
}

@Serializable
data class JikanEdgeFavoritesResponse(
    val data: JikanEdgeFavoriteData
)

@Serializable
data class JikanEdgeFavoriteData(
    val anime: List<JikanEdgeFavoriteMedia> = emptyList(),
    val manga: List<JikanEdgeFavoriteMedia> = emptyList()
)

@Serializable
data class JikanEdgeFavoriteMedia(
    val malId: Int,
    val title: String? = null,
    val name: String? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    val type: String? = null,
    val startYear: Int? = null
) {
    val displayTitle: String
        get() = title?.takeIf { it.isNotBlank() } ?: name.orEmpty()
}
