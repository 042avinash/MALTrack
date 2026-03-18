package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: Int,
    val name: String,
    @SerialName("picture") val picture: String? = null,
    val gender: String? = null,
    val birthday: String? = null,
    val location: String? = null,
    @SerialName("anime_statistics") val animeStatistics: AnimeStatistics? = null,
    @SerialName("manga_statistics") val mangaStatistics: MangaStatistics? = null
)

@Serializable
data class AnimeStatistics(
    @SerialName("num_items_watching") val numWatching: Int = 0,
    @SerialName("num_items_completed") val numCompleted: Int = 0,
    @SerialName("num_items_on_hold") val numOnHold: Int = 0,
    @SerialName("num_items_dropped") val numDropped: Int = 0,
    @SerialName("num_items_plan_to_watch") val numPlanToWatch: Int = 0,
    @SerialName("num_items") val numItems: Int = 0,
    @SerialName("num_days_watched") val numDaysWatched: Float = 0f,
    @SerialName("num_days_watching") val numDaysWatching: Float = 0f,
    @SerialName("num_days_completed") val numDaysCompleted: Float = 0f,
    @SerialName("num_days_on_hold") val numDaysOnHold: Float = 0f,
    @SerialName("num_days_dropped") val numDaysDropped: Float = 0f,
    @SerialName("num_days") val numDays: Float = 0f,
    @SerialName("num_episodes") val numEpisodes: Int = 0,
    @SerialName("num_times_rewatched") val numTimesRewatched: Int = 0,
    @SerialName("mean_score") val meanScore: Float = 0f
)

@Serializable
data class MangaStatistics(
    @SerialName("num_items_reading") val numReading: Int = 0,
    @SerialName("num_items_completed") val numCompleted: Int = 0,
    @SerialName("num_items_on_hold") val numOnHold: Int = 0,
    @SerialName("num_items_dropped") val numDropped: Int = 0,
    @SerialName("num_items_plan_to_read") val numPlanToRead: Int = 0,
    @SerialName("num_items") val numItems: Int = 0,
    @SerialName("num_days_read") val numDaysRead: Float = 0f,
    @SerialName("num_days_reading") val numDaysReading: Float = 0f,
    @SerialName("num_days_completed") val numDaysCompleted: Float = 0f,
    @SerialName("num_days_on_hold") val numDaysOnHold: Float = 0f,
    @SerialName("num_days_dropped") val numDaysDropped: Float = 0f,
    @SerialName("num_days") val numDays: Float = 0f,
    @SerialName("num_chapters") val numChapters: Int = 0,
    @SerialName("num_volumes") val numVolumes: Int = 0,
    @SerialName("num_times_reread") val numTimesReread: Int = 0,
    @SerialName("mean_score") val meanScore: Float = 0f
)

@Serializable
data class JikanFullUserProfile(
    val mal_id: Int? = null,
    val username: String,
    val url: String? = null,
    val images: JikanUserImages? = null,
    val last_online: String? = null,
    val gender: String? = null,
    val birthday: String? = null,
    val location: String? = null,
    val joined: String? = null,
    val statistics: JikanUserStatistics? = null,
    val favorites: JikanUserFavorites? = null,
    val about: String? = null,
    val external: List<JikanExternalLink>? = null
)

@Serializable
data class JikanUserImages(
    val jpg: JikanImageFormat? = null,
    val webp: JikanImageFormat? = null
)

@Serializable
data class JikanImageFormat(
    val image_url: String? = null
)

@Serializable
data class JikanUserStatistics(
    val anime: JikanAnimeStats? = null,
    val manga: JikanMangaStats? = null
)

@Serializable
data class JikanAnimeStats(
    val days_watched: Float = 0f,
    val mean_score: Float = 0f,
    val watching: Int = 0,
    val completed: Int = 0,
    val on_hold: Int = 0,
    val dropped: Int = 0,
    val plan_to_watch: Int = 0,
    val total_entries: Int = 0,
    val episodes_watched: Int = 0,
    val rewatched: Int = 0
)

@Serializable
data class JikanMangaStats(
    val days_read: Float = 0f,
    val mean_score: Float = 0f,
    val reading: Int = 0,
    val completed: Int = 0,
    val on_hold: Int = 0,
    val dropped: Int = 0,
    val plan_to_read: Int = 0,
    val total_entries: Int = 0,
    val chapters_read: Int = 0,
    val volumes_read: Int = 0,
    val reread: Int = 0
)

@Serializable
data class JikanUserFavorites(
    val anime: List<JikanFavoriteItem>? = null,
    val manga: List<JikanFavoriteItem>? = null,
    val characters: List<JikanFavoriteItem>? = null,
    val people: List<JikanFavoriteItem>? = null,
    val studios: List<JikanFavoriteItem>? = null
)

@Serializable
data class JikanFavoriteItem(
    val mal_id: Int,
    val type: String? = null,
    val name: String? = null,
    val title: String? = null,
    val url: String? = null,
    val images: JikanUserImages? = null
)

@Serializable
data class JikanExternalLink(
    val name: String? = null,
    val url: String? = null
)
