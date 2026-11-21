package com.example.myapplication.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Jikan-compatible public catalogue endpoints hosted by Tenrai. */
interface TenraiApiService {
    @GET("random/anime")
    suspend fun getRandomAnime(): JikanRandomAnimeResponse

    @GET("anime/{id}/characters")
    suspend fun getAnimeCharacters(@Path("id") id: Int): JikanCharactersResponse

    @GET("manga/{id}/characters")
    suspend fun getMangaCharacters(@Path("id") id: Int): JikanCharactersResponse

    @GET("anime/{id}/staff")
    suspend fun getAnimeStaff(@Path("id") id: Int): JikanStaffResponse

    @GET("anime/{id}/themes")
    suspend fun getAnimeThemes(@Path("id") id: Int): JikanThemesResponse

    @GET("anime/{id}/reviews")
    suspend fun getAnimeReviews(
        @Path("id") id: Int,
        @Query("preliminary") preliminary: Boolean = true
    ): JikanReviewsResponse

    @GET("manga/{id}/reviews")
    suspend fun getMangaReviews(
        @Path("id") id: Int,
        @Query("preliminary") preliminary: Boolean = true
    ): JikanReviewsResponse

    @GET("anime/{id}/streaming")
    suspend fun getAnimeStreaming(@Path("id") id: Int): JikanStreamingResponse

    @GET("anime/{id}/statistics")
    suspend fun getAnimeStatistics(@Path("id") id: Int): JikanAnimeStatisticsResponse

    @GET("manga/{id}/statistics")
    suspend fun getMangaStatistics(@Path("id") id: Int): JikanMangaStatisticsResponse

    @GET("top/manga")
    suspend fun getTopManga(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1
    ): JikanMangaResponse

    companion object {
        const val BASE_URL = "https://api.tenrai.org/v1/"
    }
}
