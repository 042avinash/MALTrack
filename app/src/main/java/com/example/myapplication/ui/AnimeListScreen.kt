package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.getPreferredTitle
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AnimeData
import com.example.myapplication.data.model.MangaData
import com.example.myapplication.data.model.MyListStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnimeListScreen(
    viewModel: AnimeViewModel,
    titleLanguage: TitleLanguage,
    initialTab: Int = 0,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onOpenAnimeUserList: () -> Unit,
    onOpenMangaUserList: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickUpdateEvent by viewModel.quickUpdateEvent.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val airingDetails by viewModel.airingDetails.collectAsState()
    val randomAnimeNavigation by viewModel.randomAnimeNavigation.collectAsState()
    val isRefreshingAnimeRecommendations by viewModel.isRefreshingAnimeRecommendations.collectAsState()
    val isRefreshingMangaRecommendations by viewModel.isRefreshingMangaRecommendations.collectAsState()
    val animeDiscoveryIsGrid by viewModel.getDiscoveryGridModeFlow(isAnime = true).collectAsState(initial = false)
    val mangaDiscoveryIsGrid by viewModel.getDiscoveryGridModeFlow(isAnime = false).collectAsState(initial = false)
    val animeDiscoverySort by viewModel.getDiscoverySortFlow(isAnime = true).collectAsState(initial = "members")
    val mangaDiscoverySort by viewModel.getDiscoverySortFlow(isAnime = false).collectAsState(initial = "members")
    val homeContinueWatchingEnabled by viewModel.getHomeContinueWatchingEnabledFlow().collectAsState(initial = true)
    val homeContinueReadingEnabled by viewModel.getHomeContinueReadingEnabledFlow().collectAsState(initial = true)
    val homeDiscoveryButtonsEnabled by viewModel.getHomeDiscoveryButtonsEnabledFlow().collectAsState(initial = true)
    val homeRandomAnimeEnabled by viewModel.getHomeRandomAnimeEnabledFlow().collectAsState(initial = true)
    val homeAnimePicksEnabled by viewModel.getHomeAnimePicksEnabledFlow().collectAsState(initial = true)
    val homeMangaPicksEnabled by viewModel.getHomeMangaPicksEnabledFlow().collectAsState(initial = true)
    val listFilters by viewModel.listFilters.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSeasonPicker by remember { mutableStateOf(false) }
    var searchMediaType by remember(initialTab) {
        mutableStateOf(if (initialTab == 0) SearchMediaType.ANIME else SearchMediaType.MANGA)
    }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDiscoveryIsGrid = when (val state = uiState) {
        is AnimeUiState.MangaDiscoveryDetails -> mangaDiscoveryIsGrid
        is AnimeUiState.TopDiscoveryDetails -> if (state.isAnime) animeDiscoveryIsGrid else mangaDiscoveryIsGrid
        is AnimeUiState.SearchSuccess -> if (searchMediaType == SearchMediaType.ANIME) animeDiscoveryIsGrid else mangaDiscoveryIsGrid
        else -> animeDiscoveryIsGrid
    }

    LaunchedEffect(Unit) {
        if (uiState is AnimeUiState.Loading && searchQuery.isEmpty()) {
            viewModel.loadHomeData()
        }
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(randomAnimeNavigation) {
        val animeId = randomAnimeNavigation ?: return@LaunchedEffect
        onAnimeClick(animeId)
        viewModel.consumeRandomAnimeNavigation()
    }

    if (uiState is AnimeUiState.SeasonalDetails || uiState is AnimeUiState.MangaDiscoveryDetails || uiState is AnimeUiState.TopDiscoveryDetails || uiState is AnimeUiState.SearchSuccess) {
        BackHandler {
            if (searchQuery.isNotEmpty()) {
                searchQuery = ""
                isSearchExpanded = false
                viewModel.loadHomeData()
            } else {
                viewModel.loadHomeData()
            }
        }
    }

    if (showSeasonPicker) {
        SeasonPicker(
            onDismiss = { showSeasonPicker = false },
            onSeasonSelected = { year, season ->
                viewModel.selectSpecificSeason(year, season)
                showSeasonPicker = false
            }
        )
    }

    quickUpdateEvent?.let { event ->
        if (event is QuickUpdateEvent.ConfirmAdd) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissQuickUpdate() },
                title = { Text("Add to List?") },
                text = { Text("Do you want to add \"${event.title}\" to your list and start watching/reading?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmQuickAdd(event.id, event.isAnime) }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissQuickUpdate() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            HomeSearchToolbar(
                searchMediaType = searchMediaType,
                searchQuery = searchQuery,
                isSearchExpanded = isSearchExpanded,
                onSearchExpandChange = { isSearchExpanded = it },
                onMediaTypeToggle = {
                    val nextType = if (searchMediaType == SearchMediaType.ANIME) SearchMediaType.MANGA else SearchMediaType.ANIME
                    searchMediaType = nextType
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchAnime(
                            searchQuery,
                            isAnimeSearch = nextType == SearchMediaType.ANIME
                        )
                    }
                },
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchAnime(
                        it,
                        isAnimeSearch = searchMediaType == SearchMediaType.ANIME
                    )
                },
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            when (val state = uiState) {
                is AnimeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AnimeUiState.HomeSuccess -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeBackgroundBlobs()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            if (homeContinueWatchingEnabled && state.continueWatching.isNotEmpty()) {
                                item {
                                    HomeSection(
                                        title = "Continue Watching",
                                        subtitle = "From your MAL list",
                                        items = state.continueWatching,
                                        airingDetails = airingDetails,
                                        titleLanguage = titleLanguage,
                                        onItemClick = { onAnimeClick(it) },
                                        onItemLongClick = { id, title -> viewModel.onLongPressAnime(id, title) },
                                        endButtonLabel = "Anime List",
                                        endButtonIcon = Icons.AutoMirrored.Filled.List,
                                        onEndButtonClick = onOpenAnimeUserList
                                    )
                                }
                            }
                            if (homeContinueReadingEnabled && state.continueReading.isNotEmpty()) {
                                item {
                                    HomeSection(
                                        title = "Continue Reading",
                                        subtitle = "From your MAL list",
                                        items = state.continueReading.map { it.toAnimeData() },
                                        airingDetails = emptyMap(),
                                        titleLanguage = titleLanguage,
                                        onItemClick = { onMangaClick(it) },
                                        onItemLongClick = { id, title -> viewModel.onLongPressManga(id, title) },
                                        endButtonLabel = "Manga List",
                                        endButtonIcon = Icons.AutoMirrored.Filled.List,
                                        onEndButtonClick = onOpenMangaUserList
                                    )
                                }
                            }
                            if (homeDiscoveryButtonsEnabled) {
                                item {
                                    DiscoveryButtonCluster(
                                        season = state.season,
                                        year = state.year,
                                        onSeasonalClick = { viewModel.showSeasonalDetails(animeDiscoverySort) },
                                        onTopAnimeClick = { viewModel.showTopDiscovery(isAnime = true, sort = animeDiscoverySort) },
                                        onTopMangaClick = { viewModel.showTopDiscovery(isAnime = false, sort = mangaDiscoverySort) }
                                    )
                                }
                            }
                            if (homeRandomAnimeEnabled) {
                                item {
                                    RandomAnimeHeroButton(
                                        onClick = { viewModel.openRandomAnime() }
                                    )
                                }
                            }
                            if (homeAnimePicksEnabled) {
                                item {
                                    HomeSection(
                                        title = "Personalized Anime Picks",
                                        subtitle = "Based on your list activity",
                                        items = state.animeRecommendations,
                                        airingDetails = airingDetails,
                                        titleLanguage = titleLanguage,
                                        onItemClick = { onAnimeClick(it) },
                                        onItemLongClick = { id, title -> viewModel.onLongPressAnime(id, title) },
                                        endButtonLabel = "Refresh Picks",
                                        endButtonIcon = Icons.Default.Refresh,
                                        endButtonLoading = isRefreshingAnimeRecommendations,
                                        onEndButtonClick = { viewModel.refreshAnimeRecommendations() }
                                    )
                                }
                            }
                            if (homeMangaPicksEnabled) {
                                item {
                                    HomeSection(
                                        title = "Personalized Manga Picks",
                                        subtitle = "Based on your list activity",
                                        items = state.mangaRecommendations.map { it.toAnimeData() },
                                        airingDetails = emptyMap(),
                                        titleLanguage = titleLanguage,
                                        onItemClick = { onMangaClick(it) },
                                        onItemLongClick = { id, title -> viewModel.onLongPressManga(id, title) },
                                        endButtonLabel = "Refresh Picks",
                                        endButtonIcon = Icons.Default.Refresh,
                                        endButtonLoading = isRefreshingMangaRecommendations,
                                        onEndButtonClick = { viewModel.refreshMangaRecommendations() }
                                    )
                                }
                            }
                        }
                    }
                }
                is AnimeUiState.SeasonalDetails -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SeasonNavigation(
                            year = state.year,
                            season = state.season,
                            canGoNext = state.canGoNext,
                            onPrevious = { viewModel.changeSeason(-1) },
                            onNext = { viewModel.changeSeason(1) },
                            onPick = { showSeasonPicker = true }
                        )
                        
                        SeasonalActionToolbar(
                            isGridView = currentDiscoveryIsGrid,
                            onGridClick = { viewModel.setDiscoveryGridMode(isAnime = true, isGrid = !currentDiscoveryIsGrid) },
                            currentSort = state.currentSort,
                            onSortChange = {
                                viewModel.setDiscoverySort(isAnime = true, sort = it)
                                viewModel.showSeasonalDetails(it)
                            },
                            listFilters = listFilters,
                            onFilterToggle = { viewModel.toggleFilter(it) }
                        )

                        SeasonalDetailsView(
                            categorizedAnime = state.categorizedAnime,
                            airingDetails = airingDetails,
                            titleLanguage = titleLanguage,
                            isGridView = currentDiscoveryIsGrid,
                            onAnimeClick = onAnimeClick,
                            onAnimeLongClick = { id, title -> viewModel.onLongPressAnime(id, title) }
                        )
                    }
                }
                is AnimeUiState.MangaDiscoveryDetails -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        SeasonalActionToolbar(
                            isGridView = currentDiscoveryIsGrid,
                            onGridClick = { viewModel.setDiscoveryGridMode(isAnime = false, isGrid = !currentDiscoveryIsGrid) },
                            currentSort = state.currentSort,
                            onSortChange = {
                                viewModel.setDiscoverySort(isAnime = false, sort = it)
                                viewModel.showMangaDiscovery(state.type, it)
                            },
                            listFilters = listFilters,
                            onFilterToggle = { viewModel.toggleFilter(it) },
                            isManga = true
                        )

                        SeasonalDetailsView(
                            categorizedAnime = state.categorizedManga.mapValues { entry -> 
                                entry.value.map { it.toAnimeData() } 
                            },
                            airingDetails = emptyMap(),
                            titleLanguage = titleLanguage,
                            isGridView = currentDiscoveryIsGrid,
                            onAnimeClick = onMangaClick,
                            onAnimeLongClick = { id, title -> viewModel.onLongPressManga(id, title) }
                        )
                    }
                }
                is AnimeUiState.TopDiscoveryDetails -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        SeasonalActionToolbar(
                            isGridView = currentDiscoveryIsGrid,
                            onGridClick = {
                                viewModel.setDiscoveryGridMode(
                                    isAnime = state.isAnime,
                                    isGrid = !currentDiscoveryIsGrid
                                )
                            },
                            currentSort = state.currentSort,
                            onSortChange = {
                                viewModel.setDiscoverySort(isAnime = state.isAnime, sort = it)
                                viewModel.showTopDiscovery(state.isAnime, it)
                            },
                            listFilters = listFilters,
                            onFilterToggle = { viewModel.toggleFilter(it) },
                            isTopList = true,
                            isManga = !state.isAnime
                        )

                        TopDiscoveryView(
                            items = state.items,
                            airingDetails = airingDetails,
                            titleLanguage = titleLanguage,
                            isGridView = currentDiscoveryIsGrid,
                            onItemClick = if (state.isAnime) onAnimeClick else onMangaClick,
                            onItemLongClick = { id, title -> 
                                if (state.isAnime) viewModel.onLongPressAnime(id, title) 
                                else viewModel.onLongPressManga(id, title) 
                            }
                        )
                    }
                }
                is AnimeUiState.SearchSuccess -> {
                    val sortedAnimeResults = if (animeDiscoverySort == "score") {
                        state.animeList.sortedByDescending { it.node.meanScore ?: 0f }
                    } else {
                        state.animeList
                    }
                    val sortedMangaResults = if (mangaDiscoverySort == "score") {
                        state.mangaList.sortedByDescending { it.node.meanScore ?: 0f }
                    } else {
                        state.mangaList
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchActionToolbar(
                            isGridView = currentDiscoveryIsGrid,
                            onGridClick = {
                                viewModel.setDiscoveryGridMode(
                                    isAnime = searchMediaType == SearchMediaType.ANIME,
                                    isGrid = !currentDiscoveryIsGrid
                                )
                            }
                        )

                        when {
                            sortedAnimeResults.isNotEmpty() -> {
                                TopDiscoveryView(
                                    items = sortedAnimeResults,
                                    airingDetails = airingDetails,
                                    titleLanguage = titleLanguage,
                                    isGridView = currentDiscoveryIsGrid,
                                    onItemClick = onAnimeClick,
                                    onItemLongClick = { id, title ->
                                        viewModel.onLongPressAnime(id, title)
                                    }
                                )
                            }

                            sortedMangaResults.isNotEmpty() -> {
                                TopDiscoveryView(
                                    items = sortedMangaResults.map { it.toAnimeData() },
                                    airingDetails = emptyMap(),
                                    titleLanguage = titleLanguage,
                                    isGridView = currentDiscoveryIsGrid,
                                    onItemClick = onMangaClick,
                                    onItemLongClick = { id, title ->
                                        viewModel.onLongPressManga(id, title)
                                    }
                                )
                            }

                            else -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "No ${searchMediaType.label.lowercase()} results found.",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                is AnimeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun SeasonalActionToolbar(
    isGridView: Boolean,
    onGridClick: () -> Unit,
    currentSort: String,
    onSortChange: (String) -> Unit,
    listFilters: Set<String>,
    onFilterToggle: (String) -> Unit,
    isTopList: Boolean = false,
    isManga: Boolean = false
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val sortOptions = listOf(
        "members" to "Popularity",
        "score" to "Score"
    )
    val currentSortLabel = sortOptions.firstOrNull { it.first == currentSort }?.second ?: currentSort

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Box {
            IconButton(onClick = { showFilterMenu = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
            DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                val filters = if (isManga) {
                    listOf(
                        "not_in_list" to "Not in My List",
                        "active" to "Reading",
                        "completed" to "Completed",
                        "on_hold" to "On-Hold",
                        "planned" to "Plan to Read",
                        "dropped" to "Dropped"
                    )
                } else {
                    listOf(
                        "not_in_list" to "Not in My List",
                        "active" to "Watching",
                        "completed" to "Completed",
                        "on_hold" to "On-Hold",
                        "planned" to "Plan to Watch",
                        "dropped" to "Dropped"
                    )
                }
                
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isAllChecked = filters.all { listFilters.contains(it.first) }
                            Checkbox(checked = isAllChecked, onCheckedChange = { onFilterToggle("all") })
                            Text("All")
                        }
                    },
                    onClick = { onFilterToggle("all") }
                )
                
                filters.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = listFilters.contains(key), onCheckedChange = { onFilterToggle(key) })
                                Text(label)
                            }
                        },
                        onClick = { onFilterToggle(key) }
                    )
                }
            }
        }

        Box {
            TextButton(onClick = { showSortMenu = true }) {
                Text("Sort: $currentSortLabel")
            }
            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                sortOptions.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onSortChange(key)
                            showSortMenu = false
                        },
                        trailingIcon = if (currentSort == key) { { Icon(Icons.Default.Check, "") } } else null
                    )
                }
            }
        }

        IconButton(onClick = onGridClick) {
            Icon(
                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                contentDescription = "Toggle View"
            )
        }
    }
}

