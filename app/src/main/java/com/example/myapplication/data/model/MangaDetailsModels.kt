package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaDetailsResponse(
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
    @SerialName("my_list_status") val myListStatus: MyMangaListStatus? = null,
    @SerialName("num_volumes") val numVolumes: Int? = null,
    @SerialName("num_chapters") val numChapters: Int? = null,
    val authors: List<AuthorRole>? = null,
    val pictures: List<MainPicture>? = null,
    val background: String? = null,
    @SerialName("related_anime") val relatedAnime: List<RelatedAnime>? = null,
    @SerialName("related_manga") val relatedManga: List<RelatedManga>? = null,
    val recommendations: List<MangaRecommendation>? = null,
    val serialization: List<Serialization>? = null,
    val statistics: Statistics? = null
)

@Serializable
data class AuthorRole(
    val node: Author,
    val role: String
)

@Serializable
data class Author(
    val id: Int,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String
)

@Serializable
data class MangaRecommendation(
    val node: MangaNode,
    @SerialName("num_recommendations") val numRecommendations: Int
)

@Serializable
data class Serialization(
    val node: SerializationNode
)

@Serializable
data class SerializationNode(
    val id: Int,
    val name: String
)
