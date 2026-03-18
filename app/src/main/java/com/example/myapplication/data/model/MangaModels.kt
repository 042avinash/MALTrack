package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaResponse(
    val data: List<MangaData>
)

@Serializable
data class MangaData(
    val node: MangaNode
)

@Serializable
data class MangaNode(
    val id: Int,
    val title: String,
    @SerialName("main_picture") val mainPicture: MainPicture? = null,
    val synopsis: String? = null,
    @SerialName("mean") val meanScore: Float? = null,
    @SerialName("num_list_users") val numListUsers: Int? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("num_volumes") val numVolumes: Int? = null,
    @SerialName("num_chapters") val numChapters: Int? = null,
    val status: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("alternative_titles") val alternativeTitles: AlternativeTitles? = null,
    @SerialName("my_list_status") val myListStatus: MyMangaListStatus? = null
)

@Serializable
data class MyMangaListStatus(
    val status: String? = null,
    val score: Int = 0,
    @SerialName("num_volumes_read") val numVolumesRead: Int = 0,
    @SerialName("num_chapters_read") val numChaptersRead: Int = 0,
    @SerialName("is_rereading") val isRereading: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
    val priority: Int = 0,
    @SerialName("num_times_reread") val numTimesReread: Int = 0,
    @SerialName("reread_value") val rereadValue: Int = 0,
    val tags: List<String> = emptyList(),
    val comments: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("finish_date") val finishDate: String? = null
)