@Composable
private fun HomeBackgroundBlobs() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = 40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = 180.dp, y = 260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 40.dp, y = 620.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun HomeSearchToolbar(
    searchMediaType: SearchMediaType,
    searchQuery: String,
    isSearchExpanded: Boolean,
    onSearchExpandChange: (Boolean) -> Unit,
    onMediaTypeToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        placeholder = {
                            Text(
                                "Search MAL ${searchMediaType.label}...",
                                fontSize = 14.sp
                            )
                        },
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
                        onClick = onMediaTypeToggle,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            if (searchMediaType == SearchMediaType.ANIME) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(searchMediaType.label, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { onSearchExpandChange(true) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchActionToolbar(
    isGridView: Boolean,
    onGridClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onGridClick) {
            Icon(
                imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                contentDescription = "Toggle View"
            )
        }
    }
}

@Composable
fun TopDiscoveryView(
    items: List<AnimeData>,
    airingDetails: Map<Int, AniListMedia>,
    titleLanguage: TitleLanguage,
    isGridView: Boolean,
    onItemClick: (Int) -> Unit,
    onItemLongClick: (Int, String) -> Unit
) {
    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                HorizontalCard(
                    anime = item, 
                    anilistMedia = airingDetails[item.node.id],
                    titleLanguage = titleLanguage,
                    onClick = { onItemClick(item.node.id) },
                    onLongClick = { onItemLongClick(item.node.id, item.node.getPreferredTitle(titleLanguage)) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                AnimeItem(
                    anime = item,
                    anilistMedia = airingDetails[item.node.id],
                    titleLanguage = titleLanguage,
                    onClick = { onItemClick(item.node.id) },
                    onLongClick = { onItemLongClick(item.node.id, item.node.getPreferredTitle(titleLanguage)) }
                )
            }
        }
    }
}

