package com.example.myapplication.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Anime and manga catalogue endpoints hosted by Tenrai. */
interface TenraiApiService {
    @GET("random/anime")
    suspend fun getRandomAnime(): TenraiRandomAnimeResponse

    @GET("anime/{id}/characters")
    suspend fun getAnimeCharacters(@Path("id") id: Int): TenraiCharactersResponse

    @GET("manga/{id}/characters")
    suspend fun getMangaCharacters(@Path("id") id: Int): TenraiCharactersResponse

    @GET("anime/{id}/staff")
    suspend fun getAnimeStaff(@Path("id") id: Int): TenraiStaffResponse

    @GET("anime/{id}/themes")
    suspend fun getAnimeThemes(@Path("id") id: Int): TenraiThemesResponse

    @GET("anime/{id}/reviews")
    suspend fun getAnimeReviews(
        @Path("id") id: Int,
        @Query("preliminary") preliminary: Boolean = true
    ): TenraiReviewsResponse

    @GET("manga/{id}/reviews")
    suspend fun getMangaReviews(
        @Path("id") id: Int,
        @Query("preliminary") preliminary: Boolean = true
    ): TenraiReviewsResponse

    @GET("anime/{id}/streaming")
    suspend fun getAnimeStreaming(@Path("id") id: Int): TenraiStreamingResponse

    @GET("anime/{id}/statistics")
    suspend fun getAnimeStatistics(@Path("id") id: Int): TenraiAnimeStatisticsResponse

    @GET("manga/{id}/statistics")
    suspend fun getMangaStatistics(@Path("id") id: Int): TenraiMangaStatisticsResponse

    @GET("top/manga")
    suspend fun getTopManga(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1
    ): TenraiMangaResponse

    companion object {
        const val BASE_URL = "https://api.tenrai.org/v1/"
    }
}
