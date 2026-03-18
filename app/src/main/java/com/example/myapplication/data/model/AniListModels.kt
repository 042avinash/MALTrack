package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AniListRequest(
    val query: String,
    val variables: AniListVariables
)

@Serializable
data class AniListVariables(
    val malIds: List<Int>
)

@Serializable
data class AniListResponse(
    val data: AniListData? = null
)

@Serializable
data class AniListData(
    val Page: AniListPage? = null
)

@Serializable
data class AniListPage(
    val media: List<AniListMedia> = emptyList()
)

@Serializable
data class AniListMedia(
    val idMal: Int? = null,
    val nextAiringEpisode: NextAiringEpisode? = null
)

@Serializable
data class NextAiringEpisode(
    val airingAt: Long,
    val timeUntilAiring: Long,
    val episode: Int
)
