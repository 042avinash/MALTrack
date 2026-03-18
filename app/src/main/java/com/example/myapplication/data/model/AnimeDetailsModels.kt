package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeDetailsResponse(
    val id: Int,
    val title: String,
    @SerialName("main_picture") val mainPicture: MainPicture? = null,
    @SerialName("alternative_titles") val alternativeTitles: AlternativeTitles? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val synopsis: String? = null,
    val mean: Float? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    @SerialName("num_list_users") val numListUsers: Int? = null,
    @SerialName("num_scoring_users") val numScoringUsers: Int? = null,
    val nsfw: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    val status: String? = null,
    val genres: List<Genre>? = null,
    @SerialName("my_list_status") val myListStatus: MyListStatus? = null,
    @SerialName("num_episodes") val numEpisodes: Int? = null,
    @SerialName("start_season") val startSeason: StartSeason? = null,
    val broadcast: Broadcast? = null,
    val source: String? = null,
    @SerialName("average_episode_duration") val averageEpisodeDuration: Int? = null,
    val nsfw_rating: String? = null,
    val pictures: List<MainPicture>? = null,
    val background: String? = null,
    @SerialName("related_anime") val relatedAnime: List<RelatedAnime>? = null,
    @SerialName("related_manga") val relatedManga: List<RelatedManga>? = null,
    val recommendations: List<Recommendation>? = null,
    val studios: List<Studio>? = null,
    val statistics: Statistics? = null
)

@Serializable
data class Statistics(
    val status: StatusStatistics? = null,
    @SerialName("num_list_users") val numListUsers: Int? = null
)

@Serializable
data class StatusStatistics(
    val watching: String? = null,
    val completed: String? = null,
    @SerialName("on_hold") val onHold: String? = null,
    val dropped: String? = null,
    @SerialName("plan_to_watch") val planToWatch: String? = null
)

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class StartSeason(
    val year: Int,
    val season: String
)

@Serializable
data class Broadcast(
    @SerialName("day_of_the_week") val dayOfTheWeek: String,
    @SerialName("start_time") val startTime: String? = null
)

@Serializable
data class RelatedAnime(
    val node: AnimeNode,
    @SerialName("relation_type") val relationType: String,
    @SerialName("relation_type_formatted") val relationTypeFormatted: String
)

@Serializable
data class RelatedManga(
    val node: MangaNode,
    @SerialName("relation_type") val relationType: String,
    @SerialName("relation_type_formatted") val relationTypeFormatted: String
)

@Serializable
data class Recommendation(
    val node: AnimeNode,
    @SerialName("num_recommendations") val numRecommendations: Int
)

@Serializable
data class Studio(
    val id: Int,
    val name: String
)
