package com.example.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

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
    @Serializable(with = IntOrStringSerializer::class) val watching: Int? = null,
    @Serializable(with = IntOrStringSerializer::class) val completed: Int? = null,
    @SerialName("on_hold") @Serializable(with = IntOrStringSerializer::class) val onHold: Int? = null,
    @Serializable(with = IntOrStringSerializer::class) val dropped: Int? = null,
    @SerialName("plan_to_watch") @Serializable(with = IntOrStringSerializer::class) val planToWatch: Int? = null
)

object IntOrStringSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntOrStringSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeInt() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()

        return when (element) {
            JsonNull -> null
            is JsonPrimitive -> {
                element.content.toIntOrNull()
            }
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeInt(value)
        }
    }
}

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
