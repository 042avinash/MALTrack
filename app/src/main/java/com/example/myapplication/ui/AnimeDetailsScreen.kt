package com.example.myapplication.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.getPreferredTitle
import com.example.myapplication.data.model.AlternativeTitles
import com.example.myapplication.data.model.AniListMedia
import com.example.myapplication.data.model.AnimeDetailsResponse
import com.example.myapplication.data.model.AnimeNode
import com.example.myapplication.data.model.MyListStatus
import com.example.myapplication.data.model.Recommendation
import com.example.myapplication.data.remote.JikanCharacterData
import com.example.myapplication.data.remote.JikanReviewData
import com.example.myapplication.data.remote.JikanAnimeScoreBucket
import com.example.myapplication.data.remote.JikanStaffData
import com.example.myapplication.data.remote.JikanStreamingData
import com.example.myapplication.data.remote.JikanThemesData
import com.example.myapplication.data.remote.JikanVoiceActor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun AnimeDetailsResponse.getPreferredTitle(language: TitleLanguage): String {
    return when (language) {
        TitleLanguage.ENGLISH -> alternativeTitles?.en.takeIf { !it.isNullOrBlank() } ?: title
        TitleLanguage.ROMAJI -> title
        TitleLanguage.JAPANESE -> alternativeTitles?.ja.takeIf { !it.isNullOrBlank() } ?: title
    }
}

fun downloadImage(context: Context, url: String, title: String) {
    try {
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("$title Image")
            .setDescription("Downloading picture")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("image/jpeg")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${safeTitle}_${System.currentTimeMillis()}.jpg"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started in Downloads", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to download", Toast.LENGTH_SHORT).show()
    }
}

private fun requiresLegacyStoragePermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

