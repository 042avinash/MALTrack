package com.example.myapplication.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.getPreferredTitle
import com.example.myapplication.data.model.AniListMedia
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
    onRefreshClick: () -> Unit,
    onOpenSettings: () -> Unit,
    isAnime: Boolean,
    onTypeSwitch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
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
                        placeholder = { Text("Search...", fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
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
                            if (isAnime) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isAnime) "To Manga" else "To Anime", fontSize = 12.sp)
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
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isTabTransitionLoading by rememberSaveable(username) { mutableStateOf(false) }
    var loadedStatuses by rememberSaveable(username) { mutableStateOf(setOf<String>()) }

    val settledPage = pagerState.settledPage
    val currentStatus = statuses[settledPage]
    val isGridView by viewModel.getGridModeFlow(currentStatus)
        .collectAsState(initial = viewModel.getSavedGridMode(currentStatus))

    LaunchedEffect(settledPage, username) {
        searchQuery = ""
        isSearchExpanded = false
        viewModel.clearSearch()
        if (currentStatus !in loadedStatuses) {
            isTabTransitionLoading = true
            viewModel.loadUserList(currentStatus, username = username, forceRefresh = false)
            loadedStatuses = loadedStatuses + currentStatus
        } else {
            isTabTransitionLoading = false
        }
    }

    LaunchedEffect(searchQuery, currentStatus, username, userListState.sort) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            viewModel.clearSearch()
        } else {
            delay(250)
            if (query == searchQuery.trim()) {
                viewModel.searchUserList(
                    query = query,
                    status = currentStatus,
                    username = username,
                    sort = userListState.sort
                )
            }
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
                    onRefreshClick = {
                        loadedStatuses = loadedStatuses + currentStatus
                        viewModel.refreshStatus(currentStatus, username = username)
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
                beyondViewportPageCount = 1
            ) { page ->
                val pageStatus = statuses[page]
                val pageSort = if (pageStatus == userListState.status) userListState.sort else null
                val pageIsGridView by viewModel.getGridModeFlow(pageStatus)
                    .collectAsState(initial = viewModel.getSavedGridMode(pageStatus))
                val effectiveUsername = (if (username == "null") null else username) ?: "@me"
                val pageCacheKey = listOf(effectiveUsername, pageStatus, pageSort ?: viewModel.getSavedSortMode(pageStatus)).joinToString("|")
                val pageItems = loadedLists[pageCacheKey].orEmpty()
                val pageIsLoading = loadingStatuses[listOf(effectiveUsername, pageStatus).joinToString("|")] == true

                if (page == settledPage && !pageIsLoading) {
                    LaunchedEffect(pageIsLoading, settledPage, userListState.status) {
                        if (userListState.status == currentStatus) {
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
                        searchQuery = if (page == settledPage) searchQuery else "",
                        searchResults = if (page == settledPage) searchState.results else emptyList(),
                        isSearchLoading = page == settledPage && searchQuery.isNotBlank() && searchState.isLoading,
                        isGridView = pageIsGridView,
                        isOwnList = username == null,
                        onAnimeClick = onAnimeClick,
                        onAnimeLongClick = { viewModel.quickIncrement(it) },
                    )

                    if (page == settledPage && (isTabTransitionLoading || pageIsLoading)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
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
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isTabTransitionLoading by rememberSaveable(username) { mutableStateOf(false) }
    var loadedStatuses by rememberSaveable(username) { mutableStateOf(setOf<String>()) }

    val settledPage = pagerState.settledPage
    val currentStatus = statuses[settledPage]
    val isGridView by viewModel.getGridModeFlow(currentStatus)
        .collectAsState(initial = viewModel.getSavedGridMode(currentStatus))

    LaunchedEffect(settledPage, username) {
        searchQuery = ""
        isSearchExpanded = false
        viewModel.clearSearch()
        if (currentStatus !in loadedStatuses) {
            isTabTransitionLoading = true
            viewModel.loadUserMangaList(currentStatus, username = username, forceRefresh = false)
            loadedStatuses = loadedStatuses + currentStatus
        } else {
            isTabTransitionLoading = false
        }
    }

    LaunchedEffect(searchQuery, currentStatus, username, userMangaListState.sort) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            viewModel.clearSearch()
        } else {
            delay(250)
            if (query == searchQuery.trim()) {
                viewModel.searchUserList(
                    query = query,
                    status = currentStatus,
                    username = username,
                    sort = userMangaListState.sort
                )
            }
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
                    onRefreshClick = {
                        loadedStatuses = loadedStatuses + currentStatus
                        viewModel.refreshStatus(currentStatus, username = username)
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
                beyondViewportPageCount = 1
            ) { page ->
                val pageStatus = statuses[page]
                val pageSort = if (pageStatus == userMangaListState.status) userMangaListState.sort else null
                val pageIsGridView by viewModel.getGridModeFlow(pageStatus)
                    .collectAsState(initial = viewModel.getSavedGridMode(pageStatus))
                val effectiveUsername = (if (username == "null") null else username) ?: "@me"
                val pageCacheKey = listOf(effectiveUsername, pageStatus, pageSort ?: viewModel.getSavedSortMode(pageStatus)).joinToString("|")
                val pageItems = loadedLists[pageCacheKey].orEmpty()
                val pageIsLoading = loadingStatuses[listOf(effectiveUsername, pageStatus).joinToString("|")] == true

                if (page == settledPage && !pageIsLoading) {
                    LaunchedEffect(pageIsLoading, settledPage, userMangaListState.status) {
                        if (userMangaListState.status == currentStatus) {
                            isTabTransitionLoading = false
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    UserMangaList(
                        listKey = "manga_${username ?: "@me"}_$pageStatus",
                        mangaList = pageItems,
                        titleLanguage = titleLanguage,
                        searchQuery = if (page == settledPage) searchQuery else "",
                        searchResults = if (page == settledPage) searchState.results else emptyList(),
                        isSearchLoading = page == settledPage && searchQuery.isNotBlank() && searchState.isLoading,
                        isGridView = pageIsGridView,
                        isOwnList = username == null,
                        onMangaClick = onMangaClick,
                        onMangaLongClick = { viewModel.quickIncrement(it) },
                    )

                    if (page == settledPage && (isTabTransitionLoading || pageIsLoading)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
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
    onAnimeLongClick: (Int) -> Unit
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
                            onLongClick = { if (isOwnList) onAnimeLongClick(item.node.id) }
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
                        onLongClick = { if (isOwnList) onAnimeLongClick(item.node.id) }
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
                            onLongClick = { if (isOwnList) onAnimeLongClick(item.node.id) }
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
                        onLongClick = { if (isOwnList) onAnimeLongClick(item.node.id) }
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
    onMangaLongClick: (Int) -> Unit
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
                            onLongClick = { if (isOwnList) onMangaLongClick(item.node.id) }
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
                        onLongClick = { if (isOwnList) onMangaLongClick(item.node.id) }
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
                            onLongClick = { if (isOwnList) onMangaLongClick(item.node.id) }
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
                        onLongClick = { if (isOwnList) onMangaLongClick(item.node.id) }
                    )
                }
            }
        }
    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserAnimeItem(
    data: UserAnimeData,
    anilistMedia: AniListMedia?,
    titleLanguage: TitleLanguage,
    isOwnList: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onLongClick()
            isAnimating = false
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { if (isOwnList) isAnimating = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model = data.node.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp, 90.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.node.getPreferredTitle(titleLanguage),
                        style = MaterialTheme.typography.titleMedium,
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
                    
                    if (data.node.status == "currently_airing" && anilistMedia?.nextAiringEpisode != null) {
                        val timeUntil = anilistMedia.nextAiringEpisode.timeUntilAiring
                        val days = timeUntil / 86400
                        val hours = (timeUntil % 86400) / 3600
                        val mins = (timeUntil % 3600) / 60
                        val countdown = if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
                        
                        Spacer(modifier = Modifier.height(4.dp))
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (data.listStatus.score > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Score",
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = data.listStatus.score.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
    onLongClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onLongClick()
            isAnimating = false
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { if (isOwnList) isAnimating = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model = data.node.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp, 90.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.node.getPreferredTitle(titleLanguage),
                        style = MaterialTheme.typography.titleMedium,
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
                if (data.listStatus.score > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Score",
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = data.listStatus.score.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
    onLongClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onLongClick()
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
                    onLongClick = { if (isOwnList) isAnimating = true }
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
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(4.dp)
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
    onLongClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (isAnimating) 0.3f else 1f, animationSpec = tween(500))
    val scale by animateFloatAsState(targetValue = if (isAnimating) 1.5f else 0f, animationSpec = tween(500))

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(600)
            onLongClick()
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
                    onLongClick = { if (isOwnList) isAnimating = true }
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
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(4.dp)
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
