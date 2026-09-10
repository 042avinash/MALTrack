package com.example.myapplication.data.repository

import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.model.AnimeMyListStatusResponse
import com.example.myapplication.data.model.AnimeResponse
import com.example.myapplication.data.model.JikanFullUserProfile
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.model.MangaData
import com.example.myapplication.data.model.MangaResponse
import com.example.myapplication.data.model.MangaStatistics
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.MyMangaListStatus
import com.example.myapplication.data.model.UserAnimeData
import com.example.myapplication.data.model.UserAnimeListResponse
import com.example.myapplication.data.model.UserMangaData
import com.example.myapplication.data.model.UserMangaListResponse
import com.example.myapplication.data.model.UserProfile
import com.example.myapplication.data.remote.AnimeScheduleApiService
import com.example.myapplication.data.remote.AnimeScheduleTimetableEntry
import com.example.myapplication.data.remote.JikanApiService
import com.example.myapplication.data.remote.JikanCharactersResponse
import com.example.myapplication.data.remote.JikanFriend
import com.example.myapplication.data.remote.JikanMangaData
import com.example.myapplication.data.remote.JikanMangaStatisticsResponse
import com.example.myapplication.data.remote.JikanReviewsResponse
import com.example.myapplication.data.remote.JikanAnimeStatisticsResponse
import com.example.myapplication.data.remote.JikanStaffResponse
import com.example.myapplication.data.remote.JikanStreamingResponse
import com.example.myapplication.data.remote.JikanThemesResponse
import com.example.myapplication.data.remote.MalApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.os.SystemClock
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

class JikanProfileFetchException(
    val status: Int?,
    val responseMessage: String?,
    val responseError: String?,
    val username: String
) : IOException(
    buildString {
        append("Profile fetch failed for ")
        append(username)
        append(" (status=")
        append(status?.toString() ?: "unknown")
        append(")")
        responseMessage?.takeIf { it.isNotBlank() }?.let {
            append(": ")
            append(it)
        }
        responseError?.takeIf { it.isNotBlank() && it != responseMessage }?.let {
            append(" | error=")
            append(it)
        }
    }
)