private fun hasDownloadPermission(context: Context): Boolean {
    return !requiresLegacyStoragePermission() ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailsScreen(
    viewModel: AnimeDetailsViewModel,
    titleLanguage: TitleLanguage,
    onBackClick: () -> Unit,
    onReviewsClick: (Int) -> Unit,
    onAnimeClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isPullRefreshing by remember { mutableStateOf(false) }

    val topBarTitle = if (uiState is AnimeDetailsUiState.Success) {
        (uiState as AnimeDetailsUiState.Success).details.getPreferredTitle(titleLanguage)
    } else "Details"

    LaunchedEffect(uiState, isPullRefreshing) {
        if (!isPullRefreshing) return@LaunchedEffect
        when (uiState) {
            is AnimeDetailsUiState.Success,
            is AnimeDetailsUiState.Error -> isPullRefreshing = false
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = topBarTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is AnimeDetailsUiState.Success) {
                        FilledTonalButton(
                            onClick = {
                                val animeId = (uiState as AnimeDetailsUiState.Success).details.id
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://myanimelist.net/anime/$animeId")
                                    )
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("MAL")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                isPullRefreshing = true
                viewModel.loadDetails(forceRefresh = true)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is AnimeDetailsUiState.Loading -> {
                        if (!isPullRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is AnimeDetailsUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is AnimeDetailsUiState.Success -> {
                        AnimeDetailsContent(
                            details = state.details,
                            characters = state.characters,
                            staff = state.staff,
                            recommendations = state.recommendations,
                            themes = state.themes,
                            reviews = state.reviews,
                            allReviewsCount = state.allReviewsCount,
                            isRecommendationsLoaded = state.isRecommendationsLoaded,
                            isRecommendationsLoading = state.isRecommendationsLoading,
                            isPeopleLoaded = state.isPeopleLoaded,
                            isPeopleLoading = state.isPeopleLoading,
                            isReviewsLoaded = state.isReviewsLoaded,
                            isReviewsLoading = state.isReviewsLoading,
                            streaming = state.streaming,
                            scoreDistribution = state.scoreDistribution,
                            isCommunityStatsRefreshing = state.isCommunityStatsRefreshing,
                            airingMedia = state.airingMedia,
                            recommendationMeta = state.recommendationMeta,
                            titleLanguage = titleLanguage,
                            onReviewsClick = { onReviewsClick(state.details.id) },
                            onLoadReviews = { viewModel.loadReviews() },
                            onLoadRecommendations = { viewModel.loadRecommendations() },
                            onLoadPeople = { viewModel.loadPeople() },
                            onRefreshCommunityStats = { viewModel.refreshCommunityStats() },
                            onAnimeClick = onAnimeClick,
                            onUpdateStatus = { status, isRewatching, score, eps, priority, timesRewatched, rewatchVal, tags, comments, start, finish ->
                                viewModel.updateListStatus(status, isRewatching, score, eps, priority, timesRewatched, rewatchVal, tags, comments, start, finish)
                            },
                            onDeleteStatus = { viewModel.deleteFromList() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeDetailsContent(
    details: AnimeDetailsResponse, 
    characters: List<JikanCharacterData>,
    staff: List<JikanStaffData>,
    recommendations: List<Recommendation>,
    themes: JikanThemesData?,
    reviews: List<JikanReviewData>,
    allReviewsCount: Int,
    isRecommendationsLoaded: Boolean,
    isRecommendationsLoading: Boolean,
    isPeopleLoaded: Boolean,
    isPeopleLoading: Boolean,
    isReviewsLoaded: Boolean,
    isReviewsLoading: Boolean,
    streaming: List<JikanStreamingData>,
    scoreDistribution: List<JikanAnimeScoreBucket>,
    isCommunityStatsRefreshing: Boolean,
    airingMedia: AniListMedia?,
    recommendationMeta: Map<Int, RecommendationCardMeta> = emptyMap(),
    titleLanguage: TitleLanguage,
    onReviewsClick: () -> Unit,
    onLoadReviews: () -> Unit,
    onLoadRecommendations: () -> Unit,
    onLoadPeople: () -> Unit,
    onRefreshCommunityStats: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onUpdateStatus: (String?, Boolean?, Int?, Int?, Int?, Int?, Int?, String?, String?, String?, String?) -> Unit,
    onDeleteStatus: () -> Unit
) {
    val context = LocalContext.current
    var isSynopsisExpanded by remember { mutableStateOf(false) }

    var selectedPicture by remember { mutableStateOf<String?>(null) }
    var showStorageSettingsPrompt by remember { mutableStateOf(false) }
    var showAllCast by remember { mutableStateOf(false) }
    var showAllPeople by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRelatedPopup by remember { mutableStateOf(false) }
    var showRecommendationsPopup by remember { mutableStateOf(false) }
    var communityStatsInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    val displayVAs = remember(characters) {
        val allVAs = characters.flatMap { it.voice_actors }
        val japaneseVAs = allVAs.filter { it.language == "Japanese" }.distinctBy { it.person.mal_id }
        if (japaneseVAs.isNotEmpty()) japaneseVAs else allVAs.distinctBy { it.person.mal_id }
    }
    val displayStaff = remember(staff) {
        staff.distinctBy { it.person.mal_id }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete from list") },
            text = { Text("Are you sure you want to delete this anime from your list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteStatus()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showStorageSettingsPrompt) {
        AlertDialog(
            onDismissRequest = { showStorageSettingsPrompt = false },
            title = { Text("Storage Access Needed") },
            text = { Text("This device requires storage permission to save images to Downloads. Open app settings to allow it.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStorageSettingsPrompt = false
                        openAppSettings(context)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStorageSettingsPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRelatedPopup) {
        DetailsGridPopup(
            title = "Related Anime",
            entries = details.relatedAnime.orEmpty().map {
                DetailsPopupEntry(
                    node = it.node,
                    relationLabel = it.relationTypeFormatted
                )
            },
            recommendationMeta = recommendationMeta,
            titleLanguage = titleLanguage,
            onAnimeClick = onAnimeClick,
            onDismiss = { showRelatedPopup = false }
        )
    }

    if (showRecommendationsPopup) {
        DetailsGridPopup(
            title = "Recommendations",
            entries = recommendations.map {
                DetailsPopupEntry(
                    node = it.node,
                    relationLabel = null
                )
            },
            recommendationMeta = recommendationMeta,
            titleLanguage = titleLanguage,
            isLoading = isRecommendationsLoading,
            onAnimeClick = onAnimeClick,
            onDismiss = { showRecommendationsPopup = false }
        )
    }

    communityStatsInfo?.let { (title, message) ->
        AlertDialog(
            onDismissRequest = { communityStatsInfo = null },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { communityStatsInfo = null }) { Text("OK") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // Header Image and Main Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                AsyncImage(
                    model = details.mainPicture?.large ?: details.mainPicture?.medium,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AsyncImage(
                        model = details.mainPicture?.medium,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp, 180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = details.getPreferredTitle(titleLanguage),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Score", tint = Color.Yellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${details.mean ?: "N/A"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${details.mediaType?.uppercase() ?: "N/A"} • ${details.status?.replace("_", " ")?.uppercase() ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        if (details.numEpisodes != null && details.numEpisodes > 0) {
                            Text(
                                text = "${details.numEpisodes} Episodes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                        
                        // Next Episode Timer
                        if (details.status == "currently_airing") {
                            val nextAiring = airingMedia?.nextAiringEpisode
                            val nextEpisode = nextAiring?.episode
                            val airedEpisodes = nextEpisode?.minus(1)?.coerceAtLeast(0)
                            val countdown = nextAiring?.timeUntilAiring?.let { timeUntil ->
                                val days = timeUntil / 86400
                                val hours = (timeUntil % 86400) / 3600
                                val mins = (timeUntil % 3600) / 60
                                if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
                            } ?: "Unknown"
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Aired: ${airedEpisodes ?: "?"} | Next Ep ${nextEpisode ?: "?"}: ${if (countdown == "Unknown") "?" else countdown}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons / My List Status
        item {
            if (details.myListStatus != null) {
                val statusLabel = if (details.myListStatus.isRewatching) {
                    "REWATCHING"
                } else {
                    details.myListStatus.status
                        ?.replace("_", " ")
                        ?.uppercase()
                        ?: "UNKNOWN"
                }
                val progressLabel = "${details.myListStatus.numEpisodesWatched}/${details.numEpisodes ?: "?"}"
                val scoreLabel = if (details.myListStatus.score > 0) "Score ${details.myListStatus.score}" else "Score -"

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "My List",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "$statusLabel | $progressLabel | $scoreLabel",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { showEditDialog = true },
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 14.dp, top = 11.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to List",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

        }

        // Genres
        if (!details.genres.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Genres",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(details.genres) { genre ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(genre.name) }
                            )
                        }
                    }
                }
            }
        }

        // Synopsis
        if (!details.synopsis.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).animateContentSize()) {
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = details.synopsis,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isSynopsisExpanded) "Show Less" else "...More",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        // Characters
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "Characters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (characters.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(characters.take(5)) { charData ->
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(charData.character.url)))
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = charData.character.images.jpg?.image_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = charData.character.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = charData.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (characters.size > 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(120.dp)
                                        .clickable { showAllCast = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "View\nAll",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    DetailsInfoMessageCard(
                        title = "Characters Unavailable",
                        message = if (isPeopleLoading) {
                            "Loading character credits..."
                        } else {
                            "No character credits were returned. This can happen for newer, niche, or lightly documented titles."
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Voice Actors
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Voice Actors",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (displayVAs.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayVAs.take(5)) { vaData ->
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(vaData.person.url)))
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = vaData.person.images.jpg?.image_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = vaData.person.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = vaData.language,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (displayVAs.size > 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(120.dp)
                                        .clickable { showAllCast = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "View\nAll",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val voiceActorReason = when {
                        isPeopleLoading -> "Loading voice actor credits..."
                        characters.isEmpty() -> "Voice actor credits depend on character data, and no character credits were returned."
                        else -> "Characters were returned, but no voice actor credits were included. This can happen when cast data is incomplete or not localized yet."
                    }
                    DetailsInfoMessageCard(
                        title = "Voice Actors Unavailable",
                        message = voiceActorReason,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Staff
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Staff",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!isPeopleLoaded && !isPeopleLoading) {
                    Button(
                        onClick = onLoadPeople,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Show Staff Credits")
                    }
                } else if (isPeopleLoading) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading people...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (displayStaff.isEmpty()) {
                    DetailsInfoMessageCard(
                        title = "Staff Credits Unavailable",
                        message = "No staff credits were returned. This can happen when people data is incomplete, delayed, or unavailable from the source.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    TextButton(
                        onClick = onLoadPeople,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Retry Staff Load")
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayStaff.take(5)) { staffData ->
                            Column(
                                modifier = Modifier
                                    .width(96.dp)
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(staffData.person.url)))
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = staffData.person.images.jpg?.image_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = staffData.person.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = staffData.positions.firstOrNull() ?: "Staff",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (displayStaff.size > 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(120.dp)
                                        .clickable { showAllPeople = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "View\nAll",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Information Details
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow("English", details.alternativeTitles?.en ?: "N/A", copyOnLongPress = true)
                InfoRow("Japanese", details.alternativeTitles?.ja ?: "N/A", copyOnLongPress = true)
                InfoRow("Romaji", details.title.takeIf { it.isNotBlank() } ?: "N/A", copyOnLongPress = true)
                InfoRow("Synonyms", details.alternativeTitles?.synonyms?.joinToString()?.takeIf { it.isNotEmpty() } ?: "N/A")
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("Rank", "#${details.rank ?: "N/A"}")
                InfoRow("Popularity", "#${details.popularity ?: "N/A"}")
                InfoRow("Source", details.source?.replace("_", " ")?.capitalize() ?: "N/A")
                InfoRow(
                    "Episode Duration",
                    details.averageEpisodeDuration
                        ?.takeIf { it > 0 }
                        ?.let { seconds ->
                            val minutes = seconds / 60
                            val remainingSeconds = seconds % 60
                            if (remainingSeconds == 0) "${minutes} min" else "${minutes} min ${remainingSeconds} sec"
                        } ?: "N/A"
                )
                InfoRow("Aired", "${details.startDate ?: "?"} to ${details.endDate ?: "?"}")
                InfoRow("Studios", details.studios?.joinToString { it.name } ?: "N/A")
                InfoRow("Rating", formatMalRating(details.rating))
            }
        }

        // Streaming Platforms
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Available On",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (streaming.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(streaming) { stream ->
                            AssistChip(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(stream.url)))
                                },
                                label = { Text(stream.name) }
                            )
                        }
                    }
                } else {
                    val availabilityReason = when {
                        details.status.equals("not_yet_aired", ignoreCase = true) ->
                            "This anime has not aired yet, so official streaming links may not be listed."
                        details.status.equals("currently_airing", ignoreCase = true) ->
                            "No official streaming links were returned yet. Availability can lag behind airing updates or vary by region."
                        else ->
                            "No official streaming links were returned. This can happen because availability varies by region, the source has no links, or the title is not currently streaming."
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Streaming Links Unavailable",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = availabilityReason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { showRelatedPopup = true },
                    modifier = Modifier.weight(1f),
                    enabled = !details.relatedAnime.isNullOrEmpty()
                ) {
                    Text("Related Anime")
                }
                FilledTonalButton(
                    onClick = {
                        if (!isRecommendationsLoaded) onLoadRecommendations()
                        showRecommendationsPopup = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRecommendationsLoading
                ) {
                    if (isRecommendationsLoading && !isRecommendationsLoaded) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Recommendations")
                }
            }
        }

        // Reviews
        item {
            FilledTonalButton(
                onClick = onReviewsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("User Reviews")
            }
        }

        // Pictures
        if (!details.pictures.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pictures",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(details.pictures) { pic ->
                            val picUrl = pic.large ?: pic.medium
                            AsyncImage(
                                model = picUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .height(250.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPicture = picUrl },
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }

        if (isReviewsLoaded && reviews.isNotEmpty()) {
            items(reviews) { review ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = review.user.images.jpg?.image_url,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(review.user.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(review.date.take(10), style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val tag = review.tags.firstOrNull() ?: ""
                                if (tag.isNotEmpty()) {
                                    val tagColor = when {
                                        tag.contains("Not", ignoreCase = true) -> Color.Red
                                        tag.contains("Mixed", ignoreCase = true) -> Color.Gray
                                        else -> Color(0xFF4CAF50) // Green
                                    }
                                    Text(
                                        text = tag, 
                                        color = tagColor, 
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Icon(Icons.Default.Star, contentDescription = "Score", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${review.score}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (review.is_spoiler && !expanded) "[Contains Spoilers] Click to read" else review.review,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (expanded) "Show Less" else "Read More",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        } else if (isReviewsLoading || isReviewsLoaded) {
            item {
                val reviewMessage = when {
                    isReviewsLoading -> "Fetching reviews from Jikan..."
                    allReviewsCount > 0 -> "Jikan reports %,d reviews, but the review preview did not return entries yet.".format(allReviewsCount)
                    else -> "No public reviews are available for this anime yet."
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isReviewsLoading) "Loading Reviews" else "Reviews Unavailable",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reviewMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Themes
        if (themes != null && (themes.openings.isNotEmpty() || themes.endings.isNotEmpty())) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Opening & Ending Themes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (themes.openings.isNotEmpty()) {
                        Text("Openings:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
                        themes.openings.forEach { theme ->
                            ThemeLink(theme) {
                                val url = "https://www.youtube.com/results?search_query=${Uri.encode(theme)}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }

                    if (themes.endings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Endings:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))
                        themes.endings.forEach { theme ->
                            ThemeLink(theme) {
                                val url = "https://www.youtube.com/results?search_query=${Uri.encode(theme)}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Stats Donut Chart
        if (details.statistics?.status != null || scoreDistribution.isNotEmpty() || (details.numScoringUsers ?: 0) > 0) {
            item {
                val stats = details.statistics?.status
                val watching = stats?.watching ?: 0
                val completed = stats?.completed ?: 0
                val onHold = stats?.onHold ?: 0
                val dropped = stats?.dropped ?: 0
                val planToWatch = stats?.planToWatch ?: 0
                
                val total = watching + completed + onHold + dropped + planToWatch
                val scoringUsers = details.numScoringUsers ?: 0
                val hasScoreDistribution = scoreDistribution.any { it.votes > 0 || it.percentage > 0.0 }
                if (total > 0 || hasScoreDistribution || scoringUsers > 0) {
                    val members = details.statistics?.numListUsers ?: details.numListUsers ?: total
                    fun pct(value: Int): Float = if (total > 0) (value * 100f / total) else 0f
                    val completionRate = pct(completed)
                    val dropRate = pct(dropped)
                    val watchingRate = pct(watching)
                    val planRate = pct(planToWatch)
                    val onHoldRate = pct(onHold)
                    val showTrendingCard = watchingRate > 45f
                    val showCompletionCard = completionRate > 55f
                    val showDropCard = dropRate >= 12f && dropRate >= completionRate * 0.30f
                    val showOnHoldCard = onHoldRate >= 8f
                    val showPlannedCard = planRate > 50f
                    val scoreBuckets = scoreDistribution.associateBy { it.score }
                    fun scoreShare(vararg scores: Int): Float {
                        return scores.sumOf { score -> scoreBuckets[score]?.percentage ?: 0.0 }.toFloat()
                    }
                    val highScoreShare = scoreShare(9, 10)
                    val midScoreShare = scoreShare(5, 6)
                    val lowScoreShare = scoreShare(1, 2, 3, 4)
                    val score6Share = scoreShare(6)
                    val positiveScoreShare = scoreShare(7, 8, 9, 10)
                    val largestBucketShare = scoreDistribution.maxOfOrNull { it.percentage }?.toFloat() ?: 0f
                    val showCommunityLovedCard = highScoreShare >= 45f
                    val showPolarizingCard = highScoreShare >= 30f && lowScoreShare >= 15f
                    val showMixedReceptionCard = largestBucketShare < 25f &&
                        highScoreShare >= 20f &&
                        midScoreShare >= 20f &&
                        lowScoreShare >= 10f
                    val showNicheAppealCard = highScoreShare >= 35f && members < 250_000
                    val showColdReceptionCard = lowScoreShare >= 20f
                    val showSafePickCard = lowScoreShare <= 3f &&
                        positiveScoreShare >= 70f &&
                        !showCommunityLovedCard &&
                        !showPolarizingCard &&
                        !showColdReceptionCard
                    val showMostlyMidCard = score6Share >= 25f && highScoreShare < 35f && lowScoreShare < 25f
                    val statPills = listOfNotNull(
                        if (showTrendingCard) {
                            StatsPillData(
                                label = "Trending Now",
                                icon = Icons.Default.TrendingUp,
                                containerColor = Color(0xFF1565C0),
                                contentColor = Color.White,
                                dialogTitle = "Trending Now",
                                infoText = "This anime has a lot of active watchers right now, so it is likely being followed while it airs or recently gained momentum."
                            )
                        } else null,
                        if (showCommunityLovedCard) {
                            StatsPillData(
                                label = "Community Loved",
                                icon = Icons.Default.Star,
                                containerColor = Color(0xFFD81B60),
                                contentColor = Color.White,
                                dialogTitle = "Community Loved",
                                infoText = "A large share of score votes land at the very top, so the viewers who rate it tend to really love it."
                            )
                        } else null,
                        if (showPolarizingCard) {
                            StatsPillData(
                                label = "Polarizing",
                                icon = Icons.Default.Warning,
                                containerColor = Color(0xFF6A1B9A),
                                contentColor = Color.White,
                                dialogTitle = "Polarizing",
                                infoText = "Both very high and very low scores have a strong presence, so viewers seem split on this one."
                            )
                        } else null,
                        if (showDropCard) {
                            StatsPillData(
                                label = "Highly Dropped",
                                icon = Icons.Default.TrendingDown,
                                containerColor = Color(0xFFC62828),
                                contentColor = Color.White,
                                dialogTitle = "Highly Dropped",
                                infoText = "A noticeable share of users stopped watching compared with how many finished it, so it may lose some viewers along the way."
                            )
                        } else null,
                        if (showColdReceptionCard) {
                            StatsPillData(
                                label = "Cold Reception",
                                icon = Icons.Default.TrendingDown,
                                containerColor = Color(0xFF455A64),
                                contentColor = Color.White,
                                dialogTitle = "Cold Reception",
                                infoText = "Lower scores make up a noticeable part of the vote spread, so this anime may not land well for many viewers."
                            )
                        } else null,
                        if (showNicheAppealCard) {
                            StatsPillData(
                                label = "Niche Appeal",
                                icon = Icons.Default.Star,
                                containerColor = Color(0xFF00897B),
                                contentColor = Color.White,
                                dialogTitle = "Niche Appeal",
                                infoText = "The score spread is strong, but the audience is relatively smaller, so it may be especially loved by its niche."
                            )
                        } else null,
                        if (showCompletionCard) {
                            StatsPillData(
                                label = "Highly Completed",
                                icon = Icons.Default.CheckCircle,
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White,
                                dialogTitle = "Highly Completed",
                                infoText = "A large share of users who added this anime have finished it, which usually means people tend to stick with it."
                            )
                        } else null,
                        if (showMixedReceptionCard) {
                            StatsPillData(
                                label = "Mixed Reception",
                                icon = Icons.Default.Star,
                                containerColor = Color(0xFF5D6D7E),
                                contentColor = Color.White,
                                dialogTitle = "Mixed Reception",
                                infoText = "The score votes are spread across several ranges, so there is no clear agreement around one rating."
                            )
                        } else null,
                        if (showOnHoldCard) {
                            StatsPillData(
                                label = "On Hold Risk",
                                icon = Icons.Default.Warning,
                                containerColor = Color(0xFFFBC02D),
                                contentColor = Color(0xFF212121),
                                dialogTitle = "On Hold Risk",
                                infoText = "More users than usual have paused this anime, which can mean pacing, timing, or interest drops off for some viewers."
                            )
                        } else null,
                        if (showMostlyMidCard) {
                            StatsPillData(
                                label = "Mostly Mid",
                                icon = Icons.Default.Star,
                                containerColor = Color(0xFF757575),
                                contentColor = Color.White,
                                dialogTitle = "Mostly Mid",
                                infoText = "Score 6 takes up a notable share, which points to a more okay-or-decent reception than a strongly loved one."
                            )
                        } else null,
                        if (showSafePickCard) {
                            StatsPillData(
                                label = "Safe Pick",
                                icon = Icons.Default.CheckCircle,
                                containerColor = Color(0xFF43A047),
                                contentColor = Color.White,
                                dialogTitle = "Safe Pick",
                                infoText = "Very few voters rate it low, so even if not everyone adores it, most viewers seem at least okay with it."
                            )
                        } else null,
                        if (showPlannedCard) {
                            StatsPillData(
                                label = "Planned by Many",
                                icon = Icons.Default.Schedule,
                                containerColor = Color(0xFF546E7A),
                                contentColor = Color.White,
                                dialogTitle = "Planned by Many",
                                infoText = "A large share of users have saved this anime to watch later, so there is strong interest even if many have not started it yet."
                            )
                        } else null
                    ).take(5)

                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Community Stats",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onRefreshCommunityStats,
                                enabled = !isCommunityStatsRefreshing
                            ) {
                                if (isCommunityStatsRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh community stats"
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            items(statPills) { pill ->
                                StatsTrendPill(
                                    label = pill.label,
                                    icon = pill.icon,
                                    containerColor = pill.containerColor,
                                    contentColor = pill.contentColor,
                                    infoText = pill.infoText,
                                    onInfoClick = { communityStatsInfo = pill.dialogTitle to it },
                                    modifier = Modifier.width(154.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var currentStartAngle = -90f
                                    val strokeWidth = 24.dp.toPx()
                                    
                                    val pieces = listOf(
                                        Pair(completed, Color(0xFF4CAF50)), // Green
                                        Pair(watching, Color(0xFF2196F3)),  // Blue
                                        Pair(planToWatch, Color(0xFF9E9E9E)), // Gray
                                        Pair(onHold, Color(0xFFFFC107)),   // Yellow
                                        Pair(dropped, Color(0xFFF44336))    // Red
                                    )
                                    
                                    pieces.forEach { (value, color) ->
                                        if (value > 0) {
                                            val sweepAngle = (value.toFloat() / total) * 360f
                                            drawArc(
                                                color = color,
                                                startAngle = currentStartAngle,
                                                sweepAngle = sweepAngle,
                                                useCenter = false,
                                                style = Stroke(strokeWidth),
                                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                            )
                                            currentStartAngle += sweepAngle
                                        }
                                    }
                                }
                                Text("Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.offset(y = (-8).dp))
                                Text("%,d".format(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = 8.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(32.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                LegendItem(Color(0xFF4CAF50), "Completed", completed, pct(completed))
                                LegendItem(Color(0xFF2196F3), "Watching", watching, pct(watching))
                                LegendItem(Color(0xFF9E9E9E), "Plan to Watch", planToWatch, pct(planToWatch))
                                LegendItem(Color(0xFFFFC107), "On Hold", onHold, pct(onHold))
                                LegendItem(Color(0xFFF44336), "Dropped", dropped, pct(dropped))
                            }
                        }

                        if (hasScoreDistribution) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Score Distribution",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Scored by %,d".format(scoringUsers),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val scoreMap = scoreDistribution.associateBy { it.score }
                            val maxVotes = (scoreDistribution.maxOfOrNull { it.votes } ?: 0).coerceAtLeast(1)
                            (10 downTo 1).forEach { score ->
                                val bucket = scoreMap[score]
                                val votes = bucket?.votes ?: 0
                                val percent = bucket?.percentage?.toFloat() ?: 0f
                                val fillRatio = votes.toFloat() / maxVotes.toFloat()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = score.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(22.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fillRatio.coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", percent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(44.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        } else {
                            val unavailableReason = when {
                                isCommunityStatsRefreshing -> "Refreshing score distribution..."
                                scoringUsers > 0 -> "MAL reports %,d scoring users, but the score buckets source did not return a distribution yet.".format(scoringUsers)
                                details.mean != null -> "MAL has an average score, but no score bucket breakdown is available yet."
                                else -> "No public score distribution is available for this anime yet."
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Score Distribution Unavailable",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = unavailableReason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Full Screen Picture View & Download
    if (selectedPicture != null) {
        Dialog(
            onDismissRequest = { selectedPicture = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.9f))) {
                AsyncImage(
                    model = selectedPicture,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedPicture = null },
                        modifier = Modifier.background(Color.Black.copy(alpha=0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Button(
                        onClick = {
                            if (hasDownloadPermission(context)) {
                                downloadImage(context, selectedPicture!!, details.getPreferredTitle(titleLanguage))
                            } else {
                                showStorageSettingsPrompt = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer, 
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Download")
                    }
                }
            }
        }
    }

    // Dialog for Full Cast (Characters + VAs in multiple languages)
    if (showAllCast) {
        Dialog(
            onDismissRequest = { showAllCast = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAllCast = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                        }
                        Text("Full Cast & Crew", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(characters) { charData ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Character fixed on left
                                    Row(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(charData.character.url))) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = charData.character.images.jpg?.image_url,
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(charData.character.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(charData.role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                    
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(16.dp).padding(horizontal = 4.dp), 
                                        tint = Color.Gray
                                    )

                                    // Side-scrolling VAs on right
                                    val sortedVAs = remember(charData.voice_actors) {
                                        charData.voice_actors.sortedWith(compareByDescending<JikanVoiceActor> { it.language == "Japanese" }
                                            .thenByDescending { it.language == "English" }
                                            .thenBy { it.language })
                                    }

                                    LazyRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        items(sortedVAs) { va ->
                                            Row(
                                                modifier = Modifier
                                                    .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(va.person.url))) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(va.person.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(va.language, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                AsyncImage(
                                                    model = va.person.images.jpg?.image_url,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(50.dp).clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAllPeople) {
        Dialog(
            onDismissRequest = { showAllPeople = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAllPeople = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                        }
                        Text("People", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayStaff) { staffData ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(staffData.person.url)))
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = staffData.person.images.jpg?.image_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = staffData.person.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = staffData.positions.joinToString().ifBlank { "Staff" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditListStatusDialog(
            currentStatus = details.myListStatus ?: MyListStatus(),
            maxEpisodes = details.numEpisodes ?: 0,
            onDismiss = { showEditDialog = false },
            onSave = { status, isRewatching, score, eps, priority, timesRewatched, rewatchVal, tags, comments, start, finish ->
                onUpdateStatus(status, isRewatching, score, eps, priority, timesRewatched, rewatchVal, tags, comments, start, finish)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun DetailsGridPopup(
    title: String,
    entries: List<DetailsPopupEntry>,
    recommendationMeta: Map<Int, RecommendationCardMeta>,
    titleLanguage: TitleLanguage,
    isLoading: Boolean = false,
    onAnimeClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                when {
                    isLoading && entries.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    entries.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No entries available")
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            gridItems(
                                items = entries,
                                key = { "${it.node.id}_${it.relationLabel ?: ""}" }
                            ) { entry ->
                                RecommendationGridCard(
                                    anime = entry.node,
                                    relationLabel = entry.relationLabel,
                                    meta = recommendationMeta[entry.node.id],
                                    titleLanguage = titleLanguage,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onDismiss()
                                        onAnimeClick(entry.node.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class DetailsPopupEntry(
    val node: AnimeNode,
    val relationLabel: String?
)

@Composable
private fun RecommendationGridCard(
    anime: AnimeNode,
    relationLabel: String? = null,
    meta: RecommendationCardMeta? = null,
    titleLanguage: TitleLanguage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val resolvedListStatus = anime.myListStatus ?: meta?.myListStatus
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = anime.mainPicture?.medium,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            resolvedListStatus?.let { status ->
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

            resolvedListStatus?.let { status ->
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
                    .height(if (relationLabel.isNullOrBlank()) 92.dp else 106.dp)
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
                    text = anime.getPreferredTitle(titleLanguage),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (relationLabel.isNullOrBlank()) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                if (!relationLabel.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = relationLabel,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                val resolvedMean = meta?.mean ?: anime.meanScore
                val resolvedMembers = meta?.members ?: anime.numListUsers
                val malScoreText = resolvedMean?.let { String.format("%.2f", it) } ?: "N/A"
                Text(
                    text = "Members: ${formatMembersCountLocal(resolvedMembers)}",
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
        }
    }
}

private fun formatMembersCountLocal(count: Int?): String {
    if (count == null) return "N/A"
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000f)
        else -> count.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListStatusDialog(
    currentStatus: MyListStatus,
    maxEpisodes: Int,
    onDismiss: () -> Unit,
    onSave: (String?, Boolean?, Int?, Int?, Int?, Int?, Int?, String?, String?, String?, String?) -> Unit
) {
    var status by remember { mutableStateOf(currentStatus.status ?: "plan_to_watch") }
    var score by remember { mutableIntStateOf(currentStatus.score) }
    var episodes by remember { mutableIntStateOf(currentStatus.numEpisodesWatched) }
    var startDate by remember { mutableStateOf(currentStatus.startDate ?: "") }
    var endDate by remember { mutableStateOf(currentStatus.finishDate ?: "") }
    var isRewatching by remember { mutableStateOf(currentStatus.isRewatching) }
    var priority by remember { mutableIntStateOf(currentStatus.priority) }
    var timesRewatched by remember { mutableIntStateOf(currentStatus.numTimesRewatched) }
    var rewatchValue by remember { mutableIntStateOf(currentStatus.rewatchValue) }
    var tags by remember { mutableStateOf(currentStatus.tags.joinToString(", ")) }
    var notes by remember { mutableStateOf(currentStatus.comments ?: "") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try { 
                dateFormat.parse(startDate)?.time ?: System.currentTimeMillis() 
            } catch (e: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = dateFormat.format(Date(it))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try { 
                dateFormat.parse(endDate)?.time ?: System.currentTimeMillis() 
            } catch (e: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = dateFormat.format(Date(it))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit List Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status Icons
                    Text("Status", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatusIcon(Icons.Default.Visibility, "Watching", status == "watching") {
                            status = "watching"
                            if (startDate.isEmpty()) startDate = dateFormat.format(Calendar.getInstance().time)
                        }
                        StatusIcon(Icons.Default.CheckCircle, "Completed", status == "completed") {
                            status = "completed"
                            if (maxEpisodes > 0) episodes = maxEpisodes
                            if (endDate.isEmpty()) endDate = dateFormat.format(Calendar.getInstance().time)
                        }
                        StatusIcon(Icons.Default.PauseCircle, "On-Hold", status == "on_hold") {
                            status = "on_hold"
                        }
                        StatusIcon(Icons.Default.Schedule, "Planned", status == "plan_to_watch") { 
                            status = "plan_to_watch"
                            episodes = 0
                        }
                        StatusIcon(Icons.Default.Cancel, "Dropped", status == "dropped") { 
                            status = "dropped" 
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Episodes
                    CounterField(
                        label = "Episodes Watched",
                        value = episodes,
                        onValueChange = {
                            val bounded = it.coerceIn(0, if (maxEpisodes > 0) maxEpisodes else 9999)
                            val wasZeroToPositiveFromPlanned = status == "plan_to_watch" && episodes == 0 && bounded > 0
                            if (bounded != episodes && status in setOf("on_hold", "dropped", "plan_to_watch", "completed")) {
                                status = "watching"
                            }
                            if (wasZeroToPositiveFromPlanned && startDate.isBlank()) {
                                startDate = dateFormat.format(Calendar.getInstance().time)
                            }
                            episodes = bounded
                        },
                        max = if (maxEpisodes > 0) maxEpisodes else null
                    )

                    // Score
                    CounterField(
                        label = "Score",
                        value = score,
                        onValueChange = { score = it.coerceIn(0, 10) },
                        max = 10
                    )

                    // Start Date
                    Text("Start Date", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showStartDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            }
                        )
                        TextButton(onClick = { startDate = dateFormat.format(Calendar.getInstance().time) }) {
                            Text("Today")
                        }
                        IconButton(onClick = { startDate = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear start date")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // End Date
                    Text("Finish Date", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showEndDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            }
                        )
                        TextButton(onClick = { endDate = dateFormat.format(Calendar.getInstance().time) }) {
                            Text("Today")
                        }
                        IconButton(onClick = { endDate = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear finish date")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Priority
                    Text("Priority", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PriorityChip("Low", priority == 0) { priority = 0 }
                        PriorityChip("Medium", priority == 1) { priority = 1 }
                        PriorityChip("High", priority == 2) { priority = 2 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rewatching
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRewatching, onCheckedChange = { isRewatching = it })
                        Text("Rewatching")
                    }
                    
                    CounterField(label = "Total Rewatches", value = timesRewatched, onValueChange = { timesRewatched = it.coerceAtLeast(0) })
                    CounterField(label = "Rewatch Value", value = rewatchValue, onValueChange = { rewatchValue = it.coerceIn(0, 5) }, max = 5)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tags & Notes
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                Button(
                    onClick = {
                        val normalizedEpisodes = if (status == "plan_to_watch") 0 else episodes
                        onSave(status, isRewatching, score, normalizedEpisodes, priority, timesRewatched, rewatchValue, tags, notes, startDate, endDate)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CounterField(label: String, value: Int, onValueChange: (Int) -> Unit, max: Int? = null) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onValueChange(value - 1) }) {
                Text("-", style = MaterialTheme.typography.headlineMedium)
            }
            OutlinedTextField(
                value = value.toString(),
                onValueChange = { newValue ->
                    if (newValue.isBlank()) {
                        onValueChange(0)
                    } else {
                        newValue.toIntOrNull()?.let { onValueChange(it) }
                    }
                },
                modifier = Modifier.width(80.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                singleLine = true
            )
            IconButton(onClick = { onValueChange(value + 1) }) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
            if (max != null) {
                Text("/ $max", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun PriorityChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int,
    percent: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%,d (%s%%)".format(
                count,
                String.format(Locale.US, "%.1f", percent)
            ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class StatsPillData(
    val label: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val dialogTitle: String,
    val infoText: String
)

@Composable
private fun DetailsInfoMessageCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatsTrendPill(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    infoText: String,
    onInfoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onInfoClick(infoText) },
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ThemeLink(theme: String, onClick: () -> Unit) {
    Text(
        text = theme,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    InfoRow(label = label, value = value, copyOnLongPress = false)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InfoRow(label: String, value: String, copyOnLongPress: Boolean) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val canCopy = copyOnLongPress && value.isNotBlank() && value != "N/A"
    val copyText: () -> Unit = {
        clipboardManager.setText(AnnotatedString(value))
        Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canCopy) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = copyText
                    )
                } else Modifier
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (canCopy) {
            IconButton(
                onClick = copyText,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatMalRating(rating: String?): String {
    return when (rating?.lowercase()) {
        "g" -> "G (All Ages)"
        "pg" -> "PG (Children)"
        "pg_13" -> "PG-13 (Teens 13 or older)"
        "r" -> "R (17+; violence & profanity)"
        "r+" -> "R+ (Mild Nudity)"
        "rx" -> "Rx (Hentai)"
        null -> "N/A"
        else -> rating.replace("_", " ").uppercase()
    }
}

