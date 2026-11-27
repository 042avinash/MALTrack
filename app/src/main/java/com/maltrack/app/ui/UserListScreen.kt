package com.maltrack.app.ui

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.maltrack.app.data.local.TitleLanguage
import com.maltrack.app.data.local.getPreferredTitle
import com.maltrack.app.data.model.AniListMedia
import com.maltrack.app.data.model.ListStatus
import com.maltrack.app.data.model.MangaListStatus
import com.maltrack.app.data.model.MyListStatus
import com.maltrack.app.data.model.MyMangaListStatus
import com.maltrack.app.data.model.UserAnimeData
import com.maltrack.app.data.model.UserMangaData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(
    animeViewModel: UserListViewModel,
    mangaViewModel: UserMangaListViewModel,
    titleLanguage: TitleLanguage,
    initialMainTab: Int = 0,
    initialSubTab: Int = 0,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHomeSearch: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    var isAnimeView by rememberSaveable(initialMainTab) {
        mutableStateOf(initialMainTab == 0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAnimeView) {
            UserAnimeSection(
                viewModel = animeViewModel,
                titleLanguage = titleLanguage,
                initialSubTab = initialSubTab,
                onAnimeClick = onAnimeClick,
                onTypeSwitch = {
                    isAnimeView = false
                    Toast.makeText(context, "Viewing Manga list", Toast.LENGTH_SHORT).show()
                },
                onOpenSettings = onOpenSettings,
                onOpenHomeSearch = { query -> onOpenHomeSearch(query, true) }
            )
        } else {
            UserMangaSection(
                viewModel = mangaViewModel,
                titleLanguage = titleLanguage,
                initialSubTab = initialSubTab,
                onMangaClick = onMangaClick,
                onTypeSwitch = {
                    isAnimeView = true
                    Toast.makeText(context, "Viewing Anime list", Toast.LENGTH_SHORT).show()
                },
                onOpenSettings = onOpenSettings,
                onOpenHomeSearch = { query -> onOpenHomeSearch(query, false) }
            )
        }
    }
}

