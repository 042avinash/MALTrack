package com.example.myapplication.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimeScheduleApiService {
    @GET("anime")
    suspend fun getAnimeByMalIds(
        @Query("mal-ids") malIds: List<Int>,
        // Multiple MAL IDs must use the API's "any" match mode. Its default
        // "all" mode makes a batch impossible to match, while a detail lookup
        // with one ID appears to work.
        @Query("mt") matchType: String = "any"
    ): AnimeScheduleResponse

    @GET("timetables/raw")
    suspend fun getRawTimetable(
        @Query("tz") timezone: String = "UTC"
    ): List<AnimeScheduleTimetableEntry>

    companion object {
        const val BASE_URL = "https://animeschedule.net/api/v3/"
    }
}

@Serializable
data class AnimeScheduleResponse(val anime: List<AnimeScheduleAnime> = emptyList())

@Serializable
data class AnimeScheduleAnime(
    val route: String? = null,
    val status: String? = null,
    val jpnTime: String? = null,
    val delayedUntil: String? = null,
    val episodeDate: String? = null,
    val episodeNumber: Int? = null,
    val airingStatus: String? = null,
    val websites: AnimeScheduleWebsites? = null
)

@Serializable
data class AnimeScheduleWebsites(val mal: String? = null)

@Serializable
data class AnimeScheduleTimetableEntry(
    val route: String,
    val episodeDate: String,
    val episodeNumber: Int,
    val airingStatus: String? = null
)
