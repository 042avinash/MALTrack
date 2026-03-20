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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val prefsManager: UserPreferencesManager
) : ViewModel() {
    companion object {
        private const val SOFT_TIMEOUT_MS = 1_500L
        private const val INITIAL_PAGE_LIMIT = 40
        private val globalFullAnimeListCache = mutableMapOf<String, List<UserAnimeData>>()
        private val globalAnimeListCacheComplete = mutableSetOf<String>()
        private val globalStatsCache = mutableMapOf<String, Map<String, Int>>()
    }

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
    val recentSearches = prefsManager.recentSearchesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val nsfwEnabled = prefsManager.nsfwFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private val backgroundFullLoadJobs = mutableMapOf<String, Job>()

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
                    backgroundFullLoadJobs.remove(cacheKey)?.cancel()
                    globalFullAnimeListCache.remove(cacheKey)
                    globalAnimeListCacheComplete.remove(cacheKey)
                    _loadedLists.value = _loadedLists.value - cacheKey
                }

                val cachedList = globalFullAnimeListCache[cacheKey]
                if (cachedList != null) {
                    _loadedLists.value = _loadedLists.value + (cacheKey to cachedList)
                    _loadingStatuses.value = _loadingStatuses.value + (statusLoadingKey to false)
                    fetchMissingAiringDetails(cachedList)
                    if (!globalAnimeListCacheComplete.contains(cacheKey)) {
                        launchFullListBackfill(
                            cacheKey = cacheKey,
                            statusKey = statusKey,
                            username = effectiveUsername,
                            sort = effectiveSort
                        )
                    }
                } else {
                    _loadingStatuses.value = _loadingStatuses.value + (statusLoadingKey to true)
                    try {
                        val firstPageDeferred = async {
                            repository.getUserAnimeList(
                                username = effectiveUsername,
                                status = if (statusKey == "all") null else statusKey,
                                sort = effectiveSort,
                                limit = INITIAL_PAGE_LIMIT
                            )
                        }
                        val firstPage = withTimeoutOrNull(SOFT_TIMEOUT_MS) { firstPageDeferred.await() }
                        if (firstPage == null) {
                            launchStatsLoad(
                                username = effectiveUsername,
                                forceRefresh = forceRefresh
                            )
                            backgroundFullLoadJobs[cacheKey]?.cancel()
                            backgroundFullLoadJobs[cacheKey] = viewModelScope.launch {
                                runCatching {
                                    val resolvedFirstPage = firstPageDeferred.await()
                                    globalFullAnimeListCache[cacheKey] = resolvedFirstPage.data
                                    _loadedLists.value = _loadedLists.value + (cacheKey to resolvedFirstPage.data)
                                    if (resolvedFirstPage.paging.next == null) {
                                        globalAnimeListCacheComplete.add(cacheKey)
                                    } else {
                                        val fullList = repository.getAllUserAnimeList(
                                            username = effectiveUsername,
                                            status = if (statusKey == "all") null else statusKey,
                                            sort = effectiveSort
                                        )
                                        globalFullAnimeListCache[cacheKey] = fullList
                                        globalAnimeListCacheComplete.add(cacheKey)
                                        _loadedLists.value = _loadedLists.value + (cacheKey to fullList)
                                    }
                                }
                            }
                            return@launch
                        }
                        globalFullAnimeListCache[cacheKey] = firstPage.data
                        _loadedLists.value = _loadedLists.value + (cacheKey to firstPage.data)
                        if (firstPage.paging.next == null) {
                            globalAnimeListCacheComplete.add(cacheKey)
                        } else {
                            launchFullListBackfill(
                                cacheKey = cacheKey,
                                statusKey = statusKey,
                                username = effectiveUsername,
                                sort = effectiveSort
                            )
                        }

                        val malIds = firstPage.data
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

                launchStatsLoad(
                    username = effectiveUsername,
                    forceRefresh = forceRefresh
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    private fun launchFullListBackfill(
        cacheKey: String,
        statusKey: String,
        username: String?,
        sort: String
    ) {
        if (globalAnimeListCacheComplete.contains(cacheKey)) return
        backgroundFullLoadJobs[cacheKey]?.cancel()
        backgroundFullLoadJobs[cacheKey] = viewModelScope.launch {
            try {
                val fullList = repository.getAllUserAnimeList(
                    username = username,
                    status = if (statusKey == "all") null else statusKey,
                    sort = sort
                )
                globalFullAnimeListCache[cacheKey] = fullList
                globalAnimeListCacheComplete.add(cacheKey)
                _loadedLists.value = _loadedLists.value + (cacheKey to fullList)
            } catch (_: Exception) {
            }
        }
    }

    private fun launchStatsLoad(username: String?, forceRefresh: Boolean) {
        viewModelScope.launch {
            try {
                val statsCacheKey = username ?: "@me"
                val cachedStats = globalStatsCache[statsCacheKey]
                if (cachedStats != null && !forceRefresh) {
                    _userListState.value = _userListState.value.copy(counts = cachedStats)
                } else {
                    try {
                        val profile = if (username != null) {
                            repository.getUserFullProfile(username).statistics?.anime?.let { jikan ->
                                globalStatsCache[statsCacheKey] = mapOf(
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

                        if (globalStatsCache[statsCacheKey].isNullOrEmpty() || username == null) {
                            val stats = profile.animeStatistics
                            globalStatsCache[statsCacheKey] = mapOf(
                                "all" to (stats?.numItems ?: 0),
                                "watching" to (stats?.numWatching ?: 0),
                                "completed" to (stats?.numCompleted ?: 0),
                                "on_hold" to (stats?.numOnHold ?: 0),
                                "plan_to_watch" to (stats?.numPlanToWatch ?: 0),
                                "dropped" to (stats?.numDropped ?: 0)
                            )
                        }
                        _userListState.value = _userListState.value.copy(counts = globalStatsCache[statsCacheKey].orEmpty())
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun quickIncrement(animeId: Int) {
        if (userListState.value.username != null) return

        viewModelScope.launch {
            try {
                repository.quickIncrementAnime(animeId)
                globalStatsCache.remove(userListState.value.username ?: "@me")
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
        globalStatsCache.remove(effectiveUsername ?: "@me")
        _searchState.value = UserAnimeSearchState()
        globalFullAnimeListCache.keys
            .filter { it.startsWith("${effectiveUsername ?: "@me"}|$status|") }
            .toList()
            .forEach {
                backgroundFullLoadJobs.remove(it)?.cancel()
                globalFullAnimeListCache.remove(it)
                globalAnimeListCacheComplete.remove(it)
                _loadedLists.value = _loadedLists.value - it
            }
        _loadingStatuses.value = _loadingStatuses.value + (refreshKey to true)
        loadUserList(status, username = effectiveUsername, forceRefresh = true)
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

                if (!globalAnimeListCacheComplete.contains(cacheKey)) {
                    backgroundFullLoadJobs[cacheKey]?.cancel()
                }
                val fullList = if (globalAnimeListCacheComplete.contains(cacheKey)) {
                    globalFullAnimeListCache[cacheKey].orEmpty()
                } else {
                    repository.getAllUserAnimeList(
                        username = effectiveUsername,
                        status = if (status == "all") null else status,
                        sort = effectiveSort
                    ).also { fetched ->
                        globalFullAnimeListCache[cacheKey] = fetched
                        globalAnimeListCacheComplete.add(cacheKey)
                        _loadedLists.value = _loadedLists.value + (cacheKey to fetched)
                    }
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
                _searchState.value = _searchState.value.copy(isLoading = false)
                throw e
            } catch (_: Exception) {
                _searchState.value = _searchState.value.copy(isLoading = false)
            }
        }
    }

    fun saveRecentSearch(query: String) {
        val cleaned = query.trim()
        if (cleaned.length < 2) return
        viewModelScope.launch {
            prefsManager.saveRecentSearch(cleaned)
        }
    }

    private suspend fun fetchMissingAiringDetails(entries: List<UserAnimeData>) {
        val missingIds = entries
            .asSequence()
            .filter { it.node.status == "currently_airing" }
            .map { it.node.id }
            .filter { it !in _airingDetails.value.keys }
            .toList()

        if (missingIds.isEmpty()) return

        val airingDetails = repository.getAiringAnimeDetails(missingIds)
            .filter { it.idMal != null }
            .associateBy { it.idMal!! }
        _airingDetails.value = _airingDetails.value + airingDetails
    }

    fun updateListStatus(
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
    ) {
        if (userListState.value.username != null) return

        viewModelScope.launch {
            try {
                repository.updateMyListStatus(
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
                globalStatsCache.remove(userListState.value.username ?: "@me")
                loadUserList(userListState.value.status, userListState.value.username, forceRefresh = true)
            } catch (_: Exception) {
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