@Composable
fun ListActionToolbar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandChange: (Boolean) -> Unit,
    onSearchClear: () -> Unit,
    onSearchSubmit: () -> Unit,
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onOpenSettings: () -> Unit,
    isAnime: Boolean,
    onTypeSwitch: () -> Unit
) {
    val listOwnerLabel = "Your"
    val mediaLabel = if (isAnime) "Anime" else "Manga"
    val filteredRecent = remember(searchQuery, recentSearches) {
        val query = searchQuery.trim()
        recentSearches
            .filter { query.isBlank() || it.contains(query, ignoreCase = true) }
            .take(4)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        placeholder = { Text("Search $listOwnerLabel $mediaLabel List...", fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                        trailingIcon = {
                            IconButton(onClick = {
                                onSearchQueryChange("")
                                onSearchExpandChange(false)
                                onSearchClear()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                } else {
                    Button(
                        onClick = onTypeSwitch,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            if (isAnime) Icons.Default.Movie else Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAnime) "Anime" else "Manga", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { onSearchExpandChange(true) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Open Settings")
                    }
                }
            }

            if (isSearchExpanded && filteredRecent.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredRecent.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onRecentSearchSelected(item) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListStatusCountRow(
    status: String,
    count: Int?,
    showNsfwCount: Boolean,
    nsfwEnabled: Boolean,
    nsfwCount: Int?,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    onSortClick: () -> Unit
) {
    val label = when (status) {
        "all" -> "All"
        "watching" -> "Watching"
        "reading" -> "Reading"
        "completed" -> "Completed"
        "on_hold" -> "On-Hold"
        "plan_to_watch", "plan_to_read" -> "Planned"
        "dropped" -> "Dropped"
        else -> status.replace("_", " ").replaceFirstChar { it.uppercase() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (showNsfwCount) {
                "$label: ${count ?: "…"} · ${if (nsfwEnabled) "NSFW: ${nsfwCount ?: "…"}" else "NSFW titles are hidden"}"
            } else {
                "$label: ${count ?: "…"}"
            },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onSortClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Sort, contentDescription = "Sort list", modifier = Modifier.size(18.dp))
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onToggleView),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                contentDescription = if (isGridView) "Switch to list view" else "Switch to tile view",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserAnimeSection(
    viewModel: UserListViewModel,
    titleLanguage: TitleLanguage,
    initialSubTab: Int,
    onAnimeClick: (Int) -> Unit,
    onTypeSwitch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHomeSearch: (String) -> Unit
) {
    val userListState by viewModel.userListState.collectAsState()
    val airingDetails by viewModel.airingDetails.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val loadedLists by viewModel.loadedLists.collectAsState()
    val completeListKeys by viewModel.completeListKeys.collectAsState()
    val loadingStatuses by viewModel.loadingStatuses.collectAsState()
    val nsfwEnabled by viewModel.nsfwEnabled.collectAsState()
    val showNsfwCount by viewModel.showNsfwCount.collectAsState()

    val statuses = listOf("all", "watching", "completed", "on_hold", "plan_to_watch", "dropped")
    val statusIcons = listOf(
        null,
        Icons.Default.Visibility,
        Icons.Default.CheckCircle,
        Icons.Default.PauseCircle,
        Icons.Default.Schedule,
        Icons.Default.Cancel
    )

    val pagerState = rememberPagerState(initialPage = initialSubTab) { statuses.size }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var submittedSearchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var isSearchTransitionLoading by remember { mutableStateOf(false) }
    var isTabTransitionLoading by rememberSaveable { mutableStateOf(false) }
    var loadedStatuses by rememberSaveable { mutableStateOf(setOf<String>()) }
    var pendingAnimeEdit by remember { mutableStateOf<UserAnimeData?>(null) }
    var isPullRefreshing by rememberSaveable { mutableStateOf(false) }
    var pullRefreshRequestToken by rememberSaveable { mutableStateOf(0) }

    val settledPage = pagerState.settledPage
    val activePage = pagerState.currentPage
    val currentStatus = statuses[activePage]
    val defaultIsGridView by viewModel.getGridModeFlow().collectAsState(initial = false)
    val pageGridModes = remember { mutableStateMapOf<String, Boolean>() }
    val isGridView = pageGridModes[currentStatus] ?: defaultIsGridView
    val currentSort = if (userListState.status == currentStatus) userListState.sort else viewModel.getSavedSortMode(currentStatus)

    val effectiveUsername = "@me"
    val refreshLoadingKey = listOf(effectiveUsername, currentStatus).joinToString("|")
    val currentStatusLoadKey = "$currentStatus|$nsfwEnabled"
    val currentStatusItems = loadedLists.entries.firstOrNull {
        it.key.startsWith("$effectiveUsername|$currentStatus|") &&
            it.key.endsWith("|$nsfwEnabled") &&
            it.key in completeListKeys
    }?.value
    val currentStatusCount = currentStatusItems?.size
    val currentStatusNsfwCount = currentStatusItems?.count { item ->
        item.node.nsfw?.lowercase() in setOf("gray", "black")
    }

    LaunchedEffect(pullRefreshRequestToken) {
        if (pullRefreshRequestToken <= 0) return@LaunchedEffect
        isPullRefreshing = true
        viewModel.refreshStatus(currentStatus)
    }

    LaunchedEffect(isPullRefreshing, loadingStatuses, refreshLoadingKey) {
        if (isPullRefreshing && loadingStatuses[refreshLoadingKey] != true) {
            isPullRefreshing = false
        }
    }

    LaunchedEffect(settledPage, nsfwEnabled) {
        if (currentStatusLoadKey !in loadedStatuses) {
            isTabTransitionLoading = true
            viewModel.loadUserList(currentStatus, forceRefresh = false)
        } else {
            isTabTransitionLoading = false
        }
    }

    LaunchedEffect(Unit) {
        searchQuery = ""
        submittedSearchQuery = ""
        isSearchExpanded = false
        viewModel.clearSearch()
    }

    LaunchedEffect(submittedSearchQuery, currentStatus, userListState.sort) {
        val query = submittedSearchQuery.trim()
        if (query.isBlank()) {
            isSearchTransitionLoading = false
            viewModel.clearSearch()
        } else {
            viewModel.searchUserList(
                query = query,
                status = currentStatus,
                sort = userListState.sort
            )
        }
    }

    LaunchedEffect(searchState.isLoading, searchState.query, submittedSearchQuery) {
        val query = submittedSearchQuery.trim()
        if (query.isBlank()) {
            isSearchTransitionLoading = false
        } else if (!searchState.isLoading && searchState.query == query) {
            isSearchTransitionLoading = false
        }
    }

    LaunchedEffect(isSearchTransitionLoading, searchQuery) {
        if (isSearchTransitionLoading && searchQuery.isNotBlank()) {
            delay(1500)
            isSearchTransitionLoading = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                ListActionToolbar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    isSearchExpanded = isSearchExpanded,
                    onSearchExpandChange = { isSearchExpanded = it },
                    onSearchClear = {
                        submittedSearchQuery = ""
                        isSearchTransitionLoading = false
                        viewModel.clearSearch()
                    },
                    onSearchSubmit = {
                        val query = searchQuery.trim()
                        if (query.isBlank()) {
                            submittedSearchQuery = ""
                            isSearchTransitionLoading = false
                            viewModel.clearSearch()
                        } else {
                            submittedSearchQuery = query
                            isSearchTransitionLoading = true
                            viewModel.saveRecentSearch(query)
                        }
                    },
                    recentSearches = recentSearches,
                    onRecentSearchSelected = {
                        searchQuery = it
                        submittedSearchQuery = it
                        isSearchTransitionLoading = true
                    },
                    onOpenSettings = onOpenSettings,
                    isAnime = true,
                    onTypeSwitch = onTypeSwitch
                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                ) {
                    statuses.forEachIndexed { index, _ ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                if (index == 0) {
                                    Icon(painterResource(com.maltrack.app.R.drawable.ic_all_dashboard), contentDescription = null, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(statusIcons[index]!!, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }
                        )
                    }
                }
                ListStatusCountRow(
                    status = currentStatus,
                    count = currentStatusCount,
                    showNsfwCount = showNsfwCount,
                    nsfwEnabled = nsfwEnabled,
                    nsfwCount = currentStatusNsfwCount,
                    isGridView = isGridView,
                    onToggleView = { pageGridModes[currentStatus] = !isGridView },
                    onSortClick = { showSortDialog = true }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = { pullRefreshRequestToken += 1 },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1
            ) { page ->
                val pageStatus = statuses[page]
                val pageSort = if (pageStatus == userListState.status) userListState.sort else null
                val pageIsGridView = pageGridModes[pageStatus] ?: defaultIsGridView
                val pageListState = rememberSaveable(
                    key = "anime_list_$pageStatus",
                    saver = LazyListState.Saver
                ) { LazyListState() }
                val pageGridState = rememberSaveable(
                    key = "anime_grid_$pageStatus",
                    saver = LazyGridState.Saver
                ) { LazyGridState() }
                val pageCacheKey = listOf(effectiveUsername, pageStatus, pageSort ?: viewModel.getSavedSortMode(pageStatus), nsfwEnabled).joinToString("|")
                val pageItems = loadedLists[pageCacheKey]
                    ?: loadedLists.entries.firstOrNull {
                        it.key.startsWith("$effectiveUsername|$pageStatus|") && it.key.endsWith("|$nsfwEnabled")
                    }?.value
                    ?: emptyList()
                val pageHasLoadedData = loadedLists.containsKey(pageCacheKey) || loadedLists.keys.any {
                    it.startsWith("$effectiveUsername|$pageStatus|") && it.endsWith("|$nsfwEnabled")
                }
                val pageIsLoading = loadingStatuses[listOf(effectiveUsername, pageStatus).joinToString("|")] == true

                if (page == settledPage && !pageIsLoading) {
                    LaunchedEffect(pageIsLoading, pageHasLoadedData, settledPage, userListState.status) {
                        if (userListState.status == currentStatus) {
                            if (pageHasLoadedData) {
                                loadedStatuses = loadedStatuses + currentStatusLoadKey
                            }
                            isTabTransitionLoading = false
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    UserAnimeList(
                        listKey = "anime_$pageStatus",
                        animeList = pageItems,
                        airingDetails = airingDetails,
                        titleLanguage = titleLanguage,
                        currentStatus = pageStatus,
                        searchQuery = if (page == activePage) submittedSearchQuery else "",
                        searchResults = if (page == activePage) searchState.results else emptyList(),
                        isSearchLoading = page == activePage && submittedSearchQuery.isNotBlank() && (searchState.isLoading || isSearchTransitionLoading),
                        isGridView = pageIsGridView,
                        onSwitchToAll = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onSearchOnMal = onOpenHomeSearch,
                        onSwitchToManga = onTypeSwitch,
                        onAnimeClick = onAnimeClick,
                        onAnimePlusOne = { item -> viewModel.quickIncrement(item.node.id) },
                        onAnimeEdit = { item -> pendingAnimeEdit = item },
                        listState = pageListState,
                        gridState = pageGridState
                    )

                    if (
                        page == activePage && (
                            isTabTransitionLoading ||
                                pageIsLoading ||
                                (submittedSearchQuery.isNotBlank() && (searchState.isLoading || isSearchTransitionLoading))
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            UserListLoadingShimmer(isGridView = pageIsGridView, isManga = false)
                        }
                    }
                }
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            title = "Sort Anime List",
            currentValue = currentSort,
            onDismiss = { showSortDialog = false },
            onSelect = { sort ->
                val mediaSort = when (sort) {
                    "title" -> "anime_title"
                    "start_date" -> "anime_start_date"
                    else -> sort
                }
                viewModel.loadUserList(currentStatus, sort = mediaSort)
                showSortDialog = false
            }
        )
    }

    pendingAnimeEdit?.let { item ->
        EditListStatusDialog(
            currentStatus = item.listStatus.toMyListStatus(),
            maxEpisodes = item.node.numEpisodes ?: 0,
            onDismiss = { pendingAnimeEdit = null },
            onSave = { status, isRewatching, score, eps, priority, timesRewatched, rewatchVal, tags, comments, start, finish ->
                viewModel.updateListStatus(
                    animeId = item.node.id,
                    status = status,
                    isRewatching = isRewatching,
                    score = score,
                    numWatchedEpisodes = eps,
                    priority = priority,
                    numTimesRewatched = timesRewatched,
                    rewatchValue = rewatchVal,
                    tags = tags,
                    comments = comments,
                    startDate = start,
                    finishDate = finish
                )
                pendingAnimeEdit = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserMangaSection(
    viewModel: UserMangaListViewModel,
    titleLanguage: TitleLanguage,
    initialSubTab: Int,
    onMangaClick: (Int) -> Unit,
    onTypeSwitch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHomeSearch: (String) -> Unit
) {
    val userMangaListState by viewModel.userMangaListState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val loadedLists by viewModel.loadedLists.collectAsState()
    val completeListKeys by viewModel.completeListKeys.collectAsState()
    val loadingStatuses by viewModel.loadingStatuses.collectAsState()
    val nsfwEnabled by viewModel.nsfwEnabled.collectAsState()
    val showNsfwCount by viewModel.showNsfwCount.collectAsState()

    val statuses = listOf("all", "reading", "completed", "on_hold", "plan_to_read", "dropped")
    val statusIcons = listOf(
        null,
        Icons.Default.Visibility,
        Icons.Default.CheckCircle,
        Icons.Default.PauseCircle,
        Icons.Default.Schedule,
        Icons.Default.Cancel
    )

    val pagerState = rememberPagerState(initialPage = initialSubTab) { statuses.size }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var submittedSearchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var isSearchTransitionLoading by remember { mutableStateOf(false) }
    var isTabTransitionLoading by rememberSaveable { mutableStateOf(false) }
    var loadedStatuses by rememberSaveable { mutableStateOf(setOf<String>()) }
    var pendingMangaEdit by remember { mutableStateOf<UserMangaData?>(null) }
    var isPullRefreshing by rememberSaveable { mutableStateOf(false) }
    var pullRefreshRequestToken by rememberSaveable { mutableStateOf(0) }

    val settledPage = pagerState.settledPage
    val activePage = pagerState.currentPage
    val currentStatus = statuses[activePage]
    val defaultIsGridView by viewModel.getGridModeFlow().collectAsState(initial = false)
    val pageGridModes = remember { mutableStateMapOf<String, Boolean>() }
    val isGridView = pageGridModes[currentStatus] ?: defaultIsGridView
    val currentSort = if (userMangaListState.status == currentStatus) userMangaListState.sort else viewModel.getSavedSortMode(currentStatus)

    val effectiveUsername = "@me"
    val refreshLoadingKey = listOf(effectiveUsername, currentStatus).joinToString("|")
    val currentStatusLoadKey = "$currentStatus|$nsfwEnabled"
    val currentStatusItems = loadedLists.entries.firstOrNull {
        it.key.startsWith("$effectiveUsername|$currentStatus|") &&
            it.key.endsWith("|$nsfwEnabled") &&
            it.key in completeListKeys
    }?.value
    val currentStatusCount = currentStatusItems?.size
    val currentStatusNsfwCount = currentStatusItems?.count { item ->
        item.node.nsfw?.lowercase() in setOf("gray", "black")
    }

    LaunchedEffect(pullRefreshRequestToken) {
        if (pullRefreshRequestToken <= 0) return@LaunchedEffect
        isPullRefreshing = true
        viewModel.refreshStatus(currentStatus)
    }

    LaunchedEffect(isPullRefreshing, loadingStatuses, refreshLoadingKey) {
        if (isPullRefreshing && loadingStatuses[refreshLoadingKey] != true) {
            isPullRefreshing = false
        }
    }

    LaunchedEffect(settledPage, nsfwEnabled) {
        if (currentStatusLoadKey !in loadedStatuses) {
            isTabTransitionLoading = true
            viewModel.loadUserMangaList(currentStatus, forceRefresh = false)
        } else {
            isTabTransitionLoading = false
        }
    }

    LaunchedEffect(Unit) {
        searchQuery = ""
        submittedSearchQuery = ""
        isSearchExpanded = false
        viewModel.clearSearch()
    }

    LaunchedEffect(submittedSearchQuery, currentStatus, userMangaListState.sort) {
        val query = submittedSearchQuery.trim()
        if (query.isBlank()) {
            isSearchTransitionLoading = false
            viewModel.clearSearch()
        } else {
            viewModel.searchUserList(
                query = query,
                status = currentStatus,
                sort = userMangaListState.sort
            )
        }
    }

    LaunchedEffect(searchState.isLoading, searchState.query, submittedSearchQuery) {
        val query = submittedSearchQuery.trim()
        if (query.isBlank()) {
            isSearchTransitionLoading = false
        } else if (!searchState.isLoading && searchState.query == query) {
            isSearchTransitionLoading = false
        }
    }

    LaunchedEffect(isSearchTransitionLoading, searchQuery) {
        if (isSearchTransitionLoading && searchQuery.isNotBlank()) {
            delay(1500)
            isSearchTransitionLoading = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                ListActionToolbar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    isSearchExpanded = isSearchExpanded,
                    onSearchExpandChange = { isSearchExpanded = it },
                    onSearchClear = {
                        submittedSearchQuery = ""
                        isSearchTransitionLoading = false
                        viewModel.clearSearch()
                    },
                    onSearchSubmit = {
                        val query = searchQuery.trim()
                        if (query.isBlank()) {
                            submittedSearchQuery = ""
                            isSearchTransitionLoading = false
                            viewModel.clearSearch()
                        } else {
                            submittedSearchQuery = query
                            isSearchTransitionLoading = true
                            viewModel.saveRecentSearch(query)
                        }
                    },
                    recentSearches = recentSearches,
                    onRecentSearchSelected = {
                        searchQuery = it
                        submittedSearchQuery = it
                        isSearchTransitionLoading = true
                    },
                    onOpenSettings = onOpenSettings,
                    isAnime = false,
                    onTypeSwitch = onTypeSwitch
                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                ) {
                    statuses.forEachIndexed { index, _ ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                if (index == 0) {
                                    Icon(painterResource(com.maltrack.app.R.drawable.ic_all_dashboard), contentDescription = null, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(statusIcons[index]!!, contentDescription = null, modifier = Modifier.size(24.dp))
                                }
                            }
                        )
                    }
                }
                ListStatusCountRow(
                    status = currentStatus,
                    count = currentStatusCount,
                    showNsfwCount = showNsfwCount,
                    nsfwEnabled = nsfwEnabled,
                    nsfwCount = currentStatusNsfwCount,
                    isGridView = isGridView,
                    onToggleView = { pageGridModes[currentStatus] = !isGridView },
                    onSortClick = { showSortDialog = true }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = { pullRefreshRequestToken += 1 },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1
            ) { page ->
                val pageStatus = statuses[page]
                val pageSort = if (pageStatus == userMangaListState.status) userMangaListState.sort else null
                val pageIsGridView = pageGridModes[pageStatus] ?: defaultIsGridView
                val pageListState = rememberSaveable(
                    key = "manga_list_$pageStatus",
                    saver = LazyListState.Saver
                ) { LazyListState() }
                val pageGridState = rememberSaveable(
                    key = "manga_grid_$pageStatus",
                    saver = LazyGridState.Saver
                ) { LazyGridState() }
                val pageCacheKey = listOf(effectiveUsername, pageStatus, pageSort ?: viewModel.getSavedSortMode(pageStatus), nsfwEnabled).joinToString("|")
                val pageItems = loadedLists[pageCacheKey]
                    ?: loadedLists.entries.firstOrNull {
                        it.key.startsWith("$effectiveUsername|$pageStatus|") && it.key.endsWith("|$nsfwEnabled")
                    }?.value
                    ?: emptyList()
                val pageHasLoadedData = loadedLists.containsKey(pageCacheKey) || loadedLists.keys.any {
                    it.startsWith("$effectiveUsername|$pageStatus|") && it.endsWith("|$nsfwEnabled")
                }
                val pageIsLoading = loadingStatuses[listOf(effectiveUsername, pageStatus).joinToString("|")] == true

                if (page == settledPage && !pageIsLoading) {
                    LaunchedEffect(pageIsLoading, pageHasLoadedData, settledPage, userMangaListState.status) {
                        if (userMangaListState.status == currentStatus) {
                            if (pageHasLoadedData) {
                                loadedStatuses = loadedStatuses + currentStatusLoadKey
                            }
                            isTabTransitionLoading = false
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    UserMangaList(
                        listKey = "manga_$pageStatus",
                        mangaList = pageItems,
                        titleLanguage = titleLanguage,
                        currentStatus = pageStatus,
                        searchQuery = if (page == activePage) submittedSearchQuery else "",
                        searchResults = if (page == activePage) searchState.results else emptyList(),
                        isSearchLoading = page == activePage && submittedSearchQuery.isNotBlank() && (searchState.isLoading || isSearchTransitionLoading),
                        isGridView = pageIsGridView,
                        onSwitchToAll = {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onSearchOnMal = onOpenHomeSearch,
                        onSwitchToAnime = onTypeSwitch,
                        onMangaClick = onMangaClick,
                        onMangaPlusOne = { item -> viewModel.quickIncrement(item.node.id) },
                        onMangaEdit = { item -> pendingMangaEdit = item },
                        listState = pageListState,
                        gridState = pageGridState
                    )

                    if (
                        page == activePage && (
                            isTabTransitionLoading ||
                                pageIsLoading ||
                                (submittedSearchQuery.isNotBlank() && (searchState.isLoading || isSearchTransitionLoading))
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            UserListLoadingShimmer(isGridView = pageIsGridView, isManga = true)
                        }
                    }
                }
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            title = "Sort Manga List",
            currentValue = currentSort,
            onDismiss = { showSortDialog = false },
            onSelect = { sort ->
                val mediaSort = when (sort) {
                    "title" -> "manga_title"
                    "start_date" -> "manga_start_date"
                    else -> sort
                }
                viewModel.loadUserMangaList(currentStatus, sort = mediaSort)
                showSortDialog = false
            }
        )
    }

    pendingMangaEdit?.let { item ->
        EditMangaListStatusDialog(
            currentStatus = item.listStatus.toMyMangaListStatus(),
            maxVolumes = item.node.numVolumes ?: 0,
            maxChapters = item.node.numChapters ?: 0,
            onDismiss = { pendingMangaEdit = null },
            onSave = { status, isRereading, score, vols, chaps, priority, timesReread, rereadVal, tags, comments, start, finish ->
                viewModel.updateListStatus(
                    mangaId = item.node.id,
                    status = status,
                    isRereading = isRereading,
                    score = score,
                    numVolumesRead = vols,
                    numChaptersRead = chaps,
                    priority = priority,
                    numTimesReread = timesReread,
                    rereadValue = rereadVal,
                    tags = tags,
                    comments = comments,
                    startDate = start,
                    finishDate = finish
                )
                pendingMangaEdit = null
            }
        )
    }
}

@Composable
private fun UserListSearchEmptyState(
    searchQuery: String,
    onSwitchToAll: () -> Unit,
    onSearchOnMal: (String) -> Unit,
    onSwitchMedia: () -> Unit,
    switchMediaLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("No matches found")
        Button(onClick = onSwitchToAll) { Text("Search in All") }
        Button(onClick = { onSearchOnMal(searchQuery) }) { Text("Search on MAL") }
        Button(onClick = onSwitchMedia) { Text(switchMediaLabel) }
    }
}

@Composable
fun UserAnimeList(
    listKey: String,
    animeList: List<UserAnimeData>,
    airingDetails: Map<Int, AniListMedia>,
    titleLanguage: TitleLanguage,
    currentStatus: String,
    searchQuery: String,
    searchResults: List<UserAnimeData>,
    isSearchLoading: Boolean,
    isGridView: Boolean,
    onSwitchToAll: () -> Unit,
    onSearchOnMal: (String) -> Unit,
    onSwitchToManga: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onAnimePlusOne: (UserAnimeData) -> Unit,
    onAnimeEdit: (UserAnimeData) -> Unit,
    listState: LazyListState,
    gridState: LazyGridState
) {
    val nowEpochSeconds by rememberCurrentEpochSeconds()
    val isSearching = searchQuery.isNotBlank()

    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(gridState),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        UserListSearchEmptyState(
                            searchQuery = searchQuery,
                            onSwitchToAll = onSwitchToAll,
                            onSearchOnMal = onSearchOnMal,
                            onSwitchMedia = onSwitchToManga,
                            switchMediaLabel = "Switch to Manga"
                        )
                    }
                } else {
                    items(
                        count = searchResults.size,
                        key = { index -> searchResults[index].node.id }
                    ) { index ->
                        val item = searchResults[index]
                        val anilistMedia = airingDetails[item.node.id]
                        UserAnimeGridItem(
                            data = item,
                            anilistMedia = anilistMedia,
                            nowEpochSeconds = nowEpochSeconds,
                            titleLanguage = titleLanguage,
                            onClick = { onAnimeClick(item.node.id) },
                            onPlusOne = { onAnimePlusOne(item) },
                            onEditStatus = { onAnimeEdit(item) }
                        )
                    }
                }
            } else {
                items(
                    count = animeList.size,
                    key = { index -> animeList[index].node.id }
                ) { index ->
                    val item = animeList[index]
                    val anilistMedia = airingDetails[item.node.id]
                    UserAnimeGridItem(
                        data = item,
                        anilistMedia = anilistMedia,
                        nowEpochSeconds = nowEpochSeconds,
                        titleLanguage = titleLanguage,
                        onClick = { onAnimeClick(item.node.id) },
                        onPlusOne = { onAnimePlusOne(item) },
                        onEditStatus = { onAnimeEdit(item) }
                    )
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(listState),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item {
                        UserListSearchEmptyState(
                            searchQuery = searchQuery,
                            onSwitchToAll = onSwitchToAll,
                            onSearchOnMal = onSearchOnMal,
                            onSwitchMedia = onSwitchToManga,
                            switchMediaLabel = "Switch to Manga"
                        )
                    }
                } else {
                    items(
                        count = searchResults.size,
                        key = { index -> searchResults[index].node.id }
                    ) { index ->
                        val item = searchResults[index]
                        val anilistMedia = airingDetails[item.node.id]
                        UserAnimeItem(
                            data = item,
                            anilistMedia = anilistMedia,
                            nowEpochSeconds = nowEpochSeconds,
                            titleLanguage = titleLanguage,
                            onClick = { onAnimeClick(item.node.id) },
                            onPlusOne = { onAnimePlusOne(item) },
                            onEditStatus = { onAnimeEdit(item) }
                        )
                    }
                }
            } else {
                items(
                    count = animeList.size,
                    key = { index -> animeList[index].node.id }
                ) { index ->
                    val item = animeList[index]
                    val anilistMedia = airingDetails[item.node.id]
                    UserAnimeItem(
                        data = item,
                        anilistMedia = anilistMedia,
                        nowEpochSeconds = nowEpochSeconds,
                        titleLanguage = titleLanguage,
                        onClick = { onAnimeClick(item.node.id) },
                        onPlusOne = { onAnimePlusOne(item) },
                        onEditStatus = { onAnimeEdit(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserMangaList(
    listKey: String,
    mangaList: List<UserMangaData>,
    titleLanguage: TitleLanguage,
    currentStatus: String,
    searchQuery: String,
    searchResults: List<UserMangaData>,
    isSearchLoading: Boolean,
    isGridView: Boolean,
    onSwitchToAll: () -> Unit,
    onSearchOnMal: (String) -> Unit,
    onSwitchToAnime: () -> Unit,
    onMangaClick: (Int) -> Unit,
    onMangaPlusOne: (UserMangaData) -> Unit,
    onMangaEdit: (UserMangaData) -> Unit,
    listState: LazyListState,
    gridState: LazyGridState
) {
    val isSearching = searchQuery.isNotBlank()

    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(gridState),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        UserListSearchEmptyState(
                            searchQuery = searchQuery,
                            onSwitchToAll = onSwitchToAll,
                            onSearchOnMal = onSearchOnMal,
                            onSwitchMedia = onSwitchToAnime,
                            switchMediaLabel = "Switch to Anime"
                        )
                    }
                } else {
                    items(
                        count = searchResults.size,
                        key = { index -> searchResults[index].node.id }
                    ) { index ->
                        val item = searchResults[index]
                        UserMangaGridItem(
                            data = item,
                            titleLanguage = titleLanguage,
                            onClick = { onMangaClick(item.node.id) },
                            onPlusOne = { onMangaPlusOne(item) },
                            onEditStatus = { onMangaEdit(item) }
                        )
                    }
                }
            } else {
                items(
                    count = mangaList.size,
                    key = { index -> mangaList[index].node.id }
                ) { index ->
                    val item = mangaList[index]
                    UserMangaGridItem(
                        data = item,
                        titleLanguage = titleLanguage,
                        onClick = { onMangaClick(item.node.id) },
                        onPlusOne = { onMangaPlusOne(item) },
                        onEditStatus = { onMangaEdit(item) }
                    )
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(listState),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item {
                        UserListSearchEmptyState(
                            searchQuery = searchQuery,
                            onSwitchToAll = onSwitchToAll,
                            onSearchOnMal = onSearchOnMal,
                            onSwitchMedia = onSwitchToAnime,
                            switchMediaLabel = "Switch to Anime"
                        )
                    }
                } else {
                    items(
                        count = searchResults.size,
                        key = { index -> searchResults[index].node.id }
                    ) { index ->
                        val item = searchResults[index]
                        UserMangaItem(
                            data = item,
                            titleLanguage = titleLanguage,
                            onClick = { onMangaClick(item.node.id) },
                            onPlusOne = { onMangaPlusOne(item) },
                            onEditStatus = { onMangaEdit(item) }
                        )
                    }
                }
            } else {
                items(
                    count = mangaList.size,
                    key = { index -> mangaList[index].node.id }
                ) { index ->
                    val item = mangaList[index]
                    UserMangaItem(
                        data = item,
                        titleLanguage = titleLanguage,
                        onClick = { onMangaClick(item.node.id) },
                        onPlusOne = { onMangaPlusOne(item) },
                        onEditStatus = { onMangaEdit(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserListLoadingShimmer(isGridView: Boolean, isManga: Boolean) {
    val shimmerBrush = rememberShimmerBrush()

    if (isGridView) {
        val gridState = rememberLazyGridState()
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(gridState),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(12) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(shimmerBrush)
                )
            }
        }
    } else {
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().verticalScrollbar(listState),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(6) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(86.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 108.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(
                                if (isManga) PaddingValues(6.dp)
                                else PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                            )
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(if (isManga) 2.dp else 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(shimmerBrush)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(shimmerBrush)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(shimmerBrush)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(shimmerBrush)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(shimmerBrush)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(84.dp)
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(shimmerBrush)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "user_list_shimmer")
    val offset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    return Brush.horizontalGradient(
        colors = listOf(base, highlight, base),
        startX = offset - 220f,
        endX = offset
    )
}

fun getUserStatusIcon(
    status: String?,
    isRewatching: Boolean = false,
    isRereading: Boolean = false
): ImageVector {
    if (isRewatching || isRereading) return Icons.Default.Autorenew
    return when (status?.lowercase()) {
        "watching", "reading" -> Icons.Default.Visibility
        "completed" -> Icons.Default.CheckCircle
        "on_hold" -> Icons.Default.PauseCircle
        "dropped" -> Icons.Default.Cancel
        "plan_to_watch", "plan_to_read" -> Icons.Default.Schedule
        else -> Icons.Default.Star
    }
}

fun getUserStatusLabel(
    status: String?,
    isRewatching: Boolean = false,
    isRereading: Boolean = false
): String {
    return when {
        isRewatching -> "REWATCHING"
        isRereading -> "REREADING"
        status.isNullOrBlank() -> "UNKNOWN"
        else -> status.replace("_", " ").uppercase()
    }
}

private fun ListStatus.toMyListStatus(): MyListStatus = MyListStatus(
    status = status,
    score = score,
    numEpisodesWatched = numEpisodesWatched,
    isRewatching = isRewatching,
    updatedAt = updatedAt
)

private fun MangaListStatus.toMyMangaListStatus(): MyMangaListStatus = MyMangaListStatus(
    status = status,
    score = score,
    numVolumesRead = numVolumesRead,
    numChaptersRead = numChaptersRead,
    isRereading = isRereading,
    updatedAt = updatedAt
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserAnimeItem(
    data: UserAnimeData,
    anilistMedia: AniListMedia?,
    nowEpochSeconds: Long,
    titleLanguage: TitleLanguage,
    onClick: () -> Unit,
    onPlusOne: () -> Unit,
    onEditStatus: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onPlusOne()
            isAnimating = false
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showQuickActions = true }
                ),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = data.node.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier
                    .width(86.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 108.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = data.node.getPreferredTitle(titleLanguage),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            minLines = 1,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        if (data.listStatus.score > 0) {
                            Row(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Your score",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = data.listStatus.score.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    val total = if (data.node.numEpisodes != null && data.node.numEpisodes > 0) data.node.numEpisodes.toString() else "?"
                    val episodesText = if (
                        data.node.status == "currently_airing" &&
                        anilistMedia?.nextAiringEpisode != null
                    ) {
                        val aired = anilistMedia?.nextAiringEpisode?.episode?.let { maxOf(it - 1, 0).toString() } ?: "?"
                        "${data.listStatus.numEpisodesWatched} / $aired / $total"
                    } else {
                        "${data.listStatus.numEpisodesWatched} / $total"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMediaType(data.node.mediaType),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getUserStatusIcon(
                                    status = data.listStatus.status,
                                    isRewatching = data.listStatus.isRewatching
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = getUserStatusLabel(
                                    status = data.listStatus.status,
                                    isRewatching = data.listStatus.isRewatching
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    val nextAiring = anilistMedia?.nextAiringEpisode
                    val airingLabel = airingTileLabel(
                        schedule = nextAiring,
                        malStatus = data.node.status,
                        broadcast = data.node.broadcast,
                        nowEpochSeconds = nowEpochSeconds
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Watched: $episodesText",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (airingLabel != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = airingLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        DropdownMenu(
            expanded = showQuickActions,
            onDismissRequest = { showQuickActions = false }
        ) {
            DropdownMenuItem(
                text = { Text("+1") },
                onClick = {
                    showQuickActions = false
                    isAnimating = true
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showQuickActions = false
                    onEditStatus()
                }
            )
        }
        if (isAnimating) {
            Text(
                text = "+1",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMangaItem(
    data: UserMangaData,
    titleLanguage: TitleLanguage,
    onClick: () -> Unit,
    onPlusOne: () -> Unit,
    onEditStatus: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onPlusOne()
            isAnimating = false
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showQuickActions = true }
                ),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = data.node.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier
                    .width(86.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(6.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 108.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = data.node.getPreferredTitle(titleLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        minLines = 1,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                        
                    val totalVols = if (data.node.numVolumes != null && data.node.numVolumes > 0) data.node.numVolumes.toString() else "?"
                    val totalChaps = if (data.node.numChapters != null && data.node.numChapters > 0) data.node.numChapters.toString() else "?"

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Score",
                                tint = Color.Yellow,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = data.listStatus.score.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Read: ${data.listStatus.numVolumesRead}/$totalVols vols, ${data.listStatus.numChaptersRead}/$totalChaps chaps",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getUserStatusIcon(
                                status = data.listStatus.status,
                                isRereading = data.listStatus.isRereading
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getUserStatusLabel(
                                status = data.listStatus.status,
                                isRereading = data.listStatus.isRereading
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = showQuickActions,
            onDismissRequest = { showQuickActions = false }
        ) {
            DropdownMenuItem(
                text = { Text("+1") },
                onClick = {
                    showQuickActions = false
                    isAnimating = true
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showQuickActions = false
                    onEditStatus()
                }
            )
        }
        if (isAnimating) {
            Text(
                text = "+1",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserAnimeGridItem(
    data: UserAnimeData, 
    anilistMedia: AniListMedia?,
    nowEpochSeconds: Long,
    titleLanguage: TitleLanguage,
    onClick: () -> Unit,
    onPlusOne: () -> Unit,
    onEditStatus: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onPlusOne()
            isAnimating = false
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showQuickActions = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = data.node.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Status Icon Overlay (Top Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f), shape = CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = getUserStatusIcon(
                            status = data.listStatus.status,
                            isRewatching = data.listStatus.isRewatching
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Score Overlay (Top Right)
                if (data.listStatus.score > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Score", tint = Color.Yellow, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${data.listStatus.score}", 
                            color = Color.White, 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = data.node.getPreferredTitle(titleLanguage),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val total = if (data.node.numEpisodes != null && data.node.numEpisodes > 0) data.node.numEpisodes.toString() else "?"
                    val episodesText = if (
                        data.node.status == "currently_airing" &&
                        anilistMedia?.nextAiringEpisode != null
                    ) {
                        val aired = anilistMedia?.nextAiringEpisode?.episode?.let { maxOf(it - 1, 0).toString() } ?: "?"
                        "${data.listStatus.numEpisodesWatched} / $aired / $total"
                    } else {
                        "${data.listStatus.numEpisodesWatched} / $total"
                    }
                    
                    Text(
                        text = "Ep: $episodesText",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                    
                    val nextAiring = anilistMedia?.nextAiringEpisode
                    val airingLabel = airingTileLabel(
                        schedule = nextAiring,
                        malStatus = data.node.status,
                        broadcast = data.node.broadcast,
                        nowEpochSeconds = nowEpochSeconds,
                        compactFallback = true
                    )
                    if (airingLabel != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = airingLabel,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
        DropdownMenu(
            expanded = showQuickActions,
            onDismissRequest = { showQuickActions = false }
        ) {
            DropdownMenuItem(
                text = { Text("+1") },
                onClick = {
                    showQuickActions = false
                    isAnimating = true
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showQuickActions = false
                    onEditStatus()
                }
            )
        }
        if (isAnimating) {
            Text(
                text = "+1",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@Composable
private fun rememberCurrentEpochSeconds(refreshMillis: Long = 60_000L): State<Long> {
    val now = remember { mutableStateOf(System.currentTimeMillis() / 1000L) }
    LaunchedEffect(refreshMillis) {
        while (true) {
            delay(refreshMillis)
            now.value = System.currentTimeMillis() / 1000L
        }
    }
    return now
}

private fun formatNextEpisodeCountdown(
    anilistMedia: AniListMedia?,
    nowEpochSeconds: Long,
    totalEpisodes: Int?
): String? {
    val nextAiring = anilistMedia?.nextAiringEpisode ?: return null
    val timeUntil = nextAiring.airingAt - nowEpochSeconds
    if (timeUntil <= 0) {
        val total = totalEpisodes?.takeIf { it > 0 } ?: return null
        if ((nextAiring.episode ?: 0) >= total) return null

        val estimatedNextAiringAt = nextAiring.airingAt + 7 * 86400
        val estimatedTimeUntil = estimatedNextAiringAt - nowEpochSeconds
        if (estimatedTimeUntil <= 0) return null
        return "~${formatCountdownDuration(estimatedTimeUntil)}"
    }

    return formatCountdownDuration(timeUntil)
}

private fun formatCountdownDuration(timeUntil: Long): String {
    val days = timeUntil / 86400
    val hours = (timeUntil % 86400) / 3600
    val mins = (timeUntil % 3600) / 60
    return if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMangaGridItem(
    data: UserMangaData, 
    titleLanguage: TitleLanguage,
    onClick: () -> Unit,
    onPlusOne: () -> Unit,
    onEditStatus: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onPlusOne()
            isAnimating = false
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showQuickActions = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = data.node.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Status Icon Overlay (Top Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f), shape = CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = getUserStatusIcon(
                            status = data.listStatus.status,
                            isRereading = data.listStatus.isRereading
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (data.listStatus.score > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Score", tint = Color.Yellow, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${data.listStatus.score}", 
                            color = Color.White, 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = data.node.getPreferredTitle(titleLanguage),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val totalVols = if (data.node.numVolumes != null && data.node.numVolumes > 0) data.node.numVolumes.toString() else "?"
                    val totalChaps = if (data.node.numChapters != null && data.node.numChapters > 0) data.node.numChapters.toString() else "?"
                    
                    Text(
                        text = "Ch: ${data.listStatus.numChaptersRead}/$totalChaps | Vol: ${data.listStatus.numVolumesRead}/$totalVols",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        DropdownMenu(
            expanded = showQuickActions,
            onDismissRequest = { showQuickActions = false }
        ) {
            DropdownMenuItem(
                text = { Text("+1") },
                onClick = {
                    showQuickActions = false
                    isAnimating = true
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showQuickActions = false
                    onEditStatus()
                }
            )
        }
        if (isAnimating) {
            Text(
                text = "+1",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
        }
    }
}
