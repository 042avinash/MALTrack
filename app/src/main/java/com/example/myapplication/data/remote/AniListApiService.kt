package com.example.myapplication.data.remote

import com.example.myapplication.data.model.AniListRequest
import com.example.myapplication.data.model.AniListResponse
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