@Singleton
class AnimeRepository @Inject constructor(
    private val apiService: MalApiService,
    private val animeScheduleApiService: AnimeScheduleApiService,
    private val jikanApiService: JikanApiService,
    private val prefsManager: UserPreferencesManager
) {
    companion object {
        private const val AIRING_SCHEDULE_BATCH_SIZE = 18
        private const val AIRING_SCHEDULE_CACHE_TTL_MS = 15 * 60 * 1000L
        private const val JIKAN_MIN_REQUEST_INTERVAL_MS = 350L
        private const val JIKAN_INITIAL_BACKOFF_MS = 1_500L
        private const val JIKAN_MAX_BACKOFF_MS = 15_000L
    }

    private val clientId = "16b21f717a3e9f733f121971c122db16"
    private val seasonalAnimeCache = mutableMapOf<String, AnimeResponse>()
    private val publishingMangaCache = mutableMapOf<String, List<JikanMangaData>>()
    private val airingScheduleCache = mutableMapOf<Int, Pair<Long, AniListMedia>>()
    private val airingScheduleCacheLock = Any()
    private var timetableCache: Pair<Long, Map<String, AnimeScheduleTimetableEntry>>? = null
    private val jikanRequestMutex = Mutex()
    private var lastJikanRequestAtMs = 0L
    private var jikanBackoffUntilMs = 0L

    private suspend fun isNsfw(): Boolean {
        return prefsManager.nsfwFlow.first()
    }

    suspend fun getTopAnime(limit: Int = 20, rankingType: String = "all"): AnimeResponse {
        return if (limit == 20 && rankingType == "all") apiService.getAnimeRanking(clientId = clientId, nsfw = isNsfw())
        else apiService.getAnimeRankingWithLimit(clientId = clientId, limit = limit, rankingType = rankingType, nsfw = isNsfw())
    }

    suspend fun getRandomAnimeId(): Int {
        return withJikanThrottle {
            retryTimeoutRequest {
                jikanApiService.getRandomAnime().data.mal_id
            }
        }
    }

    suspend fun getSeasonalAnime(
        year: Int? = null,
        season: String? = null,
        loadAllPages: Boolean = false,
        forceRefresh: Boolean = false
    ): AnimeResponse {
        val targetYear: Int
        val targetSeason: String

        if (year != null && season != null) {
            targetYear = year
            targetSeason = season
        } else {
            val calendar = Calendar.getInstance()
            targetYear = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            targetSeason = when (month) {
                in 0..2 -> "winter"
                in 3..5 -> "spring"
                in 6..8 -> "summer"
                else -> "fall"
            }
        }
        val nsfwEnabled = isNsfw()
        val cacheKey = listOf(targetYear, targetSeason, nsfwEnabled, loadAllPages).joinToString("|")
        if (forceRefresh) {
            seasonalAnimeCache.remove(cacheKey)
        } else {
            seasonalAnimeCache[cacheKey]?.let { return it }
        }

        val limit = 100
        val allItems = mutableListOf<com.example.myapplication.data.model.AnimeData>()
        var offset = 0

        while (true) {
            val response = try {
                retryTimeoutRequest {
                    apiService.getSeasonalAnime(
                        clientId = clientId,
                        year = targetYear,
                        season = targetSeason,
                        limit = limit,
                        offset = offset,
                        nsfw = nsfwEnabled
                    )
                }
            } catch (e: HttpException) {
                if (e.code() == 429 && allItems.isNotEmpty()) break
                throw e
            } catch (e: Throwable) {
                if (isTimeoutError(e) && allItems.isNotEmpty()) break
                throw e
            }
            if (response.data.isEmpty()) break

            allItems.addAll(response.data)

            if (!loadAllPages || response.data.size < limit) break
            offset += response.data.size
            delay(250)
        }

        return AnimeResponse(data = allItems).also {
            seasonalAnimeCache[cacheKey] = it
        }
    }

    private suspend fun <T> retryTimeoutRequest(block: suspend () -> T): T {
        var lastError: Throwable? = null
        val delays = listOf(0L, 350L, 900L)
        repeat(delays.size) { attempt ->
            try {
                if (delays[attempt] > 0) delay(delays[attempt])
                return block()
            } catch (e: Throwable) {
                lastError = e
                if (!isTimeoutError(e) || attempt == delays.lastIndex) throw e
            }
        }
        throw lastError ?: IllegalStateException("Unknown timeout error")
    }

    private fun isTimeoutError(error: Throwable): Boolean {
        if (error is SocketTimeoutException) return true
        if (error is IOException && error.message?.contains("timeout", ignoreCase = true) == true) return true
        val cause = error.cause
        return cause != null && isTimeoutError(cause)
    }

    private suspend fun <T> withJikanThrottle(block: suspend () -> T): T {
        return jikanRequestMutex.withLock {
            val now = SystemClock.elapsedRealtime()
            val waitUntil = maxOf(lastJikanRequestAtMs + JIKAN_MIN_REQUEST_INTERVAL_MS, jikanBackoffUntilMs)
            if (waitUntil > now) {
                delay(waitUntil - now)
            }
            lastJikanRequestAtMs = SystemClock.elapsedRealtime()
            try {
                block()
            } catch (e: Throwable) {
                registerJikanBackoffIfNeeded(e)
                throw e
            }
        }
    }

    private fun registerJikanBackoffIfNeeded(error: Throwable) {
        val status = when (error) {
            is HttpException -> error.code()
            is JikanProfileFetchException -> error.status
            else -> null
        }
        if (status != 429) return

        val now = SystemClock.elapsedRealtime()
        val remaining = (jikanBackoffUntilMs - now).coerceAtLeast(0L)
        val nextBackoff = if (remaining <= 0L) {
            JIKAN_INITIAL_BACKOFF_MS
        } else {
            (remaining * 2).coerceAtMost(JIKAN_MAX_BACKOFF_MS)
        }
        jikanBackoffUntilMs = maxOf(jikanBackoffUntilMs, now + nextBackoff)
    }

    suspend fun getTopManga(limit: Int = 20, rankingType: String = "all"): MangaResponse {
        return if (limit == 20 && rankingType == "all") apiService.getMangaRanking(clientId = clientId, nsfw = isNsfw())
        else apiService.getMangaRankingWithLimit(clientId = clientId, limit = limit, rankingType = rankingType, nsfw = isNsfw())
    }

    suspend fun getPublishingManga(loadAllPages: Boolean = false): List<JikanMangaData> {
        val cacheKey = "publishing|$loadAllPages"
        publishingMangaCache[cacheKey]?.let { return it }

        val allItems = mutableListOf<JikanMangaData>()
        var page = 1

        while (true) {
            val response = try {
                withJikanThrottle {
                    jikanApiService.getTopManga(filter = "publishing", page = page)
                }
            } catch (e: HttpException) {
                if (e.code() == 429 && allItems.isNotEmpty()) break
                throw e
            }
            if (response.data.isEmpty()) break

            allItems.addAll(response.data)

            if (!loadAllPages || response.pagination?.has_next_page != true) break
            page++
            delay(400)
        }

        return allItems.also {
            publishingMangaCache[cacheKey] = it
        }
    }

    suspend fun searchAnime(query: String): AnimeResponse {
        return apiService.searchAnime(clientId = clientId, query = query, nsfw = isNsfw())
    }
    
    suspend fun searchManga(query: String): MangaResponse {
        return apiService.searchManga(clientId = clientId, query = query, nsfw = isNsfw())
    }

    suspend fun getAnimeSuggestions(limit: Int = 5, offset: Int = 0): AnimeResponse {
        return apiService.getAnimeSuggestions(clientId = clientId, limit = limit, offset = offset)
    }

    suspend fun getMangaSuggestions(limit: Int = 5, offset: Int = 0): MangaResponse {
        return apiService.getMangaSuggestions(clientId = clientId, limit = limit, offset = offset)
    }

    suspend fun getFallbackMangaRecommendations(limit: Int = 5): List<MangaData> {
        val seedStatuses = listOf("reading", "completed")
        val seeds = mutableListOf<UserMangaData>()

        for (status in seedStatuses) {
            seeds += getUserMangaList(
                status = status,
                sort = "list_updated_at",
                limit = 10
            ).data
            if (seeds.size >= 10) break
        }

        val seenIds = seeds.map { it.node.id }.toMutableSet()
        val recommendations: MutableList<MangaData> = mutableListOf()

        for (seed in seeds) {
            val details = runCatching { getMangaDetails(seed.node.id) }.getOrNull() ?: continue
            val candidates: List<MangaData> = details.recommendations.orEmpty()
                .sortedByDescending { it.numRecommendations }
                .map { MangaData(it.node) }

            for (candidate in candidates) {
                if (candidate.node.id in seenIds) continue
                seenIds += candidate.node.id
                recommendations += candidate
                if (recommendations.size >= limit) return recommendations
            }
        }

        return recommendations
    }

    suspend fun getAnimeDetails(id: Int): AnimeDetailsResponse {
        return apiService.getAnimeDetails(clientId = clientId, animeId = id)
    }

    suspend fun getAnimeDetailsLite(id: Int): AnimeDetailsResponse {
        return apiService.getAnimeDetailsLite(clientId = clientId, animeId = id)
    }

    suspend fun getAnimeRecommendationsOnly(id: Int): AnimeDetailsResponse {
        return apiService.getAnimeRecommendationsOnly(clientId = clientId, animeId = id)
    }

    suspend fun getAnimeMyListStatus(id: Int): AnimeMyListStatusResponse {
        return apiService.getAnimeMyListStatus(clientId = clientId, animeId = id)
    }

    suspend fun getMangaDetails(id: Int): MangaDetailsResponse {
        return apiService.getMangaDetails(clientId = clientId, mangaId = id)
    }

    suspend fun getMangaDetailsLite(id: Int): MangaDetailsResponse {
        return apiService.getMangaDetailsLite(clientId = clientId, mangaId = id)
    }

    suspend fun getMangaRecommendationsOnly(id: Int): MangaDetailsResponse {
        return apiService.getMangaRecommendationsOnly(clientId = clientId, mangaId = id)
    }

    suspend fun updateMyListStatus(
        animeId: Int,
        status: String? = null,
        isRewatching: Boolean? = null,
        score: Int? = null,
        numWatchedEpisodes: Int? = null,
        priority: Int? = null,
        numTimesRewatched: Int? = null,
        rewatchValue: Int? = null,
        tags: String? = null,
        comments: String? = null,
        startDate: String? = null,
        finishDate: String? = null
    ): MyListStatus {
        return apiService.updateMyListStatus(
            clientId = clientId,
            animeId = animeId,
            status = status,
            isRewatching = isRewatching,
            score = score,
            numWatchedEpisodes = numWatchedEpisodes,
            priority = priority,
            numTimesRewatched = numTimesRewatched,
            rewatchValue = rewatchValue,
            tags = tags,
            comments = comments,
            startDate = startDate,
            finishDate = finishDate
        )
    }

    suspend fun updateMyMangaListStatus(
        mangaId: Int,
        status: String? = null,
        isRereading: Boolean? = null,
        score: Int? = null,
        numVolumesRead: Int? = null,
        numChaptersRead: Int? = null,
        priority: Int? = null,
        numTimesReread: Int? = null,
        rereadValue: Int? = null,
        tags: String? = null,
        comments: String? = null,
        startDate: String? = null,
        finishDate: String? = null
    ): MyMangaListStatus {
        return apiService.updateMyMangaListStatus(
            clientId = clientId,
            mangaId = mangaId,
            status = status,
            isRereading = isRereading,
            score = score,
            numVolumesRead = numVolumesRead,
            numChaptersRead = numChaptersRead,
            priority = priority,
            numTimesReread = numTimesReread,
            rereadValue = rereadValue,
            tags = tags,
            comments = comments,
            startDate = startDate,
            finishDate = finishDate
        )
    }

    suspend fun quickIncrementAnime(animeId: Int): MyListStatus {
        val details = getAnimeDetails(animeId)
        val currentStatus = details.myListStatus
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return if (currentStatus != null) {
            val newEpisodes = (currentStatus.numEpisodesWatched + 1).let {
                if (details.numEpisodes != null && details.numEpisodes > 0) it.coerceAtMost(details.numEpisodes) else it
            }
            val newStatus = if (details.numEpisodes != null && details.numEpisodes > 0 && newEpisodes == details.numEpisodes) "completed" else currentStatus.status
            val finishDate = if (newStatus == "completed") dateFormat.format(Date()) else currentStatus.finishDate
            
            updateMyListStatus(
                animeId = animeId,
                status = newStatus,
                numWatchedEpisodes = newEpisodes,
                finishDate = finishDate
            )
        } else {
            // Should not be called directly if not on list without confirmation, 
            // but repository can provide the logic.
            updateMyListStatus(
                animeId = animeId,
                status = "watching",
                numWatchedEpisodes = 1,
                startDate = dateFormat.format(Date())
            )
        }
    }

    suspend fun quickIncrementManga(mangaId: Int): MyMangaListStatus {
        val details = getMangaDetails(mangaId)
        val currentStatus = details.myListStatus
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return if (currentStatus != null) {
            val newChapters = (currentStatus.numChaptersRead + 1).let {
                if (details.numChapters != null && details.numChapters > 0) it.coerceAtMost(details.numChapters) else it
            }
            val newStatus = if (details.numChapters != null && details.numChapters > 0 && newChapters == details.numChapters) "completed" else currentStatus.status
            val finishDate = if (newStatus == "completed") dateFormat.format(Date()) else currentStatus.finishDate

            updateMyMangaListStatus(
                mangaId = mangaId,
                status = newStatus,
                numChaptersRead = newChapters,
                finishDate = finishDate
            )
        } else {
            updateMyMangaListStatus(
                mangaId = mangaId,
                status = "reading",
                numChaptersRead = 1,
                startDate = dateFormat.format(Date())
            )
        }
    }

    suspend fun deleteMyListStatus(animeId: Int) {
        apiService.deleteMyListStatus(clientId = clientId, animeId = animeId)
    }

    suspend fun deleteMyMangaListStatus(mangaId: Int) {
        apiService.deleteMyMangaListStatus(clientId = clientId, mangaId = mangaId)
    }

    suspend fun getAnimeCharacters(id: Int): JikanCharactersResponse {
        return withJikanThrottle { jikanApiService.getAnimeCharacters(id) }
    }

    suspend fun getMangaCharacters(id: Int): JikanCharactersResponse {
        return withJikanThrottle { jikanApiService.getMangaCharacters(id) }
    }

    suspend fun getAnimeStaff(id: Int): JikanStaffResponse {
        return withJikanThrottle { jikanApiService.getAnimeStaff(id) }
    }

    suspend fun getAnimeThemes(id: Int): JikanThemesResponse {
        return withJikanThrottle { jikanApiService.getAnimeThemes(id) }
    }

    suspend fun getAnimeReviews(id: Int): JikanReviewsResponse {
        return withJikanThrottle { jikanApiService.getAnimeReviews(id, preliminary = true) }
    }

    suspend fun getMangaReviews(id: Int): JikanReviewsResponse {
        return withJikanThrottle { jikanApiService.getMangaReviews(id, preliminary = true) }
    }

    suspend fun getAnimeStreaming(id: Int): JikanStreamingResponse {
        return withJikanThrottle { jikanApiService.getAnimeStreaming(id) }
    }

    suspend fun getAnimeStatistics(id: Int): JikanAnimeStatisticsResponse {
        return withJikanThrottle { jikanApiService.getAnimeStatistics(id) }
    }

    suspend fun getMangaStatistics(id: Int): JikanMangaStatisticsResponse {
        return withJikanThrottle { jikanApiService.getMangaStatistics(id) }
    }

    suspend fun getMyUserProfile(): UserProfile {
        return apiService.getMyUserProfile(clientId = clientId)
    }

    /**
     * MAL does not reliably return manga_statistics from /users/@me. Build the
     * fields the profile screen can show from the authenticated user's manga list.
     */
    suspend fun getMyMangaStatisticsFromList(): MangaStatistics {
        val entries = getAllUserMangaList()
        val scoredEntries = entries.filter { it.listStatus.score > 0 }

        return MangaStatistics(
            numReading = entries.count { it.listStatus.status == "reading" },
            numCompleted = entries.count { it.listStatus.status == "completed" },
            numOnHold = entries.count { it.listStatus.status == "on_hold" },
            numDropped = entries.count { it.listStatus.status == "dropped" },
            numPlanToRead = entries.count { it.listStatus.status == "plan_to_read" },
            numItems = entries.size,
            numChapters = entries.sumOf { it.listStatus.numChaptersRead },
            numVolumes = entries.sumOf { it.listStatus.numVolumesRead },
            numTimesReread = entries.sumOf { it.listStatus.numTimesReread },
            meanScore = if (scoredEntries.isEmpty()) 0f else {
                scoredEntries.map { it.listStatus.score }.average().toFloat()
            }
        )
    }

    suspend fun getUserFullProfile(username: String): JikanFullUserProfile {
        val response = withJikanThrottle { jikanApiService.getUserFullProfile(username) }
        return response.data ?: throw JikanProfileFetchException(
            status = response.status,
            responseMessage = response.message,
            responseError = response.error,
            username = username
        )
    }

    suspend fun getUserFriends(username: String): List<JikanFriend> {
        return withJikanThrottle { jikanApiService.getUserFriends(username).data }
    }

    suspend fun getUserAnimeList(username: String? = null, status: String? = null, sort: String = "list_score", offset: Int = 0, limit: Int = 100): UserAnimeListResponse {
        return apiService.getUserAnimeList(clientId = clientId, username = username ?: "@me", status = status, sort = sort, offset = offset, limit = limit, nsfw = isNsfw())
    }

    suspend fun getUserMangaList(username: String? = null, status: String? = null, sort: String = "list_score", offset: Int = 0, limit: Int = 100): UserMangaListResponse {
        return apiService.getUserMangaList(clientId = clientId, username = username ?: "@me", status = status, sort = sort, offset = offset, limit = limit, nsfw = isNsfw())
    }

    suspend fun getAllUserAnimeList(username: String? = null, status: String? = null, sort: String = "list_score"): List<UserAnimeData> {
        val allItems = mutableListOf<UserAnimeData>()
        var offset = 0
        val limit = 1000

        do {
            val response = getUserAnimeList(
                username = username,
                status = status,
                sort = sort,
                offset = offset,
                limit = limit
            )
            allItems.addAll(response.data)
            offset += response.data.size
        } while (response.paging.next != null && response.data.isNotEmpty())

        return allItems
    }

    suspend fun getAllUserMangaList(username: String? = null, status: String? = null, sort: String = "list_score"): List<UserMangaData> {
        val allItems = mutableListOf<UserMangaData>()
        var offset = 0
        val limit = 1000

        do {
            val response = getUserMangaList(
                username = username,
                status = status,
                sort = sort,
                offset = offset,
                limit = limit
            )
            allItems.addAll(response.data)
            offset += response.data.size
        } while (response.paging.next != null && response.data.isNotEmpty())

        return allItems
    }

    suspend fun getAiringAnimeDetails(malIds: List<Int>): List<AniListMedia> {
        if (malIds.isEmpty()) return emptyList()

        val nowMs = System.currentTimeMillis()
        val ids = malIds.distinct()
        val results = mutableListOf<AniListMedia>()
        val idsToFetch = mutableListOf<Int>()

        synchronized(airingScheduleCacheLock) {
            ids.forEach { id ->
                val cached = airingScheduleCache[id]
                // Do not keep a failed or incomplete lookup. Earlier app versions cached
                // these null entries, which prevented a later authenticated timetable
                // request from ever correcting the UI.
                if (cached != null &&
                    cached.second.nextAiringEpisode != null &&
                    nowMs - cached.first < AIRING_SCHEDULE_CACHE_TTL_MS
                ) {
                    results += cached.second
                } else {
                    idsToFetch += id
                }
            }
        }

        if (idsToFetch.isEmpty()) return results

        // The authenticated timetable contains the authoritative latest episode
        // number. Cache it once and use it to fill fields omitted by /anime.
        val timetableByRoute = runCatching { getCurrentTimetable() }.getOrDefault(emptyMap())
        idsToFetch.chunked(AIRING_SCHEDULE_BATCH_SIZE).forEach { batch ->
            val schedules = runCatching {
                animeScheduleApiService.getAnimeByMalIds(batch).anime
            }.getOrDefault(emptyList())
            val mediaById = schedules.mapNotNull { schedule ->
                val id = schedule.websites?.mal
                    ?.substringAfter("/anime/", "")
                    ?.substringBefore('/')
                    ?.toIntOrNull()
                    ?: return@mapNotNull null
                val nextAiring = schedule.toNextAiringEpisode(timetableByRoute[schedule.route])
                    ?: return@mapNotNull null
                AniListMedia(idMal = id, nextAiringEpisode = nextAiring)
            }.associateBy { it.idMal }

            batch.forEach { id ->
                mediaById[id]?.let { media ->
                    results += media
                    synchronized(airingScheduleCacheLock) {
                        airingScheduleCache[id] = nowMs to media
                    }
                }
            }
        }

        return results
    }

    private suspend fun getCurrentTimetable(): Map<String, AnimeScheduleTimetableEntry> {
        val nowMs = System.currentTimeMillis()
        timetableCache?.takeIf { nowMs - it.first < AIRING_SCHEDULE_CACHE_TTL_MS }?.let { return it.second }
        val timetable = animeScheduleApiService.getRawTimetable().associateBy { it.route }
        timetableCache = nowMs to timetable
        return timetable
    }

    private fun com.example.myapplication.data.remote.AnimeScheduleAnime.toNextAiringEpisode(
        timetable: AnimeScheduleTimetableEntry?
    ): com.example.myapplication.data.model.NextAiringEpisode? {
        val exactEpisodeDate = (episodeDate ?: timetable?.episodeDate)?.let(::parseAnimeScheduleDateTime)
        if (!status.equals("ongoing", ignoreCase = true) && exactEpisodeDate == null) return null
        // Exact episodeDate is authoritative when available. jpnTime is only the
        // recurring-time fallback for schedule records without an exact timestamp.
        val scheduledAt = exactEpisodeDate ?: jpnTime?.let(::parseAnimeScheduleDateTime) ?: return null
        val scheduledTime = scheduledAt.atZone(ZoneOffset.UTC)
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        var next = now.withHour(scheduledTime.hour)
            .withMinute(scheduledTime.minute)
            .withSecond(scheduledTime.second)
            .withNano(0)
        val daysUntil = (scheduledTime.dayOfWeek.value - next.dayOfWeek.value + 7) % 7
        next = next.plusDays(daysUntil.toLong())
        if (!next.isAfter(now)) next = next.plusWeeks(1)
        delayedUntil?.let { delayed ->
            parseAnimeScheduleDateTime(delayed)?.atZone(ZoneOffset.UTC)
                ?.takeIf { it.isAfter(next) }
                ?.let { next = it }
        }
        // The authenticated /anime response contains these exact fields. The weekly
        // timetable remains a fallback for records where they are unavailable.
        val timetableDate = exactEpisodeDate
        val timetableEpisode = episodeNumber ?: timetable?.episodeNumber
        val timetableStatus = airingStatus ?: timetable?.airingStatus
        val exactNext = timetableDate?.takeIf { it.isAfter(Instant.now()) }
        val nextEpisode = when {
            exactNext != null -> timetableEpisode
            timetableEpisode != null && timetableStatus.equals("aired", ignoreCase = true) -> timetableEpisode + 1
            // A delayed/unconfirmed entry still identifies the next numbered episode.
            // Keeping that number lets the list show the known aired count instead of '?'.
            timetableEpisode != null -> timetableEpisode
            else -> null
        }
        val nextInstant = exactNext ?: next.toInstant()
        return com.example.myapplication.data.model.NextAiringEpisode(
            airingAt = nextInstant.epochSecond,
            timeUntilAiring = ChronoUnit.SECONDS.between(now.toInstant(), nextInstant),
            episode = nextEpisode,
            source = "anime_schedule"
        )
    }

    /** AnimeSchedule currently sends jpnTime as `dd-MM-yyyy HH:mm:ss`, while
     * timetable fields use ISO-8601. Support both formats. */
    private fun parseAnimeScheduleDateTime(value: String): Instant? {
        return runCatching { Instant.parse(value) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(
                    value,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                ).atZone(ZoneId.of("Asia/Tokyo")).toInstant()
            }.getOrNull()
    }
}
