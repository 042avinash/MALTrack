package com.example.myapplication.data.remote

import com.example.myapplication.data.model.JikanFullUserProfile
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApiService {
    @GET("random/anime")
    suspend fun getRandomAnime(): JikanRandomAnimeResponse

    @GET("anime/{id}/characters")
    suspend fun getAnimeCharacters(@Path("id") id: Int): JikanCharactersResponse

    @GET("anime/{id}/themes")
    suspend fun getAnimeThemes(@Path("id") id: Int): JikanThemesResponse

    @GET("anime/{id}/reviews")
    suspend fun getAnimeReviews(
        @Path("id") id: Int, 
        @Query("preliminary") preliminary: Boolean = true
    ): JikanReviewsResponse

    @GET("anime/{id}/streaming")
    suspend fun getAnimeStreaming(@Path("id") id: Int): JikanStreamingResponse

    @GET("users/{username}/full")
    suspend fun getUserFullProfile(@Path("username") username: String): JikanFullUserProfileResponse

    @GET("users/{username}/friends")
    suspend fun getUserFriends(
        @Path("username") username: String,
        @Query("page") page: Int = 1
    ): JikanFriendsResponse

    @GET("top/manga")
    suspend fun getTopManga(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1
    ): JikanMangaResponse

    companion object {
        const val BASE_URL = "https://api.jikan.moe/v4/"
    }
}

@Serializable
data class JikanFullUserProfileResponse(
    val data: JikanFullUserProfile
)

@Serializable
data class JikanFriendsResponse(
    val data: List<JikanFriend> = emptyList(),
    val pagination: JikanPagination? = null
)

@Serializable
data class JikanFriend(
    val user: JikanUser,
    val last_online: String? = null,
    val friends_since: String? = null
)

@Serializable
data class JikanPagination(
    val last_visible_page: Int,
    val has_next_page: Boolean
)

@Serializable
data class JikanCharactersResponse(
    val data: List<JikanCharacterData> = emptyList()
)

@Serializable
data class JikanCharacterData(
    val character: JikanCharacter,
    val role: String,
    val voice_actors: List<JikanVoiceActor> = emptyList()
)

@Serializable
data class JikanCharacter(
    val mal_id: Int,
    val url: String,
    val images: JikanImages,
    val name: String
)

@Serializable
data class JikanVoiceActor(
    val person: JikanPerson,
    val language: String
)

@Serializable
data class JikanPerson(
    val mal_id: Int,
    val url: String,
    val images: JikanImages,
    val name: String
)

@Serializable
data class JikanThemesResponse(
    val data: JikanThemesData? = null
)

@Serializable
data class JikanThemesData(
    val openings: List<String> = emptyList(),
    val endings: List<String> = emptyList()
)

@Serializable
data class JikanReviewsResponse(
    val data: List<JikanReviewData> = emptyList()
)

@Serializable
data class JikanReviewData(
    val mal_id: Int,
    val url: String,
    val type: String? = null,
    val score: Int,
    val date: String,
    val review: String,
    val tags: List<String> = emptyList(),
    val is_spoiler: Boolean = false,
    val is_preliminary: Boolean = false,
    val user: JikanUser
)

@Serializable
data class JikanUser(
    val username: String,
    val url: String,
    val images: JikanImages
)

@Serializable
data class JikanStreamingResponse(
    val data: List<JikanStreamingData> = emptyList()
)

@Serializable
data class JikanStreamingData(
    val name: String,
    val url: String
)

@Serializable
data class JikanImages(
    val jpg: JikanImageFormat? = null,
    val webp: JikanImageFormat? = null
)

@Serializable
data class JikanImageFormat(
    val image_url: String? = null,
    val small_image_url: String? = null,
    val large_image_url: String? = null
)

@Serializable
data class JikanMangaResponse(
    val data: List<JikanMangaData> = emptyList(),
    val pagination: JikanPagination? = null
)

@Serializable
data class JikanMangaData(
    val mal_id: Int,
    val title: String,
    val images: JikanImages,
    val members: Int? = null,
    val score: Float? = null,
    val synopsis: String? = null,
    val type: String? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val status: String? = null
)

@Serializable
data class JikanRandomAnimeResponse(
    val data: JikanRandomAnimeData
)

@Serializable
data class JikanRandomAnimeData(
    val mal_id: Int
)
