package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AnimeData
import com.example.myapplication.data.model.AnimeNode
import com.example.myapplication.data.model.MangaData
import com.example.myapplication.data.model.MangaNode
import com.example.myapplication.data.model.MainPicture
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.MyMangaListStatus
import com.example.myapplication.data.remote.JikanMangaData
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import android.os.SystemClock
import java.net.SocketTimeoutException
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AnimeViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val prefsManager: UserPreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<AnimeUiState>(AnimeUiState.Loading)
    val uiState: StateFlow<AnimeUiState> = _uiState.asStateFlow()

    private val _quickUpdateEvent = MutableStateFlow<QuickUpdateEvent?>(null)
    val quickUpdateEvent: StateFlow<QuickUpdateEvent?> = _quickUpdateEvent.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userPfp = MutableStateFlow<String?>(null)
    val userPfp: StateFlow<String?> = _userPfp.asStateFlow()

    private val _airingDetails = MutableStateFlow<Map<Int, AniListMedia>>(emptyMap())
    val airingDetails: StateFlow<Map<Int, AniListMedia>> = _airingDetails.asStateFlow()
    private val _randomAnimeNavigation = MutableStateFlow<Int?>(null)
    val randomAnimeNavigation: StateFlow<Int?> = _randomAnimeNavigation.asStateFlow()
    private val _isRefreshingAnimeRecommendations = MutableStateFlow(false)
    val isRefreshingAnimeRecommendations: StateFlow<Boolean> = _isRefreshingAnimeRecommendations.asStateFlow()
    private val _isRefreshingMangaRecommendations = MutableStateFlow(false)
    val isRefreshingMangaRecommendations: StateFlow<Boolean> = _isRefreshingMangaRecommendations.asStateFlow()

    // Generic keys: not_in_list, active, completed, on_hold, planned, dropped
    private val _listFilters = MutableStateFlow(setOf("not_in_list", "active", "completed", "on_hold", "planned", "dropped"))
    val listFilters: StateFlow<Set<String>> = _listFilters.asStateFlow()

    val nsfwEnabled = prefsManager.nsfwFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private var searchJob: Job? = null
    private var currentSeasonalData: List<AnimeData> = emptyList()
    private var currentMangaDiscoveryData: List<MangaData> = emptyList()
    
    private val calendar = Calendar.getInstance()
    private val initialYear = calendar.get(Calendar.YEAR)
    private val initialSeason = getCurrentSeasonString()
    
    private var currentYear = initialYear
    private var currentSeason = initialSeason
    private var currentSeasonalSort = "members"
    
    private var currentDiscoveryType = "publishing"
    private var currentDiscoverySort = "members"
    private var lastHomeLoadAtMs: Long = 0L
    private var cachedHomeState: AnimeUiState.HomeSuccess? = null

    init {
        fetchUserPfp()
        
        // Respond to NSFW setting changes without resetting UI state unnecessarily
        viewModelScope.launch {
            prefsManager.nsfwFlow.drop(1).collect {
                refreshCurrentView()
            }
        }
    }

    private fun getCurrentSeasonString(): String {
        val month = calendar.get(Calendar.MONTH)
        return when (month) {
            in 0..2 -> "winter"
            in 3..5 -> "spring"
            in 6..8 -> "summer"
            else -> "fall"
        }
    }

    fun loadHomeData() {
        val now = SystemClock.elapsedRealtime()
        if (_uiState.value is AnimeUiState.Loading && now - lastHomeLoadAtMs < 10_000L) return
        if (_uiState.value is AnimeUiState.HomeSuccess && now - lastHomeLoadAtMs < 60_000L) return
        if (cachedHomeState != null && now - lastHomeLoadAtMs < 60_000L) {
            _uiState.value = cachedHomeState!!
            return
        }

        searchJob?.cancel()
        viewModelScope.launch {
            lastHomeLoadAtMs = SystemClock.elapsedRealtime()
            val previousState = cachedHomeState
            if (previousState == null) {
                _uiState.value = AnimeUiState.Loading
            }
            try {
                supervisorScope {
                    val continueWatchingDeferred = async {
                        runCatching {
                            repository.getUserAnimeList(
                                status = "watching",
                                sort = "list_updated_at",
                                limit = 5
                            ).data.map { userAnime ->
                                AnimeData(
                                    node = userAnime.node.copy(
                                        myListStatus = MyListStatus(
                                            status = userAnime.listStatus.status,
                                            score = userAnime.listStatus.score,
                                            numEpisodesWatched = userAnime.listStatus.numEpisodesWatched,
                                            isRewatching = userAnime.listStatus.isRewatching,
                                            updatedAt = userAnime.listStatus.updatedAt
                                        )
                                    )
                                )
                            }
                        }.getOrDefault(emptyList())
                    }
                    val continueReadingDeferred = async {
                        runCatching {
                            repository.getUserMangaList(
                                status = "reading",
                                sort = "list_updated_at",
                                limit = 5
                            ).data.map { userManga ->
                                MangaData(
                                    node = userManga.node.copy(
                                        myListStatus = MyMangaListStatus(
                                            status = userManga.listStatus.status,
                                            score = userManga.listStatus.score,
                                            numVolumesRead = userManga.listStatus.numVolumesRead,
                                            numChaptersRead = userManga.listStatus.numChaptersRead,
                                            isRereading = userManga.listStatus.isRereading,
                                            updatedAt = userManga.listStatus.updatedAt
                                        )
                                    )
                                )
                            }
                        }.getOrDefault(emptyList())
                    }
                    val animeSuggestionsDeferred = async {
                        runCatching<List<AnimeData>> { repository.getAnimeSuggestions(limit = 5).data }
                            .getOrDefault(emptyList<AnimeData>())
                    }
                    val mangaSuggestionsDeferred = async {
                        runCatching<List<MangaData>> { repository.getMangaSuggestions(limit = 5).data }
                            .recoverCatching { repository.getFallbackMangaRecommendations(limit = 5) }
                            .getOrDefault(emptyList<MangaData>())
                    }

                    val continueWatching = continueWatchingDeferred.await()
                    val continueReading = continueReadingDeferred.await()
                    val animeSuggestions = animeSuggestionsDeferred.await()
                    val mangaSuggestions = enrichMangaRecommendationStats(mangaSuggestionsDeferred.await())

                    val successState = AnimeUiState.HomeSuccess(
                        continueWatching = continueWatching,
                        continueReading = continueReading,
                        seasonal = emptyList(),
                        topAnime = emptyList(),
                        topManga = emptyList(),
                        animeRecommendations = animeSuggestions,
                        mangaRecommendations = mangaSuggestions,
                        year = currentYear,
                        season = currentSeason,
                        canGoNext = canGoToNextSeason(currentYear, currentSeason)
                    )
                    cachedHomeState = successState
                    _uiState.value = successState
                    lastHomeLoadAtMs = SystemClock.elapsedRealtime()

                    fetchAiringDetails(animeSuggestions.map { it.node.id })
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
                _uiState.value = previousState ?: AnimeUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun refreshAnimeRecommendations() {
        val currentState = _uiState.value as? AnimeUiState.HomeSuccess ?: return
        viewModelScope.launch {
            _isRefreshingAnimeRecommendations.value = true
            try {
                val previousIds = currentState.animeRecommendations.map { it.node.id }.toSet()
                val fetched = repository.getAnimeSuggestions(limit = 10).data
                    .distinctBy { it.node.id }
                val fresh = (fetched.filter { it.node.id !in previousIds } + fetched)
                    .distinctBy { it.node.id }
                    .take(5)

                val updatedState = currentState.copy(animeRecommendations = fresh)
                cachedHomeState = updatedState
                _uiState.value = updatedState
                fetchAiringDetails(fresh.map { it.node.id })
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
            } finally {
                _isRefreshingAnimeRecommendations.value = false
            }
        }
    }

    fun refreshMangaRecommendations() {
        val currentState = _uiState.value as? AnimeUiState.HomeSuccess ?: return
        viewModelScope.launch {
            _isRefreshingMangaRecommendations.value = true
            try {
                val previousIds = currentState.mangaRecommendations.map { it.node.id }.toSet()
                val fetched = runCatching { repository.getMangaSuggestions(limit = 10).data }
                    .recoverCatching { repository.getFallbackMangaRecommendations(limit = 10) }
                    .getOrDefault(emptyList())
                    .distinctBy { it.node.id }
                val enrichedFresh = enrichMangaRecommendationStats(
                    (fetched.filter { it.node.id !in previousIds } + fetched)
                        .distinctBy { it.node.id }
                        .take(5)
                )
                val updatedState = currentState.copy(mangaRecommendations = enrichedFresh)
                cachedHomeState = updatedState
                _uiState.value = updatedState
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
            } finally {
                _isRefreshingMangaRecommendations.value = false
            }
        }
    }

    fun openRandomAnime() {
        viewModelScope.launch {
            try {
                val pool = repository.getTopAnime(limit = 100, rankingType = "all").data
                val randomId = pool.randomOrNull()?.node?.id
                if (randomId != null) {
                    _randomAnimeNavigation.value = randomId
                } else {
                    _errorMessage.value = "Could not find a random anime right now. Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
            }
        }
    }

    fun consumeRandomAnimeNavigation() {
        _randomAnimeNavigation.value = null
    }

    private fun fetchUserPfp() {
        viewModelScope.launch {
            try {
                val profile = repository.getMyUserProfile()
                _userPfp.value = profile.picture
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun fetchAiringDetails(malIds: List<Int>) {
        viewModelScope.launch {
            try {
                val detailsMap = _airingDetails.value.toMutableMap()
                val chunkedIds = malIds.chunked(50)
                for (chunk in chunkedIds) {
                    val anilistMedia = repository.getAiringAnimeDetails(chunk)
                    for (media in anilistMedia) {
                        media.idMal?.let { detailsMap[it] = media }
                    }
                }
                _airingDetails.value = detailsMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun changeSeason(delta: Int) {
        if (delta > 0 && !canGoToNextSeason(currentYear, currentSeason)) return

        val seasons = listOf("winter", "spring", "summer", "fall")
        var index = seasons.indexOf(currentSeason) + delta
        
        var newYear = currentYear
        if (index < 0) {
            index = 3
            newYear--
        } else if (index > 3) {
            index = 0
            newYear++
        }
        
        currentYear = newYear
        currentSeason = seasons[index]
        showSeasonalDetails(currentSeasonalSort)
    }

    fun selectSpecificSeason(year: Int, season: String) {
        val targetSeason = season.lowercase()
        if (isFutureSeason(year, targetSeason)) {
            // Cap to current + 1 if they try to go too far manually
            val seasons = listOf("winter", "spring", "summer", "fall")
            val currentIndex = seasons.indexOf(initialSeason)
            if (currentIndex == 3) {
                currentYear = initialYear + 1
                currentSeason = "winter"
            } else {
                currentYear = initialYear
                currentSeason = seasons[currentIndex + 1]
            }
        } else {
            currentYear = year
            currentSeason = targetSeason
        }
        showSeasonalDetails(currentSeasonalSort)
    }

    private fun canGoToNextSeason(year: Int, season: String): Boolean {
        val seasons = listOf("winter", "spring", "summer", "fall")
        val currentIndex = seasons.indexOf(initialSeason)
        
        val maxYear: Int
        val maxSeason: String
        
        if (currentIndex == 3) {
            maxYear = initialYear + 1
            maxSeason = "winter"
        } else {
            maxYear = initialYear
            maxSeason = seasons[currentIndex + 1]
        }
        
        if (year < maxYear) return true
        if (year > maxYear) return false
        
        return seasons.indexOf(season) < seasons.indexOf(maxSeason)
    }

    private fun isFutureSeason(year: Int, season: String): Boolean {
        val seasons = listOf("winter", "spring", "summer", "fall")
        val currentIndex = seasons.indexOf(initialSeason)
        
        val maxYear: Int
        val maxSeason: String
        
        if (currentIndex == 3) {
            maxYear = initialYear + 1
            maxSeason = "winter"
        } else {
            maxYear = initialYear
            maxSeason = seasons[currentIndex + 1]
        }

        if (year > maxYear) return true
        if (year == maxYear && seasons.indexOf(season) > seasons.indexOf(maxSeason)) return true
        return false
    }

    private fun mapMalStatusToFilter(malStatus: String?): String {
        return when (malStatus?.lowercase()) {
            "watching", "reading" -> "active"
            "plan_to_watch", "plan_to_read" -> "planned"
            null -> "not_in_list"
            else -> malStatus.lowercase() // completed, on_hold, dropped
        }
    }

    fun toggleFilter(filter: String) {
        val current = _listFilters.value.toMutableSet()
        val allFilters = listOf("not_in_list", "active", "completed", "on_hold", "planned", "dropped")
        if (filter == "all") {
            if (current.containsAll(allFilters)) {
                current.clear()
            } else {
                current.addAll(allFilters)
            }
        } else {
            if (current.contains(filter)) {
                current.remove(filter)
            } else {
                current.add(filter)
            }
        }
        _listFilters.value = current
        
        val currentState = _uiState.value
        if (currentState is AnimeUiState.SeasonalDetails) {
            applyCurrentSeasonalFiltersAndSort()
        } else if (currentState is AnimeUiState.MangaDiscoveryDetails) {
            applyCurrentMangaDiscoveryFiltersAndSort()
        } else if (currentState is AnimeUiState.TopDiscoveryDetails) {
            applyCurrentTopDiscoveryFiltersAndSort()
        }
    }

    fun showSeasonalDetails(sort: String = "members") {
        currentSeasonalSort = sort
        viewModelScope.launch {
            _uiState.value = AnimeUiState.Loading
            try {
                val response = repository.getSeasonalAnime(currentYear, currentSeason, loadAllPages = true)
                currentSeasonalData = response.data
                applyCurrentSeasonalFiltersAndSort()
                fetchAiringDetails(currentSeasonalData.map { it.node.id })
            } catch (e: Exception) {
                _uiState.value = AnimeUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    private fun applyCurrentSeasonalFiltersAndSort() {
        var filtered = currentSeasonalData.filter { anime ->
            val statusKey = mapMalStatusToFilter(anime.node.myListStatus?.status)
            _listFilters.value.contains(statusKey)
        }

        filtered = when (currentSeasonalSort) {
            "score" -> filtered.sortedByDescending { it.node.meanScore ?: 0f }
            "members" -> filtered.sortedByDescending { it.node.numListUsers ?: 0 }
            else -> filtered
        }

        val categorized = filtered.groupBy { 
            it.node.mediaType?.uppercase() ?: "UNKNOWN" 
        }
        
        _uiState.value = AnimeUiState.SeasonalDetails(
            categorizedAnime = categorized,
            year = currentYear,
            season = currentSeason,
            canGoNext = canGoToNextSeason(currentYear, currentSeason),
            currentSort = currentSeasonalSort
        )
    }

    fun showMangaDiscovery(type: String = "publishing", sort: String = "members") {
        currentDiscoveryType = type
        currentDiscoverySort = sort
        if (type == "top") {
            showTopDiscovery(isAnime = false, sort = sort)
            return
        }
        viewModelScope.launch {
            _uiState.value = AnimeUiState.Loading
            try {
                val mangaList = repository.getPublishingManga(loadAllPages = true).map { it.toMangaData() }
                currentMangaDiscoveryData = mangaList
                applyCurrentMangaDiscoveryFiltersAndSort()
            } catch (e: Exception) {
                _uiState.value = AnimeUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    private fun applyCurrentMangaDiscoveryFiltersAndSort() {
        var filtered = currentMangaDiscoveryData.filter { manga ->
            val statusKey = mapMalStatusToFilter(manga.node.myListStatus?.status)
            _listFilters.value.contains(statusKey)
        }

        filtered = when (currentDiscoverySort) {
            "score" -> filtered.sortedByDescending { it.node.meanScore ?: 0f }
            else -> filtered
        }

        val categorized = filtered.groupBy { 
            it.node.mediaType?.uppercase() ?: "UNKNOWN" 
        }

        _uiState.value = AnimeUiState.MangaDiscoveryDetails(
            categorizedManga = categorized,
            title = "Releasing Now (Manga)",
            currentSort = currentDiscoverySort,
            type = currentDiscoveryType
        )
    }

    fun showTopDiscovery(isAnime: Boolean, sort: String = "members") {
        currentDiscoveryType = if (isAnime) "top_anime" else "top_manga"
        currentDiscoverySort = sort
        viewModelScope.launch {
            _uiState.value = AnimeUiState.Loading
            try {
                val rankingType = if (sort == "score") "all" else "bypopularity"
                if (isAnime) {
                    currentSeasonalData = repository.getTopAnime(limit = 100, rankingType = rankingType).data
                } else {
                    currentMangaDiscoveryData = repository.getTopManga(limit = 100, rankingType = rankingType).data
                }
                applyCurrentTopDiscoveryFiltersAndSort()
            } catch (e: Exception) {
                _uiState.value = AnimeUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    private fun applyCurrentTopDiscoveryFiltersAndSort() {
        val isAnime = currentDiscoveryType == "top_anime"
        val data = if (isAnime) currentSeasonalData else currentMangaDiscoveryData.map { it.toAnimeData() }
        
        var filtered = data.filter { item ->
            val statusKey = mapMalStatusToFilter(item.node.myListStatus?.status)
            _listFilters.value.contains(statusKey)
        }

        filtered = when (currentDiscoverySort) {
            "score" -> filtered.sortedByDescending { it.node.meanScore ?: 0f }
            else -> filtered
        }

        _uiState.value = AnimeUiState.TopDiscoveryDetails(
            items = filtered,
            title = if (isAnime) "Top 100 Anime" else "Top 100 Manga",
            currentSort = currentDiscoverySort,
            isAnime = isAnime
        )
    }

    fun searchAnime(query: String, isAnimeSearch: Boolean) {
        searchJob?.cancel()
        if (query.isBlank()) {
            loadHomeData()
            return
        }
        
        if (query.length < 3) return

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.value = AnimeUiState.Loading
            try {
                if (isAnimeSearch) {
                    val animeResponse = repository.searchAnime(query)
                    _uiState.value = AnimeUiState.SearchSuccess(
                        animeList = animeResponse.data,
                        mangaList = emptyList()
                    )
                    fetchAiringDetails(animeResponse.data.map { it.node.id })
                } else {
                    val mangaResponse = repository.searchManga(query)
                    _uiState.value = AnimeUiState.SearchSuccess(
                        animeList = emptyList(),
                        mangaList = mangaResponse.data
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
                _uiState.value = AnimeUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun onLongPressAnime(animeId: Int, title: String) {
        viewModelScope.launch {
            try {
                val details = repository.getAnimeDetails(animeId)
                if (details.myListStatus != null && details.myListStatus.status != null) {
                    repository.quickIncrementAnime(animeId)
                    refreshCurrentView()
                } else {
                    _quickUpdateEvent.value = QuickUpdateEvent.ConfirmAdd(animeId, title, isAnime = true)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun onLongPressManga(mangaId: Int, title: String) {
        viewModelScope.launch {
            try {
                val details = repository.getMangaDetails(mangaId)
                if (details.myListStatus != null && details.myListStatus.status != null) {
                    repository.quickIncrementManga(mangaId)
                    refreshCurrentView()
                } else {
                    _quickUpdateEvent.value = QuickUpdateEvent.ConfirmAdd(mangaId, title, isAnime = false)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun confirmQuickAdd(id: Int, isAnime: Boolean) {
        viewModelScope.launch {
            try {
                if (isAnime) repository.quickIncrementAnime(id)
                else repository.quickIncrementManga(id)
                _quickUpdateEvent.value = null
                refreshCurrentView()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    private fun refreshCurrentView() {
        val currentState = _uiState.value
        when (currentState) {
            is AnimeUiState.HomeSuccess -> loadHomeData()
            is AnimeUiState.SeasonalDetails -> showSeasonalDetails(currentSeasonalSort)
            is AnimeUiState.MangaDiscoveryDetails -> showMangaDiscovery(currentDiscoveryType, currentDiscoverySort)
            is AnimeUiState.TopDiscoveryDetails -> showTopDiscovery(currentState.isAnime, currentDiscoverySort)
            else -> {}
        }
    }

    fun getDiscoveryGridModeFlow(isAnime: Boolean) = if (isAnime) {
        prefsManager.defaultAnimeDiscoveryStyleFlow
    } else {
        prefsManager.defaultMangaDiscoveryStyleFlow
    }

    fun setDiscoveryGridMode(isAnime: Boolean, isGrid: Boolean) {
        viewModelScope.launch {
            if (isAnime) prefsManager.saveDefaultAnimeDiscoveryStyle(isGrid)
            else prefsManager.saveDefaultMangaDiscoveryStyle(isGrid)
        }
    }

    fun getDiscoverySortFlow(isAnime: Boolean) = if (isAnime) {
        prefsManager.defaultAnimeDiscoverySortFlow
    } else {
        prefsManager.defaultMangaDiscoverySortFlow
    }

    fun getHomeContinueWatchingEnabledFlow() = prefsManager.homeContinueWatchingEnabledFlow
    fun getHomeContinueReadingEnabledFlow() = prefsManager.homeContinueReadingEnabledFlow
    fun getHomeDiscoveryButtonsEnabledFlow() = prefsManager.homeDiscoveryButtonsEnabledFlow
    fun getHomeRandomAnimeEnabledFlow() = prefsManager.homeRandomAnimeEnabledFlow
    fun getHomeAnimePicksEnabledFlow() = prefsManager.homeAnimePicksEnabledFlow
    fun getHomeMangaPicksEnabledFlow() = prefsManager.homeMangaPicksEnabledFlow

    fun setDiscoverySort(isAnime: Boolean, sort: String) {
        viewModelScope.launch {
            if (isAnime) prefsManager.saveDefaultAnimeDiscoverySort(sort)
            else prefsManager.saveDefaultMangaDiscoverySort(sort)
        }
    }

    fun dismissQuickUpdate() {
        _quickUpdateEvent.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun getFriendlyErrorMessage(error: Throwable): String {
        return if (error is SocketTimeoutException) {
            "Request timed out. MAL or Jikan may be responding slowly, too many requests may have fired close together, a request may have stalled while switching screens, or your network may be unstable."
        } else {
            error.message ?: "Something went wrong."
        }
    }

    private suspend fun enrichMangaRecommendationStats(items: List<MangaData>): List<MangaData> = supervisorScope {
        items.map { manga ->
            async {
                val needsStats = manga.node.meanScore == null || manga.node.numListUsers == null
                if (!needsStats) return@async manga

                // Suggestion payloads can be partial; fetch details with a retry to avoid transient misses/timeouts.
                val details = runCatching { repository.getMangaDetails(manga.node.id) }.getOrNull()
                    ?: run {
                        delay(250)
                        runCatching { repository.getMangaDetails(manga.node.id) }.getOrNull()
                    }

                if (details == null) {
                    return@async manga.copy(
                        node = manga.node.copy(
                            meanScore = manga.node.meanScore ?: 0f,
                            numListUsers = manga.node.numListUsers ?: 0
                        )
                    )
                }

                manga.copy(
                    node = manga.node.copy(
                        meanScore = manga.node.meanScore ?: details.mean ?: 0f,
                        numListUsers = manga.node.numListUsers ?: details.numListUsers ?: 0
                    )
                )
            }
        }.awaitAll()
    }

    private fun JikanMangaData.toMangaData(): MangaData {
        return MangaData(
            node = MangaNode(
                id = this.mal_id,
                title = this.title,
                mainPicture = MainPicture(
                    medium = this.images.jpg?.image_url ?: "",
                    large = this.images.jpg?.large_image_url ?: this.images.jpg?.image_url ?: ""
                ),
                meanScore = this.score,
                numListUsers = this.members,
                synopsis = this.synopsis,
                status = this.status,
                mediaType = this.type
            )
        )
    }

    private fun MangaData.toAnimeData(): AnimeData {
        return AnimeData(
            node = AnimeNode(
                id = this.node.id,
                title = this.node.title,
                mainPicture = this.node.mainPicture,
                synopsis = this.node.synopsis,
                meanScore = this.node.meanScore,
                numListUsers = this.node.numListUsers,
                mediaType = this.node.mediaType ?: "manga",
                alternativeTitles = this.node.alternativeTitles,
                myListStatus = this.node.myListStatus?.let {
                    com.example.myapplication.data.model.MyListStatus(
                        status = it.status,
                        score = it.score,
                        numEpisodesWatched = it.numChaptersRead,
                        isRewatching = it.isRereading,
                        updatedAt = it.updatedAt
                    )
                }
            )
        )
    }
}

sealed interface QuickUpdateEvent {
    data class ConfirmAdd(val id: Int, val title: String, val isAnime: Boolean) : QuickUpdateEvent
}

sealed interface AnimeUiState {
    data object Loading : AnimeUiState
    data class HomeSuccess(
        val continueWatching: List<AnimeData>,
        val continueReading: List<MangaData>,
        val seasonal: List<AnimeData>,
        val topAnime: List<AnimeData>,
        val topManga: List<MangaData>,
        val animeRecommendations: List<AnimeData>,
        val mangaRecommendations: List<MangaData>,
        val year: Int,
        val season: String,
        val canGoNext: Boolean
    ) : AnimeUiState
    data class SearchSuccess(
        val animeList: List<AnimeData>,
        val mangaList: List<MangaData>
    ) : AnimeUiState
    data class SeasonalDetails(
        val categorizedAnime: Map<String, List<AnimeData>>,
        val year: Int,
        val season: String,
        val canGoNext: Boolean,
        val currentSort: String
    ) : AnimeUiState
    data class MangaDiscoveryDetails(
        val categorizedManga: Map<String, List<MangaData>>,
        val title: String,
        val currentSort: String,
        val type: String
    ) : AnimeUiState
    data class TopDiscoveryDetails(
        val items: List<AnimeData>,
        val title: String,
        val currentSort: String,
        val isAnime: Boolean
    ) : AnimeUiState
    data class Error(val message: String) : AnimeUiState
}
