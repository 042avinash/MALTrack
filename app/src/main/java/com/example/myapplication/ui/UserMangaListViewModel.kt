package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserPreferencesManager
import com.example.myapplication.data.model.UserMangaData
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
class UserMangaListViewModel @Inject constructor(
    private val repository: AnimeRepository,
    private val prefsManager: UserPreferencesManager
) : ViewModel() {
    companion object {
        private const val SOFT_TIMEOUT_MS = 1_500L
        private val globalFullMangaListCache = mutableMapOf<String, List<UserMangaData>>()
        private val globalMangaListCacheComplete = mutableSetOf<String>()
        private val globalStatsCache = mutableMapOf<String, Map<String, Int>>()
    }

    private val mangaStatuses = listOf("all", "reading", "completed", "on_hold", "plan_to_read", "dropped")

    private val _userMangaListState = MutableStateFlow(UserMangaListState())
    val userMangaListState: StateFlow<UserMangaListState> = _userMangaListState.asStateFlow()
    private val _savedSorts = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _savedGridModes = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _refreshGenerations = MutableStateFlow<Map<String, Int>>(emptyMap())
    val refreshGenerations: StateFlow<Map<String, Int>> = _refreshGenerations.asStateFlow()
    private val _searchState = MutableStateFlow(UserMangaSearchState())
    val searchState: StateFlow<UserMangaSearchState> = _searchState.asStateFlow()
    private val _loadedLists = MutableStateFlow<Map<String, List<UserMangaData>>>(emptyMap())
    val loadedLists: StateFlow<Map<String, List<UserMangaData>>> = _loadedLists.asStateFlow()
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
        mangaStatuses.forEach { status ->
            viewModelScope.launch {
                val listKey = "manga_$status"
                val sort = prefsManager.getSortModeFlow(listKey).first()
                val grid = prefsManager.getGridModeFlow(listKey).first()

                _savedSorts.value = _savedSorts.value + (status to sort)
                _savedGridModes.value = _savedGridModes.value + (status to grid)
            }
        }
    }

    fun loadUserMangaList(status: String?, username: String? = null, sort: String? = null, forceRefresh: Boolean = false) {
        val statusKey = status ?: "all"
        val effectiveUsername = if (username == "null") null else username

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val savedSort = prefsManager.getSortModeFlow("manga_$statusKey").first()
                val effectiveSort = sort ?: savedSort
                val cacheKey = listOf(effectiveUsername ?: "@me", statusKey, effectiveSort).joinToString("|")
                val statusLoadingKey = listOf(effectiveUsername ?: "@me", statusKey).joinToString("|")

                _savedSorts.value = _savedSorts.value + (statusKey to effectiveSort)
                
                _userMangaListState.value = UserMangaListState(
                    status = statusKey,
                    username = effectiveUsername,
                    sort = effectiveSort
                )

                if (forceRefresh) {
                    backgroundFullLoadJobs.remove(cacheKey)?.cancel()
                    globalFullMangaListCache.remove(cacheKey)
                    globalMangaListCacheComplete.remove(cacheKey)
                    _loadedLists.value = _loadedLists.value - cacheKey
                }

                val cachedList = globalFullMangaListCache[cacheKey]
                if (cachedList != null) {
                    _loadedLists.value = _loadedLists.value + (cacheKey to cachedList)
                    if (!globalMangaListCacheComplete.contains(cacheKey)) {
                        launchFullListBackfill(
                            cacheKey = cacheKey,
                            statusKey = statusKey,
                            username = effectiveUsername,
                            sort = effectiveSort
                        )
                    }
                    _loadingStatuses.value = _loadingStatuses.value + (statusLoadingKey to false)
                } else {
                    _loadingStatuses.value = _loadingStatuses.value + (statusLoadingKey to true)
                    try {
                        val firstPageDeferred = async {
                            repository.getUserMangaList(
                                username = effectiveUsername,
                                status = if (statusKey == "all") null else statusKey,
                                sort = effectiveSort,
                                limit = 100
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
                                    globalFullMangaListCache[cacheKey] = resolvedFirstPage.data
                                    _loadedLists.value = _loadedLists.value + (cacheKey to resolvedFirstPage.data)
                                    if (resolvedFirstPage.paging.next == null) {
                                        globalMangaListCacheComplete.add(cacheKey)
                                    } else {
                                        val fullList = repository.getAllUserMangaList(
                                            username = effectiveUsername,
                                            status = if (statusKey == "all") null else statusKey,
                                            sort = effectiveSort
                                        )
                                        globalFullMangaListCache[cacheKey] = fullList
                                        globalMangaListCacheComplete.add(cacheKey)
                                        _loadedLists.value = _loadedLists.value + (cacheKey to fullList)
                                    }
                                }
                            }
                            return@launch
                        }
                        globalFullMangaListCache[cacheKey] = firstPage.data
                        _loadedLists.value = _loadedLists.value + (cacheKey to firstPage.data)
                        if (firstPage.paging.next == null) {
                            globalMangaListCacheComplete.add(cacheKey)
                        } else {
                            launchFullListBackfill(
                                cacheKey = cacheKey,
                                statusKey = statusKey,
                                username = effectiveUsername,
                                sort = effectiveSort
                            )
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
        if (globalMangaListCacheComplete.contains(cacheKey)) return
        backgroundFullLoadJobs[cacheKey]?.cancel()
        backgroundFullLoadJobs[cacheKey] = viewModelScope.launch {
            try {
                val fullList = repository.getAllUserMangaList(
                    username = username,
                    status = if (statusKey == "all") null else statusKey,
                    sort = sort
                )
                globalFullMangaListCache[cacheKey] = fullList
                globalMangaListCacheComplete.add(cacheKey)
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
                    _userMangaListState.value = _userMangaListState.value.copy(counts = cachedStats)
                } else {
                    try {
                        val profile = if (username != null) {
                            repository.getUserFullProfile(username).statistics?.manga?.let { jikan ->
                                globalStatsCache[statsCacheKey] = mapOf(
                                    "all" to jikan.total_entries,
                                    "reading" to jikan.reading,
                                    "completed" to jikan.completed,
                                    "on_hold" to jikan.on_hold,
                                    "plan_to_read" to jikan.plan_to_read,
                                    "dropped" to jikan.dropped
                                )
                            }
                            repository.getMyUserProfile()
                        } else {
                            repository.getMyUserProfile()
                        }

                        if (globalStatsCache[statsCacheKey].isNullOrEmpty() || username == null) {
                            val stats = profile.mangaStatistics
                            globalStatsCache[statsCacheKey] = mapOf(
                                "all" to (stats?.numItems ?: 0),
                                "reading" to (stats?.numReading ?: 0),
                                "completed" to (stats?.numCompleted ?: 0),
                                "on_hold" to (stats?.numOnHold ?: 0),
                                "plan_to_read" to (stats?.numPlanToRead ?: 0),
                                "dropped" to (stats?.numDropped ?: 0)
                            )
                        }
                        _userMangaListState.value = _userMangaListState.value.copy(counts = globalStatsCache[statsCacheKey].orEmpty())
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun quickIncrement(mangaId: Int) {
        if (userMangaListState.value.username != null) return

        viewModelScope.launch {
            try {
                repository.quickIncrementManga(mangaId)
                globalStatsCache.remove(userMangaListState.value.username ?: "@me")
                loadUserMangaList(userMangaListState.value.status, userMangaListState.value.username, forceRefresh = true)
            } catch (e: Exception) {
            }
        }
    }
    
    fun getGridModeFlow(status: String) = prefsManager.getGridModeFlow("manga_$status")

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
        _searchState.value = UserMangaSearchState()
        globalFullMangaListCache.keys
            .filter { it.startsWith("${effectiveUsername ?: "@me"}|$status|") }
            .toList()
            .forEach {
                backgroundFullLoadJobs.remove(it)?.cancel()
                globalFullMangaListCache.remove(it)
                globalMangaListCacheComplete.remove(it)
                _loadedLists.value = _loadedLists.value - it
            }
        _loadingStatuses.value = _loadingStatuses.value + (refreshKey to true)
        loadUserMangaList(status, username = effectiveUsername, forceRefresh = true)
    }

    fun clearSearch() {
        _searchState.value = UserMangaSearchState()
    }

    fun searchUserList(query: String, status: String, username: String? = null, sort: String? = null) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            clearSearch()
            return
        }

        val effectiveUsername = if (username == "null") null else username
        val effectiveSort = sort ?: _savedSorts.value[status] ?: userMangaListState.value.sort
        val cacheKey = listOf(effectiveUsername ?: "@me", status, effectiveSort).joinToString("|")

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _searchState.value = _searchState.value.copy(
                    query = normalizedQuery,
                    isLoading = true
                )

                if (!globalMangaListCacheComplete.contains(cacheKey)) {
                    backgroundFullLoadJobs[cacheKey]?.cancel()
                }
                val fullList = if (globalMangaListCacheComplete.contains(cacheKey)) {
                    globalFullMangaListCache[cacheKey].orEmpty()
                } else {
                    repository.getAllUserMangaList(
                        username = effectiveUsername,
                        status = if (status == "all") null else status,
                        sort = effectiveSort
                    ).also { fetched ->
                        globalFullMangaListCache[cacheKey] = fetched
                        globalMangaListCacheComplete.add(cacheKey)
                        _loadedLists.value = _loadedLists.value + (cacheKey to fetched)
                    }
                }

                val lowerQuery = normalizedQuery.lowercase()
                val results = fullList.filter { item ->
                    item.node.matchesQuery(lowerQuery)
                }

                _searchState.value = UserMangaSearchState(
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

    fun updateListStatus(
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
    ) {
        if (userMangaListState.value.username != null) return

        viewModelScope.launch {
            try {
                repository.updateMyMangaListStatus(
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
                globalStatsCache.remove(userMangaListState.value.username ?: "@me")
                loadUserMangaList(userMangaListState.value.status, userMangaListState.value.username, forceRefresh = true)
            } catch (_: Exception) {
            }
        }
    }

    private fun com.example.myapplication.data.model.MangaNode.matchesQuery(query: String): Boolean {
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
            mangaStatuses.associateWith { isGrid }
        } else {
            _savedGridModes.value + (status to isGrid)
        }
    }
}

data class UserMangaListState(
    val status: String = "all",
    val username: String? = null,
    val sort: String = "list_score",
    val counts: Map<String, Int> = emptyMap()
)

data class UserMangaSearchState(
    val query: String = "",
    val results: List<UserMangaData> = emptyList(),
    val isLoading: Boolean = false
)
