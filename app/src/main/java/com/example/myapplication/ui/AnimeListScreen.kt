package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnimeListScreen(
    viewModel: AnimeViewModel,
    titleLanguage: TitleLanguage,
    initialTab: Int = 0,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickUpdateEvent by viewModel.quickUpdateEvent.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val airingDetails by viewModel.airingDetails.collectAsState()
    val isGridView by viewModel.getGridModeFlow().collectAsState(initial = true)
    val listFilters by viewModel.listFilters.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSeasonPicker by remember { mutableStateOf(false) }

    val homeTabs = listOf("Anime", "Manga")
    val homePagerState = rememberPagerState(initialPage = initialTab) { homeTabs.size }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

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

    if (uiState is AnimeUiState.SeasonalDetails || uiState is AnimeUiState.MangaDiscoveryDetails || uiState is AnimeUiState.TopDiscoveryDetails || uiState is AnimeUiState.SearchSuccess) {
        BackHandler {
            if (searchQuery.isNotEmpty()) {
                searchQuery = ""
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
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchAnime(it)
                        },
                        placeholder = { 
                            Text(
                                "Search Anime & Manga...", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = null, 
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
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
                    Column {
                        TabRow(selectedTabIndex = homePagerState.currentPage) {
                            homeTabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = homePagerState.currentPage == index,
                                    onClick = { 
                                        coroutineScope.launch {
                                            homePagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(title) }
                                )
                            }
                        }
                        
                        HorizontalPager(state = homePagerState, modifier = Modifier.weight(1f)) { page ->
                            if (page == 0) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    item {
                                        HomeSection(
                                            title = "Seasonal chart (anime)",
                                            items = state.seasonal,
                                            airingDetails = airingDetails,
                                            titleLanguage = titleLanguage,
                                            onItemClick = { onAnimeClick(it) },
                                            onItemLongClick = { id, title -> viewModel.onLongPressAnime(id, title) },
                                            onMoreClick = { viewModel.showSeasonalDetails() }
                                        )
                                    }
                                    item {
                                        HomeSection(
                                            title = "Top Anime",
                                            items = state.topAnime,
                                            airingDetails = airingDetails,
                                            titleLanguage = titleLanguage,
                                            onItemClick = { onAnimeClick(it) },
                                            onItemLongClick = { id, title -> viewModel.onLongPressAnime(id, title) },
                                            onMoreClick = { viewModel.showTopDiscovery(isAnime = true) }
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    item {
                                        HomeSection(
                                            title = "Releasing Now (Manga)",
                                            items = state.publishingManga.map { it.toAnimeData() },
                                            airingDetails = emptyMap(),
                                            titleLanguage = titleLanguage,
                                            onItemClick = { onMangaClick(it) },
                                            onItemLongClick = { id, title -> viewModel.onLongPressManga(id, title) },
                                            onMoreClick = { viewModel.showMangaDiscovery("publishing") }
                                        )
                                    }
                                    item {
                                        HomeSection(
                                            title = "Top Manga",
                                            items = state.topManga.map { it.toAnimeData() },
                                            airingDetails = emptyMap(),
                                            titleLanguage = titleLanguage,
                                            onItemClick = { onMangaClick(it) },
                                            onItemLongClick = { id, title -> viewModel.onLongPressManga(id, title) },
                                            onMoreClick = { viewModel.showTopDiscovery(isAnime = false) }
                                        )
                                    }
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
                            isGridView = isGridView,
                            onGridClick = { viewModel.setGridMode(!isGridView) },
                            currentSort = state.currentSort,
                            onSortChange = { viewModel.showSeasonalDetails(it) },
                            listFilters = listFilters,
                            onFilterToggle = { viewModel.toggleFilter(it) }
                        )

                        SeasonalDetailsView(
                            categorizedAnime = state.categorizedAnime,
                            airingDetails = airingDetails,
                            titleLanguage = titleLanguage,
                            isGridView = isGridView,
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
                            isGridView = isGridView,
                            onGridClick = { viewModel.setGridMode(!isGridView) },
                            currentSort = state.currentSort,
                            onSortChange = { viewModel.showMangaDiscovery(state.type, it) },
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
                            isGridView = isGridView,
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
                            isGridView = isGridView,
                            onGridClick = { viewModel.setGridMode(!isGridView) },
                            currentSort = state.currentSort,
                            onSortChange = { viewModel.showTopDiscovery(state.isAnime, it) },
                            listFilters = listFilters,
                            onFilterToggle = { viewModel.toggleFilter(it) },
                            isTopList = true,
                            isManga = !state.isAnime
                        )

                        TopDiscoveryView(
                            items = state.items,
                            airingDetails = airingDetails,
                            titleLanguage = titleLanguage,
                            isGridView = isGridView,
                            onItemClick = if (state.isAnime) onAnimeClick else onMangaClick,
                            onItemLongClick = { id, title -> 
                                if (state.isAnime) viewModel.onLongPressAnime(id, title) 
                                else viewModel.onLongPressManga(id, title) 
                            }
                        )
                    }
                }
                is AnimeUiState.SearchSuccess -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.animeList.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Anime",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(state.animeList) { animeData ->
                                AnimeItem(
                                    anime = animeData,
                                    anilistMedia = airingDetails[animeData.node.id],
                                    titleLanguage = titleLanguage,
                                    onClick = { onAnimeClick(animeData.node.id) },
                                    onLongClick = { viewModel.onLongPressAnime(animeData.node.id, animeData.node.getPreferredTitle(titleLanguage)) }
                                )
                            }
                        }
                        
                        if (state.mangaList.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Manga",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(state.mangaList) { mangaData ->
                                AnimeItem(
                                    anime = mangaData.toAnimeData(),
                                    anilistMedia = null,
                                    titleLanguage = titleLanguage,
                                    onClick = { onMangaClick(mangaData.node.id) },
                                    onLongClick = { viewModel.onLongPressManga(mangaData.node.id, mangaData.node.getPreferredTitle(titleLanguage)) }
                                )
                            }
                        }
                        
                        if (state.animeList.isEmpty() && state.mangaList.isEmpty()) {
                            item {
                                Text(
                                    text = "No results found.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
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
    
    val sortOptions = if (isTopList) {
        listOf(
            "members" to "Popularity",
            "score" to "Score"
        )
    } else {
        listOf(
            "members" to "Popularity",
            "score" to "Score",
            "title" to "Title"
        )
    }

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
                Text("Sort")
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

@Composable
fun HomeSection(
    title: String,
    items: List<AnimeData>,
    airingDetails: Map<Int, AniListMedia>,
    titleLanguage: TitleLanguage,
    onItemClick: (Int) -> Unit,
    onItemLongClick: (Int, String) -> Unit,
    onMoreClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (onMoreClick != null) {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Show More")
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    Text(
                        text = type,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(animeList) { anime ->
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
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categorizedAnime.forEach { (type, animeList) ->
                item {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(animeList) { anime ->
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
                .width(140.dp)
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
                        .size(80.dp, 120.dp),
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
                        text = "Global Score: ${anime.node.meanScore ?: "N/A"}",
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
