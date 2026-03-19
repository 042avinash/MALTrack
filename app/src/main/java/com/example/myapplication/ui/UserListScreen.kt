package com.example.myapplication.ui

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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.getPreferredTitle
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.ListStatus
import com.example.myapplication.data.model.MangaListStatus
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.MyMangaListStatus
import com.example.myapplication.data.model.UserAnimeData
import com.example.myapplication.data.model.UserMangaData
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
    username: String? = null,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onOpenSettings: () -> Unit
) {
    var isAnimeView by remember { mutableStateOf(initialMainTab == 0) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAnimeView) {
            UserAnimeSection(
                viewModel = animeViewModel,
                titleLanguage = titleLanguage,
                initialSubTab = initialSubTab,
                username = username,
                onAnimeClick = onAnimeClick,
                onTypeSwitch = { isAnimeView = false },
                onOpenSettings = onOpenSettings
            )
        } else {
            UserMangaSection(
                viewModel = mangaViewModel,
                titleLanguage = titleLanguage,
                initialSubTab = initialSubTab,
                username = username,
                onMangaClick = onMangaClick,
                onTypeSwitch = { isAnimeView = true },
                onOpenSettings = onOpenSettings
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
    onSearchSubmit: () -> Unit,
    recentSearches: List<String>,
    onRecentSearchSelected: (String) -> Unit,
    onRefreshClick: () -> Unit,
    onOpenSettings: () -> Unit,
    isAnime: Boolean,
    isOwnList: Boolean,
    onTypeSwitch: () -> Unit
) {
    val listOwnerLabel = if (isOwnList) "Your" else "User"
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
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

                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserAnimeSection(
    viewModel: UserListViewModel,
    titleLanguage: TitleLanguage,
    initialSubTab: Int,
    username: String? = null,
    onAnimeClick: (Int) -> Unit,
    onTypeSwitch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val userListState by viewModel.userListState.collectAsState()
    val airingDetails by viewModel.airingDetails.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val loadedLists by viewModel.loadedLists.collectAsState()
    val loadingStatuses by viewModel.loadingStatuses.collectAsState()

    val statuses = listOf("all", "watching", "completed", "on_hold", "plan_to_watch", "dropped")
    val statusIcons = listOf(
        Icons.AutoMirrored.Filled.List,
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
    var isSearchTransitionLoading by remember { mutableStateOf(false) }
    var isTabTransitionLoading by rememberSaveable(username) { mutableStateOf(false) }
    var loadedStatuses by rememberSaveable(username) { mutableStateOf(setOf<String>()) }
    var pendingAnimeEdit by remember { mutableStateOf<UserAnimeData?>(null) }

    val settledPage = pagerState.settledPage
    val activePage = pagerState.currentPage
    val currentStatus = statuses[activePage]
    val isGridView by viewModel.getGridModeFlow(currentStatus)
        .collectAsState(initial = viewModel.getSavedGridMode(currentStatus))

    LaunchedEffect(settledPage, username) {
        if (currentStatus !in loadedStatuses) {
            isTabTransitionLoading = true
            viewModel.loadUserList(currentStatus, username = username, forceRefresh = false)
        } else {
            isTabTransitionLoading = false
        }
    }

    LaunchedEffect(username) {
        searchQuery = ""
        submittedSearchQuery = ""
        isSearchExpanded = false
        viewModel.clearSearch()
    }

    LaunchedEffect(submittedSearchQuery, currentStatus, username, userListState.sort) {
        val query = submittedSearchQuery.trim()
        if (query.isBlank()) {
            isSearchTransitionLoading = false
            viewModel.clearSearch()
        } else {
            viewModel.searchUserList(
                query = query,
                status = currentStatus,
                username = username,
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
                    onRefreshClick = {
                        viewModel.refreshStatus(currentStatus, username = username)
                    },
                    onOpenSettings = onOpenSettings,
                    isAnime = true,
                    isOwnList = username.isNullOrBlank() || username == "@me",
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    statuses.forEachIndexed { index, _ ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Icon(statusIcons[index], contentDescription = null, modifier = Modifier.size(24.dp))
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 0
            ) { page ->
                val pageStatus = statuses[page]
                val pageSort = if (pageStatus == userListState.status) userListState.sort else null
                val pageIsGridView by viewModel.getGridModeFlow(pageStatus)
                    .collectAsState(initial = viewModel.getSavedGridMode(pageStatus))
                val effectiveUsername = (if (username == "null") null else username) ?: "@me"
                val pageCacheKey = listOf(effectiveUsername, pageStatus, pageSort ?: viewModel.getSavedSortMode(pageStatus)).joinToString("|")
                val pageItems = loadedLists[pageCacheKey]
                    ?: loadedLists.entries.firstOrNull {
                        it.key.startsWith("$effectiveUsername|$pageStatus|")
                    }?.value
                    ?: emptyList()
                val pageHasLoadedData = loadedLists.containsKey(pageCacheKey) || loadedLists.keys.any {
                    it.startsWith("$effectiveUsername|$pageStatus|")
                }
                val pageIsLoading = loadingStatuses[listOf(effectiveUsername, pageStatus).joinToString("|")] == true

                if (page == settledPage && !pageIsLoading) {
                    LaunchedEffect(pageIsLoading, pageHasLoadedData, settledPage, userListState.status) {
                        if (userListState.status == currentStatus) {
                            if (pageHasLoadedData) {
                                loadedStatuses = loadedStatuses + currentStatus
                            }
                            isTabTransitionLoading = false
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    UserAnimeList(
                        listKey = "anime_${username ?: "@me"}_$pageStatus",
                        animeList = pageItems,
                        airingDetails = airingDetails,
                        titleLanguage = titleLanguage,
                        searchQuery = if (page == activePage) submittedSearchQuery else "",
                        searchResults = if (page == activePage) searchState.results else emptyList(),
                        isSearchLoading = page == activePage && submittedSearchQuery.isNotBlank() && (searchState.isLoading || isSearchTransitionLoading),
                        isGridView = pageIsGridView,
                        isOwnList = username == null,
                        onAnimeClick = onAnimeClick,
                        onAnimePlusOne = { item -> viewModel.quickIncrement(item.node.id) },
                        onAnimeEdit = { item -> pendingAnimeEdit = item },
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
                            UserListLoadingShimmer(isGridView = pageIsGridView)
                        }
                    }
                }
            }
        }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMangaSection(
    viewModel: UserMangaListViewModel,
    titleLanguage: TitleLanguage,
    initialSubTab: Int,
    username: String? = null,
    onMangaClick: (Int) -> Unit,
    onTypeSwitch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val userMangaListState by viewModel.userMangaListState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val loadedLists by viewModel.loadedLists.collectAsState()
    val loadingStatuses by viewModel.loadingStatuses.collectAsState()

    val statuses = listOf("all", "reading", "completed", "on_hold", "plan_to_read", "dropped")
    val statusIcons = listOf(
        Icons.AutoMirrored.Filled.List,
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
    var isSearchTransitionLoading by remember { mutableStateOf(false) }
    var isTabTransitionLoading by rememberSaveable(username) { mutableStateOf(false) }
    var loadedStatuses by rememberSaveable(username) { mutableStateOf(setOf<String>()) }
    var pendingMangaEdit by remember { mutableStateOf<UserMangaData?>(null) }

    val settledPage = pagerState.settledPage
    val activePage = pagerState.currentPage
    val currentStatus = statuses[activePage]
    val isGridView by viewModel.getGridModeFlow(currentStatus)
        .collectAsState(initial = viewModel.getSavedGridMode(currentStatus))

    LaunchedEffect(settledPage, username) {
        if (currentStatus !in loadedStatuses) {
            isTabTransitionLoading = true
            viewModel.loadUserMangaList(currentStatus, username = username, forceRefresh = false)
        } else {
            isTabTransitionLoading = false
        }
    }

    LaunchedEffect(username) {
        searchQuery = ""
        submittedSearchQuery = ""
        isSearchExpanded = false
        viewModel.clearSearch()
    }

    LaunchedEffect(submittedSearchQuery, currentStatus, username, userMangaListState.sort) {
        val query = submittedSearchQuery.trim()
        if (query.isBlank()) {
            isSearchTransitionLoading = false
            viewModel.clearSearch()
        } else {
            viewModel.searchUserList(
                query = query,
                status = currentStatus,
                username = username,
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
                    onRefreshClick = {
                        viewModel.refreshStatus(currentStatus, username = username)
                    },
                    onOpenSettings = onOpenSettings,
                    isAnime = false,
                    isOwnList = username.isNullOrBlank() || username == "@me",
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    statuses.forEachIndexed { index, _ ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Icon(statusIcons[index], contentDescription = null, modifier = Modifier.size(24.dp))
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 0
            ) { page ->
                val pageStatus = statuses[page]
                val pageSort = if (pageStatus == userMangaListState.status) userMangaListState.sort else null
                val pageIsGridView by viewModel.getGridModeFlow(pageStatus)
                    .collectAsState(initial = viewModel.getSavedGridMode(pageStatus))
                val effectiveUsername = (if (username == "null") null else username) ?: "@me"
                val pageCacheKey = listOf(effectiveUsername, pageStatus, pageSort ?: viewModel.getSavedSortMode(pageStatus)).joinToString("|")
                val pageItems = loadedLists[pageCacheKey]
                    ?: loadedLists.entries.firstOrNull {
                        it.key.startsWith("$effectiveUsername|$pageStatus|")
                    }?.value
                    ?: emptyList()
                val pageHasLoadedData = loadedLists.containsKey(pageCacheKey) || loadedLists.keys.any {
                    it.startsWith("$effectiveUsername|$pageStatus|")
                }
                val pageIsLoading = loadingStatuses[listOf(effectiveUsername, pageStatus).joinToString("|")] == true

                if (page == settledPage && !pageIsLoading) {
                    LaunchedEffect(pageIsLoading, pageHasLoadedData, settledPage, userMangaListState.status) {
                        if (userMangaListState.status == currentStatus) {
                            if (pageHasLoadedData) {
                                loadedStatuses = loadedStatuses + currentStatus
                            }
                            isTabTransitionLoading = false
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    UserMangaList(
                        listKey = "manga_${username ?: "@me"}_$pageStatus",
                        mangaList = pageItems,
                        titleLanguage = titleLanguage,
                        searchQuery = if (page == activePage) submittedSearchQuery else "",
                        searchResults = if (page == activePage) searchState.results else emptyList(),
                        isSearchLoading = page == activePage && submittedSearchQuery.isNotBlank() && (searchState.isLoading || isSearchTransitionLoading),
                        isGridView = pageIsGridView,
                        isOwnList = username == null,
                        onMangaClick = onMangaClick,
                        onMangaPlusOne = { item -> viewModel.quickIncrement(item.node.id) },
                        onMangaEdit = { item -> pendingMangaEdit = item },
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
                            UserListLoadingShimmer(isGridView = pageIsGridView)
                        }
                    }
                }
            }
        }
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
fun UserAnimeList(
    listKey: String,
    animeList: List<UserAnimeData>,
    airingDetails: Map<Int, AniListMedia>,
    titleLanguage: TitleLanguage,
    searchQuery: String,
    searchResults: List<UserAnimeData>,
    isSearchLoading: Boolean,
    isGridView: Boolean,
    isOwnList: Boolean = true,
    onAnimeClick: (Int) -> Unit,
    onAnimePlusOne: (UserAnimeData) -> Unit,
    onAnimeEdit: (UserAnimeData) -> Unit
) {
    val listState = rememberSaveable(listKey, saver = LazyListState.Saver) {
        LazyListState()
    }
    val gridState = rememberSaveable(listKey, saver = LazyGridState.Saver) {
        LazyGridState()
    }
    val isSearching = searchQuery.isNotBlank()

    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No matches found")
                        }
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
                            titleLanguage = titleLanguage,
                            isOwnList = isOwnList,
                            onClick = { onAnimeClick(item.node.id) },
                            onPlusOne = { if (isOwnList) onAnimePlusOne(item) },
                            onEditStatus = { if (isOwnList) onAnimeEdit(item) }
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
                        titleLanguage = titleLanguage,
                        isOwnList = isOwnList,
                        onClick = { onAnimeClick(item.node.id) },
                        onPlusOne = { if (isOwnList) onAnimePlusOne(item) },
                        onEditStatus = { if (isOwnList) onAnimeEdit(item) }
                    )
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No matches found")
                        }
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
                            titleLanguage = titleLanguage,
                            isOwnList = isOwnList,
                            onClick = { onAnimeClick(item.node.id) },
                            onPlusOne = { if (isOwnList) onAnimePlusOne(item) },
                            onEditStatus = { if (isOwnList) onAnimeEdit(item) }
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
                        titleLanguage = titleLanguage,
                        isOwnList = isOwnList,
                        onClick = { onAnimeClick(item.node.id) },
                        onPlusOne = { if (isOwnList) onAnimePlusOne(item) },
                        onEditStatus = { if (isOwnList) onAnimeEdit(item) }
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
    searchQuery: String,
    searchResults: List<UserMangaData>,
    isSearchLoading: Boolean,
    isGridView: Boolean,
    isOwnList: Boolean = true,
    onMangaClick: (Int) -> Unit,
    onMangaPlusOne: (UserMangaData) -> Unit,
    onMangaEdit: (UserMangaData) -> Unit
) {
    val listState = rememberSaveable(listKey, saver = LazyListState.Saver) {
        LazyListState()
    }
    val gridState = rememberSaveable(listKey, saver = LazyGridState.Saver) {
        LazyGridState()
    }
    val isSearching = searchQuery.isNotBlank()

    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No matches found")
                        }
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
                            isOwnList = isOwnList,
                            onClick = { onMangaClick(item.node.id) },
                            onPlusOne = { if (isOwnList) onMangaPlusOne(item) },
                            onEditStatus = { if (isOwnList) onMangaEdit(item) }
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
                        isOwnList = isOwnList,
                        onClick = { onMangaClick(item.node.id) },
                        onPlusOne = { if (isOwnList) onMangaPlusOne(item) },
                        onEditStatus = { if (isOwnList) onMangaEdit(item) }
                    )
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                if (!isSearchLoading && searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No matches found")
                        }
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
                            isOwnList = isOwnList,
                            onClick = { onMangaClick(item.node.id) },
                            onPlusOne = { if (isOwnList) onMangaPlusOne(item) },
                            onEditStatus = { if (isOwnList) onMangaEdit(item) }
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
                        isOwnList = isOwnList,
                        onClick = { onMangaClick(item.node.id) },
                        onPlusOne = { if (isOwnList) onMangaPlusOne(item) },
                        onEditStatus = { if (isOwnList) onMangaEdit(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserListLoadingShimmer(isGridView: Boolean) {
    val shimmerBrush = rememberShimmerBrush()

    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(6) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(146.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(82.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerBrush)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerBrush)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerBrush)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerBrush)
                            )
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

fun getUserStatusIcon(status: String?): ImageVector {
    return when (status?.lowercase()) {
        "watching", "reading" -> Icons.Default.Visibility
        "completed" -> Icons.Default.CheckCircle
        "on_hold" -> Icons.Default.PauseCircle
        "dropped" -> Icons.Default.Cancel
        "plan_to_watch", "plan_to_read" -> Icons.Default.Schedule
        else -> Icons.Default.Star
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
    titleLanguage: TitleLanguage,
    isOwnList: Boolean = true,
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
                    onLongClick = { if (isOwnList) showQuickActions = true }
                ),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = data.node.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier
                    .width(82.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.node.getPreferredTitle(titleLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val total = if (data.node.numEpisodes != null && data.node.numEpisodes > 0) data.node.numEpisodes.toString() else "?"
                        val episodesText = if (data.node.status == "currently_airing") {
                            val aired = if (anilistMedia?.nextAiringEpisode != null) (anilistMedia.nextAiringEpisode.episode - 1).toString() else "?"
                            "${data.listStatus.numEpisodesWatched} / $aired / $total"
                        } else {
                            "${data.listStatus.numEpisodesWatched} / $total"
                        }
                        
                        Text(
                            text = "Score: ${data.listStatus.score} | Watched: $episodesText",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val countdown = if (data.node.status == "currently_airing" && anilistMedia?.nextAiringEpisode != null) {
                            val timeUntil = anilistMedia.nextAiringEpisode.timeUntilAiring
                            val days = timeUntil / 86400
                            val hours = (timeUntil % 86400) / 3600
                            val mins = (timeUntil % 3600) / 60
                            if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
                        } else null

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getUserStatusIcon(data.listStatus.status),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = data.listStatus.status.replace("_", " ").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        if (countdown != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Next Ep: $countdown",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
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
                color = MaterialTheme.colorScheme.primary,
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
    isOwnList: Boolean = true,
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
                    onLongClick = { if (isOwnList) showQuickActions = true }
                ),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = data.node.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier
                    .width(82.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.node.getPreferredTitle(titleLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val totalVols = if (data.node.numVolumes != null && data.node.numVolumes > 0) data.node.numVolumes.toString() else "?"
                        val totalChaps = if (data.node.numChapters != null && data.node.numChapters > 0) data.node.numChapters.toString() else "?"
                        
                        Text(
                            text = "Score: ${data.listStatus.score} | Read: ${data.listStatus.numVolumesRead}/$totalVols vols, ${data.listStatus.numChaptersRead}/$totalChaps chaps",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getUserStatusIcon(data.listStatus.status),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = data.listStatus.status.replace("_", " ").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
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
                color = MaterialTheme.colorScheme.primary,
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
    titleLanguage: TitleLanguage,
    isOwnList: Boolean = true,
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
                    onLongClick = { if (isOwnList) showQuickActions = true }
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
                        imageVector = getUserStatusIcon(data.listStatus.status),
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
                    val episodesText = if (data.node.status == "currently_airing") {
                        val aired = if (anilistMedia?.nextAiringEpisode != null) (anilistMedia.nextAiringEpisode.episode - 1).toString() else "?"
                        "${data.listStatus.numEpisodesWatched} / $aired / $total"
                    } else {
                        "${data.listStatus.numEpisodesWatched} / $total"
                    }
                    
                    Text(
                        text = "Ep: $episodesText",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                    
                    if (data.node.status == "currently_airing" && anilistMedia?.nextAiringEpisode != null) {
                        val timeUntil = anilistMedia.nextAiringEpisode.timeUntilAiring
                        val days = timeUntil / 86400
                        val hours = (timeUntil % 86400) / 3600
                        val mins = (timeUntil % 3600) / 60
                        val countdown = if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
                        
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Next: $countdown",
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMangaGridItem(
    data: UserMangaData, 
    titleLanguage: TitleLanguage,
    isOwnList: Boolean = true,
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
                    onLongClick = { if (isOwnList) showQuickActions = true }
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
                        imageVector = getUserStatusIcon(data.listStatus.status),
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale)
            )
        }
    }
}