@Composable
fun SeasonNavigation(
    year: Int,
    season: String,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Season")
            }
            TextButton(onClick = onPick) {
                Text(
                    text = "${season.replaceFirstChar { it.uppercase() }} $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onNext,
                enabled = canGoNext
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Season",
                    tint = if (canGoNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Composable
fun SeasonPicker(
    onDismiss: () -> Unit,
    onSeasonSelected: (Int, String) -> Unit
) {
    var yearText by remember { mutableStateOf("2024") }
    val seasons = listOf("Winter", "Spring", "Summer", "Fall")
    var expanded by remember { mutableStateOf(false) }
    var selectedSeason by remember { mutableStateOf(seasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick a Season") },
        text = {
            Column {
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedSeason)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        seasons.forEach { season ->
                            DropdownMenuItem(
                                text = { Text(season) },
                                onClick = {
                                    selectedSeason = season
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val year = yearText.toIntOrNull() ?: 2024
                onSeasonSelected(year, selectedSeason.lowercase())
            }) {
                Text("View")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

private enum class SearchMediaType(val label: String) {
    ANIME("Anime"),
    MANGA("Manga")
}

@Composable
fun HomeSection(
    title: String,
    subtitle: String? = null,
    badges: List<String> = emptyList(),
    items: List<AnimeData>,
    airingDetails: Map<Int, AniListMedia>,
    titleLanguage: TitleLanguage,
    onItemClick: (Int) -> Unit,
    onItemLongClick: (Int, String) -> Unit,
    endButtonLabel: String? = null,
    endButtonIcon: ImageVector = Icons.AutoMirrored.Filled.List,
    endButtonLoading: Boolean = false,
    onEndButtonClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null
) {
    var isCollapsed by rememberSaveable(title) { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (subtitle != null || badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        badges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                CollapseChevronPill(
                    isCollapsed = isCollapsed,
                    onToggle = { isCollapsed = !isCollapsed }
                )
                if (onMoreClick != null) {
                    IconButton(onClick = onMoreClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Show More")
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    HorizontalCard(
                        anime = item,
                        anilistMedia = airingDetails[item.node.id],
                        titleLanguage = titleLanguage,
                        showHomeMeta = true,
                        onClick = { onItemClick(item.node.id) },
                        onLongClick = { onItemLongClick(item.node.id, item.node.getPreferredTitle(titleLanguage)) }
                    )
                }
                if (onEndButtonClick != null && !endButtonLabel.isNullOrBlank()) {
                    item {
                        ContinueListActionCard(
                            label = endButtonLabel,
                            icon = endButtonIcon,
                            isLoading = endButtonLoading,
                            onClick = onEndButtonClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueListActionCard(
    label: String,
    icon: ImageVector,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(124.dp)
            .aspectRatio(0.7f)
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isLoading) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refreshing",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isLoading) "Refreshing" else label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun SeasonalHeroButton(
    season: String,
    year: Int,
    onClick: () -> Unit
) {
    val normalizedSeason = season.lowercase()
    val seasonLabel = normalizedSeason.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val seasonIcon = when (normalizedSeason) {
        "spring" -> Icons.Default.LocalFlorist
        "summer" -> Icons.Default.WbSunny
        "fall" -> Icons.Default.Park
        "winter" -> Icons.Default.AcUnit
        else -> Icons.Default.Movie
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
            ) {
                Icon(
                    imageVector = seasonIcon,
                    contentDescription = "$seasonLabel season",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp).size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Seasonal Chart • $seasonLabel $year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open seasonal chart",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscoveryHeroButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscoveryButtonCluster(
    season: String,
    year: Int,
    onSeasonalClick: () -> Unit,
    onTopAnimeClick: () -> Unit,
    onTopMangaClick: () -> Unit
) {
    val normalizedSeason = season.lowercase()
    val seasonLabel = normalizedSeason.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val seasonIcon = when (normalizedSeason) {
        "spring" -> Icons.Default.LocalFlorist
        "summer" -> Icons.Default.WbSunny
        "fall" -> Icons.Default.Park
        "winter" -> Icons.Default.AcUnit
        else -> Icons.Default.Movie
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(170.dp)
                .clickable(onClick = onSeasonalClick),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = seasonIcon,
                        contentDescription = "$seasonLabel season",
                        modifier = Modifier
                            .padding(horizontal = 13.dp, vertical = 11.dp)
                            .size(30.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Seasonal Chart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$seasonLabel $year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(170.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DiscoveryMiniButton(
                title = "Top 100 Anime",
                icon = Icons.Default.Movie,
                onClick = onTopAnimeClick,
                modifier = Modifier.weight(1f)
            )
            DiscoveryMiniButton(
                title = "Top 100 Manga",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = onTopMangaClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DiscoveryMiniButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RandomAnimeHeroButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Random Anime",
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .size(22.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Random Anime",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Find something unexpected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SeasonalDetailsView(
    categorizedAnime: Map<String, List<AnimeData>>,
    airingDetails: Map<Int, AniListMedia>,
    titleLanguage: TitleLanguage,
    isGridView: Boolean,
    onAnimeClick: (Int) -> Unit,
    onAnimeLongClick: (Int, String) -> Unit
) {
    val collapsedSections = remember { mutableStateMapOf<String, Boolean>() }

    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categorizedAnime.forEach { (type, animeList) ->
                item(span = { GridItemSpan(3) }) {
                    val isCollapsed = collapsedSections[type] ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        CollapseChevronPill(
                            isCollapsed = isCollapsed,
                            onToggle = { collapsedSections[type] = !isCollapsed }
                        )
                    }
                }
                items(animeList) { anime ->
                    AnimatedVisibility(
                        visible = !(collapsedSections[type] ?: false),
                        enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160))
                    ) {
                        HorizontalCard(
                            anime = anime, 
                            anilistMedia = airingDetails[anime.node.id],
                            titleLanguage = titleLanguage,
                            onClick = { onAnimeClick(anime.node.id) },
                            onLongClick = { onAnimeLongClick(anime.node.id, anime.node.getPreferredTitle(titleLanguage)) }
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categorizedAnime.forEach { (type, animeList) ->
                item {
                    val isCollapsed = collapsedSections[type] ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        CollapseChevronPill(
                            isCollapsed = isCollapsed,
                            onToggle = { collapsedSections[type] = !isCollapsed }
                        )
                    }
                }
                items(animeList) { anime ->
                    AnimatedVisibility(
                        visible = !(collapsedSections[type] ?: false),
                        enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(160))
                    ) {
                        AnimeItem(
                            anime = anime,
                            anilistMedia = airingDetails[anime.node.id],
                            titleLanguage = titleLanguage,
                            onClick = { onAnimeClick(anime.node.id) },
                            onLongClick = { onAnimeLongClick(anime.node.id, anime.node.getPreferredTitle(titleLanguage)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapseChevronPill(
    isCollapsed: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
        modifier = Modifier.clickable(onClick = onToggle)
    ) {
        Icon(
            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = if (isCollapsed) "Expand section" else "Collapse section",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

fun getStatusIcon(status: String?): ImageVector {
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
fun HorizontalCard(
    anime: AnimeData,
    anilistMedia: AniListMedia?,
    titleLanguage: TitleLanguage,
    showHomeMeta: Boolean = true,
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
                .width(124.dp)
                .aspectRatio(0.7f)
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { isAnimating = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = anime.node.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Status Icon Overlay (Top Left)
                anime.node.myListStatus?.let { status ->
                    if (status.status != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(4.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f), shape = CircleShape)
                                .padding(4.dp)
                        ) {
                            @Suppress("DEPRECATION")
                            Icon(
                                imageVector = getStatusIcon(status.status),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Score Overlay (Top Right)
                anime.node.myListStatus?.let { status ->
                    if (status.score > 0) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = status.score.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
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
                        text = anime.node.getPreferredTitle(titleLanguage),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )

                    if (showHomeMeta) {
                        val malScoreText = anime.node.meanScore?.let { String.format("%.2f", it) } ?: "N/A"
                        Text(
                            text = "Members: ${formatMembersCount(anime.node.numListUsers)}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "MAL: $malScoreText",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp
                        )
                    }
                    
                    // Next Ep Timer
                    if (anime.node.status == "currently_airing" && anilistMedia?.nextAiringEpisode != null) {
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
                                text = "Ep ${anilistMedia.nextAiringEpisode.episode}: $countdown",
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
fun AnimeItem(
    anime: AnimeData,
    anilistMedia: AniListMedia?,
    titleLanguage: TitleLanguage,
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
                .padding(vertical = 4.dp)
                .alpha(alpha)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { isAnimating = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                AsyncImage(
                    model = anime.node.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp, 108.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = anime.node.getPreferredTitle(titleLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Type: ${anime.node.mediaType?.uppercase() ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Members: ${formatMembersCount(anime.node.numListUsers)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (anime.node.status == "currently_airing" && anilistMedia?.nextAiringEpisode != null) {
                        val timeUntil = anilistMedia.nextAiringEpisode.timeUntilAiring
                        val days = timeUntil / 86400
                        val hours = (timeUntil % 86400) / 3600
                        val mins = (timeUntil % 3600) / 60
                        val countdown = if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
                        
                        Text(
                            text = "Next Ep ${anilistMedia.nextAiringEpisode.episode}: $countdown",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "MAL Score: ${anime.node.meanScore ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    anime.node.myListStatus?.let { status ->
                        if (status.status != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    @Suppress("DEPRECATION")
                                    Icon(
                                        imageVector = getStatusIcon(status.status),
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    if (status.score > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = status.score.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
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

fun MangaData.toAnimeData(): AnimeData {
    return AnimeData(
        node = com.example.myapplication.data.model.AnimeNode(
            id = this.node.id,
            title = this.node.title,
            mainPicture = this.node.mainPicture,
            synopsis = this.node.synopsis,
            meanScore = this.node.meanScore,
            numListUsers = this.node.numListUsers,
            mediaType = this.node.mediaType,
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

private fun formatMembersCount(count: Int?): String {
    val value = count ?: return "N/A"
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
        value >= 1_000 -> String.format("%.1fK", value / 1_000f)
        else -> value.toString()
    }
}
