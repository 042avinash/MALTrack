package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeResponse(
    val data: List<AnimeData>
)

@Serializable
data class AnimeData(
    val node: AnimeNode
)

@Serializable
data class AnimeNode(
    val id: Int,
    val title: String,
    @SerialName("main_picture") val mainPicture: MainPicture? = null,
    val synopsis: String? = null,
    @SerialName("mean") val meanScore: Float? = null,
    @SerialName("num_list_users") val numListUsers: Int? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("num_episodes") val numEpisodes: Int? = null,
    val status: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("alternative_titles") val alternativeTitles: AlternativeTitles? = null,
    @SerialName("my_list_status") val myListStatus: MyListStatus? = null
)

@Serializable
data class MyListStatus(
    val status: String? = null,
    val score: Int = 0,
    @SerialName("num_episodes_watched") val numEpisodesWatched: Int = 0,
    @SerialName("is_rewatching") val isRewatching: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
    val priority: Int = 0,
    @SerialName("num_times_rewatched") val numTimesRewatched: Int = 0,
    @SerialName("rewatch_value") val rewatchValue: Int = 0,
    val tags: List<String> = emptyList(),
    val comments: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("finish_date") val finishDate: String? = null
)

@Serializable
data class AlternativeTitles(
    val synonyms: List<String>? = null,
    val en: String? = null,
    val ja: String? = null
)

@Serializable
data class MainPicture(
    val medium: String,
    val large: String
)
