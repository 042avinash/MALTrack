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
                    val seasonalDeferred = async { repository.getSeasonalAnime(currentYear, currentSeason).data }
                    val topAnimeDeferred = async { repository.getTopAnime().data }
                    val topMangaDeferred = async { repository.getTopManga().data }
                    val publishingMangaDeferred = async { repository.getPublishingManga() }

                    val seasonal = seasonalDeferred.await()
                    val topAnime = topAnimeDeferred.await()
                    val topManga = topMangaDeferred.await()
                    val publishingManga = publishingMangaDeferred.await().map { it.toMangaData() }

                    currentSeasonalData = seasonal
                    val successState = AnimeUiState.HomeSuccess(
                        seasonal = seasonal,
                        topAnime = topAnime,
                        topManga = topManga,
                        publishingManga = publishingManga,
                        year = currentYear,
                        season = currentSeason,
                        canGoNext = canGoToNextSeason(currentYear, currentSeason)
                    )
                    cachedHomeState = successState
                    _uiState.value = successState
                    lastHomeLoadAtMs = SystemClock.elapsedRealtime()

                    fetchAiringDetails(seasonal.map { it.node.id })
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorMessage.value = getFriendlyErrorMessage(e)
                _uiState.value = previousState ?: AnimeUiState.Error(e.message ?: "Unknown Error")
            }
        }
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
                val response = repository.getSeasonalAnime(currentYear, currentSeason)
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
            "title" -> filtered.sortedBy { it.node.title.lowercase() }
            else -> filtered // API default
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
                val mangaList = repository.getPublishingManga().map { it.toMangaData() }
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
            "title" -> filtered.sortedBy { it.node.title.lowercase() }
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

    fun searchAnime(query: String) {
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
                supervisorScope {
                    val animeDeferred = async { repository.searchAnime(query) }
                    val mangaDeferred = async { repository.searchManga(query) }

                    val animeResponse = animeDeferred.await()
                    val mangaResponse = mangaDeferred.await()

                    _uiState.value = AnimeUiState.SearchSuccess(animeResponse.data, mangaResponse.data)

                    val allAnimeIds = animeResponse.data.map { it.node.id }
                    fetchAiringDetails(allAnimeIds)
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

    fun getGridModeFlow() = prefsManager.getGridModeFlow("seasonal_chart")

    fun setGridMode(isGrid: Boolean) {
        viewModelScope.launch {
            prefsManager.saveGridMode("seasonal_chart", isGrid, false)
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
        val seasonal: List<AnimeData>,
        val topAnime: List<AnimeData>,
        val topManga: List<MangaData>,
        val publishingManga: List<MangaData>,
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
