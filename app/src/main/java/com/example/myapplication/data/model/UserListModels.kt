package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAnimeListResponse(
    val data: List<UserAnimeData>,
    val paging: Paging
)

@Serializable
data class UserAnimeData(
    val node: AnimeNode,
    @SerialName("list_status") val listStatus: ListStatus
)

@Serializable
data class UserMangaListResponse(
    val data: List<UserMangaData>,
    val paging: Paging
)

@Serializable
data class UserMangaData(
    val node: MangaNode,
    @SerialName("list_status") val listStatus: MangaListStatus
)

@Serializable
data class ListStatus(
    val status: String,
    val score: Int,
    @SerialName("num_episodes_watched") val numEpisodesWatched: Int,
    @SerialName("is_rewatching") val isRewatching: Boolean,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class MangaListStatus(
    val status: String,
    val score: Int,
    @SerialName("num_volumes_read") val numVolumesRead: Int,
    @SerialName("num_chapters_read") val numChaptersRead: Int,
    @SerialName("is_rereading") val isRereading: Boolean,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class Paging(
    val next: String? = null
)
