package com.example.myapplication.data.remote

import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.model.AnimeResponse
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.model.MangaResponse
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.MyMangaListStatus
import com.example.myapplication.data.model.UserAnimeListResponse
import com.example.myapplication.data.model.UserMangaListResponse
import com.example.myapplication.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MalApiService {
    @GET("anime/ranking")
    suspend fun getAnimeRanking(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("ranking_type") rankingType: String = "all",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): AnimeResponse

    @GET("anime/season/{year}/{season}")
    suspend fun getSeasonalAnime(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("year") year: Int,
        @Path("season") season: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): AnimeResponse

    @GET("manga/ranking")
    suspend fun getMangaRanking(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("ranking_type") rankingType: String = "all",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): MangaResponse

    @GET("anime")
    suspend fun searchAnime(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): AnimeResponse

    @GET("manga")
    suspend fun searchManga(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): MangaResponse

    @GET("anime/suggestions")
    suspend fun getAnimeSuggestions(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): AnimeResponse

    @GET("manga/suggestions")
    suspend fun getMangaSuggestions(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("limit") limit: Int = 5,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): MangaResponse

    @GET("anime/{anime_id}")
    suspend fun getAnimeDetails(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("anime_id") animeId: Int,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_episodes,start_season,broadcast,source,average_episode_duration,rating,pictures,background,related_anime,related_manga,recommendations,studios,statistics"
    ): AnimeDetailsResponse

    @GET("anime/{anime_id}")
    suspend fun getAnimeDetailsLite(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("anime_id") animeId: Int,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_episodes,start_season,broadcast,source,average_episode_duration,rating,pictures,background,related_anime,related_manga,studios,statistics"
    ): AnimeDetailsResponse

    @GET("anime/{anime_id}")
    suspend fun getAnimeRecommendationsOnly(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("anime_id") animeId: Int,
        @Query("fields") fields: String = "id,title,recommendations"
    ): AnimeDetailsResponse

    @GET("manga/{manga_id}")
    suspend fun getMangaDetails(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("manga_id") mangaId: Int,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_volumes,num_chapters,authors{first_name,last_name},pictures,background,related_anime,related_manga,recommendations,serialization{name},statistics"
    ): MangaDetailsResponse

    @GET("manga/{manga_id}")
    suspend fun getMangaDetailsLite(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("manga_id") mangaId: Int,
        @Query("fields") fields: String = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_volumes,num_chapters,authors{first_name,last_name},pictures,background,related_anime,related_manga,serialization{name},statistics"
    ): MangaDetailsResponse

    @GET("manga/{manga_id}")
    suspend fun getMangaRecommendationsOnly(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("manga_id") mangaId: Int,
        @Query("fields") fields: String = "id,title,recommendations"
    ): MangaDetailsResponse

    @FormUrlEncoded
    @PATCH("anime/{anime_id}/my_list_status")
    suspend fun updateMyListStatus(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("anime_id") animeId: Int,
        @Field("status") status: String? = null,
        @Field("is_rewatching") isRewatching: Boolean? = null,
        @Field("score") score: Int? = null,
        @Field("num_watched_episodes") numWatchedEpisodes: Int? = null,
        @Field("priority") priority: Int? = null,
        @Field("num_times_rewatched") numTimesRewatched: Int? = null,
        @Field("rewatch_value") rewatchValue: Int? = null,
        @Field("tags") tags: String? = null,
        @Field("comments") comments: String? = null,
        @Field("start_date") startDate: String? = null,
        @Field("finish_date") finishDate: String? = null
    ): MyListStatus

    @FormUrlEncoded
    @PATCH("manga/{manga_id}/my_list_status")
    suspend fun updateMyMangaListStatus(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("manga_id") mangaId: Int,
        @Field("status") status: String? = null,
        @Field("is_rereading") isRereading: Boolean? = null,
        @Field("score") score: Int? = null,
        @Field("num_volumes_read") numVolumesRead: Int? = null,
        @Field("num_chapters_read") numChaptersRead: Int? = null,
        @Field("priority") priority: Int? = null,
        @Field("num_times_reread") numTimesReread: Int? = null,
        @Field("reread_value") rereadValue: Int? = null,
        @Field("tags") tags: String? = null,
        @Field("comments") comments: String? = null,
        @Field("start_date") startDate: String? = null,
        @Field("finish_date") finishDate: String? = null
    ): MyMangaListStatus

    @DELETE("anime/{anime_id}/my_list_status")
    suspend fun deleteMyListStatus(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("anime_id") animeId: Int
    ): Response<Unit>

    @DELETE("manga/{manga_id}/my_list_status")
    suspend fun deleteMyMangaListStatus(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("manga_id") mangaId: Int
    ): Response<Unit>

    @GET("users/@me")
    suspend fun getMyUserProfile(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("fields") fields: String = "id,name,picture,gender,birthday,location,anime_statistics,manga_statistics"
    ): UserProfile

    @GET("users/{username}/animelist")
    suspend fun getUserAnimeList(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("username") username: String = "@me",
        @Query("status") status: String? = null,
        @Query("sort") sort: String = "list_score",
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "list_status,main_picture,num_episodes,status,start_date,mean,alternative_titles,num_list_users,media_type"
    ): UserAnimeListResponse

    @GET("users/{username}/mangalist")
    suspend fun getUserMangaList(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Path("username") username: String = "@me",
        @Query("status") status: String? = null,
        @Query("sort") sort: String = "list_score",
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "list_status,main_picture,num_volumes,num_chapters,status,start_date,mean,alternative_titles,num_list_users,media_type"
    ): UserMangaListResponse

    @GET("anime/ranking")
    suspend fun getAnimeRankingWithLimit(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("ranking_type") rankingType: String = "all",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): AnimeResponse

    @GET("manga/ranking")
    suspend fun getMangaRankingWithLimit(
        @Header("X-MAL-CLIENT-ID") clientId: String,
        @Query("ranking_type") rankingType: String = "all",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("nsfw") nsfw: Boolean = false,
        @Query("fields") fields: String = "id,title,main_picture,mean,num_list_users,synopsis,media_type,alternative_titles,my_list_status"
    ): MangaResponse

    companion object {
        const val BASE_URL = "https://api.myanimelist.net/v2/"
    }
}

