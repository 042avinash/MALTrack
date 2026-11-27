package com.maltrack.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TenraiPagination(
    val last_visible_page: Int,
    val has_next_page: Boolean
)

@Serializable
data class TenraiCharactersResponse(
    val data: List<TenraiCharacterData> = emptyList()
)

@Serializable
data class TenraiCharacterData(
    val character: TenraiCharacter,
    val role: String,
    val voice_actors: List<TenraiVoiceActor> = emptyList()
)

@Serializable
data class TenraiStaffResponse(
    val data: List<TenraiStaffData> = emptyList()
)

@Serializable
data class TenraiStaffData(
    val person: TenraiPerson,
    val positions: List<String> = emptyList()
)

@Serializable
data class TenraiCharacter(
    val mal_id: Int,
    val url: String,
    val images: TenraiImages,
    val name: String
)

@Serializable
data class TenraiVoiceActor(
    val person: TenraiPerson,
    val language: String
)

@Serializable
data class TenraiPerson(
    val mal_id: Int,
    val url: String,
    val images: TenraiImages,
    val name: String
)

@Serializable
data class TenraiThemesResponse(
    val data: TenraiThemesData? = null
)

@Serializable
data class TenraiThemesData(
    val openings: List<String> = emptyList(),
    val endings: List<String> = emptyList()
)

@Serializable
data class TenraiReviewsResponse(
    val data: List<TenraiReviewData> = emptyList()
)

@Serializable
data class TenraiReviewData(
    val mal_id: Int,
    val url: String,
    val type: String? = null,
    val score: Int,
    val date: String,
    val review: String,
    val tags: List<String> = emptyList(),
    val is_spoiler: Boolean = false,
    val is_preliminary: Boolean = false,
    val user: TenraiUser
)

@Serializable
data class TenraiUser(
    val username: String,
    val url: String,
    val images: TenraiImages
)

@Serializable
data class TenraiStreamingResponse(
    val data: List<TenraiStreamingData> = emptyList()
)

@Serializable
data class TenraiStreamingData(
    val name: String,
    val url: String
)

@Serializable
data class TenraiAnimeStatisticsResponse(
    val data: TenraiAnimeStatisticsData
)

@Serializable
data class TenraiAnimeStatisticsData(
    val scores: List<TenraiAnimeScoreBucket> = emptyList()
)

@Serializable
data class TenraiMangaStatisticsResponse(
    val data: TenraiMangaStatisticsData
)

@Serializable
data class TenraiMangaStatisticsData(
    val reading: Int? = null,
    val completed: Int? = null,
    val on_hold: Int? = null,
    val dropped: Int? = null,
    val plan_to_read: Int? = null,
    val total: Int? = null,
    val scores: List<TenraiAnimeScoreBucket> = emptyList()
)

@Serializable
data class TenraiAnimeScoreBucket(
    val score: Int,
    val votes: Int,
    val percentage: Double
)

@Serializable
data class TenraiImages(
    val jpg: TenraiImageFormat? = null,
    val webp: TenraiImageFormat? = null
)

@Serializable
data class TenraiImageFormat(
    val image_url: String? = null,
    val small_image_url: String? = null,
    val large_image_url: String? = null
)

@Serializable
data class TenraiMangaResponse(
    val data: List<TenraiMangaData> = emptyList(),
    val pagination: TenraiPagination? = null
)

@Serializable
data class TenraiMangaData(
    val mal_id: Int,
    val title: String,
    val images: TenraiImages,
    val members: Int? = null,
    val score: Float? = null,
    val synopsis: String? = null,
    val type: String? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val status: String? = null
)

@Serializable
data class TenraiRandomAnimeResponse(
    val data: TenraiRandomAnimeData
)

@Serializable
data class TenraiRandomAnimeData(
    val mal_id: Int
)
