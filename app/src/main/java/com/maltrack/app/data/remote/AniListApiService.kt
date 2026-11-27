package com.maltrack.app.data.remote

import com.maltrack.app.data.model.AniListRequest
import com.maltrack.app.data.model.AniListResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AniListApiService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun getAnimeDetails(@Body request: AniListRequest): AniListResponse
    
    companion object {
        const val BASE_URL = "https://graphql.anilist.co/"
    }
}
