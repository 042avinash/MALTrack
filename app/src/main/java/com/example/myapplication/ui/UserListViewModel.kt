package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.UserAnimeData
import com.example.myapplication.data.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val prefsManager: UserPreferencesManager
) : ViewModel() {
    private val animeStatuses = listOf("all", "watching", "completed", "on_hold", "plan_to_watch", "dropped")

    private val _userListState = MutableStateFlow(UserListState())
    val userListState: StateFlow<UserListState> = _userListState.asStateFlow()

    private val _airingDetails = MutableStateFlow<Map<Int, AniListMedia>>(emptyMap())
    val airingDetails: StateFlow<Map<Int, AniListMedia>> = _airingDetails.asStateFlow()
    private val _savedSorts = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _savedGridModes = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _refreshGenerations = MutableStateFlow<Map<String, Int>>(emptyMap())
    val refreshGenerations: StateFlow<Map<String, Int>> = _refreshGenerations.asStateFlow()
    private val _searchState = MutableStateFlow(UserAnimeSearchState())
    val searchState: StateFlow<UserAnimeSearchState> = _searchState.asStateFlow()
    private val _loadedLists = MutableStateFlow<Map<String, List<UserAnimeData>>>(emptyMap())
    val loadedLists: StateFlow<Map<String, List<UserAnimeData>>> = _loadedLists.asStateFlow()
    private val _loadingStatuses = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val loadingStatuses: StateFlow<Map<String, Boolean>> = _loadingStatuses.asStateFlow()

    val nsfwEnabled = prefsManager.nsfwFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private var statsCache: Map<String, Int> = emptyMap()
    private val fullAnimeListCache = mutableMapOf<String, List<UserAnimeData>>()
    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        preloadSavedPreferences()
    }

    private fun preloadSavedPreferences() {
        animeStatuses.forEach { status ->
            viewModelScope.launch {
                val listKey = "anime_$status"
                val sort = prefsManager.getSortModeFlow(listKey).first()
                val grid = prefsManager.getGridModeFlow(listKey).first()

                _savedSorts.value = _savedSorts.value + (status to sort)
                _savedGridModes.value = _savedGridModes.value + (status to grid)
            }
        }
    }

    fun loadUserList(status: String?, username: String? = null, sort: String? = null, forceRefresh: Boolean = false) {
        val statusKey = status ?: "all"
        val effectiveUsername = if (username == "null") null else username

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val savedSort = prefsManager.getSortModeFlow("anime_$statusKey").first()
                val effectiveSort = sort ?: savedSort
                val cacheKey = listOf(effectiveUsername ?: "@me", statusKey, effectiveSort).joinToString("|")
                val statusLoadingKey = listOf(effectiveUsername ?: "@me", statusKey).joinToString("|")

                _savedSorts.value = _savedSorts.value + (statusKey to effectiveSort)

                if (_userListState.value.username != effectiveUsername) {
                    _airingDetails.value = emptyMap()
                }

                _userListState.value = UserListState(
                    status = statusKey,
                    username = effectiveUsername,
                    sort = effectiveSort
                )

                if (forceRefresh) {
                    fullAnimeListCache.remove(cacheKey)
                    _loadedLists.value = _loadedLists.value - cacheKey
                }

                if (!fullAnimeListCache.containsKey(cacheKey)) {
                    _loadingStatuses.value = _loadingStatuses.value + (statusLoadingKey to true)
                    try {
                        val fullList = repository.getAllUserAnimeList(
                            username = effectiveUsername,
                            status = if (statusKey == "all") null else statusKey,
                            sort = effectiveSort
                        )
                        fullAnimeListCache[cacheKey] = fullList
                        _loadedLists.value = _loadedLists.value + (cacheKey to fullList)

                        val malIds = fullList
                            .asSequence()
                            .filter { it.node.status == "currently_airing" }
                            .map { it.node.id }
                            .filter { it !in _airingDetails.value.keys }
                            .toList()
                        if (malIds.isNotEmpty()) {
                            val airingDetails = repository.getAiringAnimeDetails(malIds)
                                .filter { it.idMal != null }
                                .associateBy { it.idMal!! }
                            _airingDetails.value = _airingDetails.value + airingDetails
                        }
                    } finally {
                        _loadingStatuses.value = _loadingStatuses.value + (statusLoadingKey to false)
                    }
                }

                if (statsCache.isEmpty() || forceRefresh) {
                    try {
                        val profile = if (effectiveUsername != null) {
                            repository.getUserFullProfile(effectiveUsername).statistics?.anime?.let { jikan ->
                                statsCache = mapOf(
                                    "all" to jikan.total_entries,
                                    "watching" to jikan.watching,
                                    "completed" to jikan.completed,
                                    "on_hold" to jikan.on_hold,
                                    "plan_to_watch" to jikan.plan_to_watch,
                                    "dropped" to jikan.dropped
                                )
                            }
                            repository.getMyUserProfile()
                        } else {
                            repository.getMyUserProfile()
                        }

                        if (statsCache.isEmpty() || effectiveUsername == null) {
                            val stats = profile.animeStatistics
                            statsCache = mapOf(
                                "all" to (stats?.numItems ?: 0),
                                "watching" to (stats?.numWatching ?: 0),
                                "completed" to (stats?.numCompleted ?: 0),
                                "on_hold" to (stats?.numOnHold ?: 0),
                                "plan_to_watch" to (stats?.numPlanToWatch ?: 0),
                                "dropped" to (stats?.numDropped ?: 0)
                            )
                        }
                        _userListState.value = _userListState.value.copy(counts = statsCache)
                    } catch (_: Exception) {
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    fun quickIncrement(animeId: Int) {
        if (userListState.value.username != null) return

        viewModelScope.launch {
            try {
                repository.quickIncrementAnime(animeId)
                statsCache = emptyMap()
                loadUserList(userListState.value.status, userListState.value.username, forceRefresh = true)
            } catch (e: Exception) {
            }
        }
    }

    fun getGridModeFlow(status: String) = prefsManager.getGridModeFlow("anime_$status")

    fun getSavedGridMode(status: String): Boolean = _savedGridModes.value[status] ?: false

    fun getSavedSortMode(status: String): String = _savedSorts.value[status] ?: "list_score"

    fun getRefreshGeneration(status: String, username: String? = null): Int {
        val effectiveUsername = if (username == "null") null else username
        val key = listOf(effectiveUsername ?: "@me", status).joinToString("|")
        return _refreshGenerations.value[key] ?: 0
    }

    fun refreshStatus(status: String, username: String? = null) {
        val effectiveUsername = if (username == "null") null else username
        val refreshKey = listOf(effectiveUsername ?: "@me", status).joinToString("|")
        _refreshGenerations.value = _refreshGenerations.value + (
            refreshKey to ((_refreshGenerations.value[refreshKey] ?: 0) + 1)
        )
        fullAnimeListCache.keys
            .filter { it.startsWith("${effectiveUsername ?: "@me"}|$status|") }
            .toList()
            .forEach {
                fullAnimeListCache.remove(it)
                _loadedLists.value = _loadedLists.value - it
            }
        if (userListState.value.status == status && userListState.value.username == effectiveUsername) {
            loadUserList(status, username = effectiveUsername, forceRefresh = true)
        }
    }

    fun clearSearch() {
        _searchState.value = UserAnimeSearchState()
    }

    fun searchUserList(query: String, status: String, username: String? = null, sort: String? = null) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            clearSearch()
            return
        }

        val effectiveUsername = if (username == "null") null else username
        val effectiveSort = sort ?: _savedSorts.value[status] ?: userListState.value.sort
        val cacheKey = listOf(effectiveUsername ?: "@me", status, effectiveSort).joinToString("|")

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _searchState.value = _searchState.value.copy(
                    query = normalizedQuery,
                    isLoading = true
                )

                val fullList = fullAnimeListCache.getOrPut(cacheKey) {
                    repository.getAllUserAnimeList(
                        username = effectiveUsername,
                        status = if (status == "all") null else status,
                        sort = effectiveSort
                    )
                }

                val lowerQuery = normalizedQuery.lowercase()
                val results = fullList.filter { item ->
                    item.node.matchesQuery(lowerQuery)
                }

                val missingIds = results
                    .asSequence()
                    .filter { it.node.status == "currently_airing" }
                    .map { it.node.id }
                    .filter { it !in _airingDetails.value.keys }
                    .toList()
                if (missingIds.isNotEmpty()) {
                    val airingDetails = repository.getAiringAnimeDetails(missingIds)
                        .filter { it.idMal != null }
                        .associateBy { it.idMal!! }
                    _airingDetails.value = _airingDetails.value + airingDetails
                }

                _searchState.value = UserAnimeSearchState(
                    query = normalizedQuery,
                    results = results,
                    isLoading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _searchState.value = _searchState.value.copy(isLoading = false)
            }
        }
    }

    private fun com.example.myapplication.data.model.AnimeNode.matchesQuery(query: String): Boolean {
        val titles = buildList {
            add(title)
            alternativeTitles?.en?.let(::add)
            alternativeTitles?.ja?.let(::add)
            alternativeTitles?.synonyms?.let { addAll(it) }
        }
        return titles.any { it.lowercase().contains(query) }
    }

    fun setGridMode(status: String, isGrid: Boolean, global: Boolean = false) {
        _savedGridModes.value = if (global) {
            animeStatuses.associateWith { isGrid }
        } else {
            _savedGridModes.value + (status to isGrid)
        }
    }
}

data class UserListState(
    val status: String = "all",
    val username: String? = null,
    val sort: String = "list_score",
    val counts: Map<String, Int> = emptyMap()
)

data class UserAnimeSearchState(
    val query: String = "",
    val results: List<UserAnimeData> = emptyList(),
    val isLoading: Boolean = false
)
