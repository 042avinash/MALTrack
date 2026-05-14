package com.example.myapplication.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.getPreferredTitle
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.model.MangaNode
import com.example.myapplication.data.model.MangaRecommendation
import com.example.myapplication.data.model.MyMangaListStatus
import com.example.myapplication.data.remote.JikanCharacterData
import com.example.myapplication.data.remote.JikanAnimeScoreBucket
import com.example.myapplication.data.remote.JikanReviewData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun MangaDetailsResponse.getPreferredTitle(language: TitleLanguage): String {
    return when (language) {
        TitleLanguage.ENGLISH -> alternativeTitles?.en.takeIf { !it.isNullOrBlank() } ?: title
        TitleLanguage.ROMAJI -> title
        TitleLanguage.JAPANESE -> alternativeTitles?.ja.takeIf { !it.isNullOrBlank() } ?: title
    }
}

fun downloadMangaImage(context: Context, url: String, title: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("$title Image")
            .setDescription("Downloading picture")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Manga_${System.currentTimeMillis()}.jpg")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to download", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailsScreen(
    viewModel: MangaDetailsViewModel,
    titleLanguage: TitleLanguage,
    onBackClick: () -> Unit,
    onMangaClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isPullRefreshing by remember { mutableStateOf(false) }

    val topBarTitle = if (uiState is MangaDetailsUiState.Success) {
        (uiState as MangaDetailsUiState.Success).details.getPreferredTitle(titleLanguage)
    } else "Details"

    LaunchedEffect(uiState, isPullRefreshing) {
        if (!isPullRefreshing) return@LaunchedEffect
        when (uiState) {
            is MangaDetailsUiState.Success,
            is MangaDetailsUiState.Error -> isPullRefreshing = false
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
                    if (uiState is MangaDetailsUiState.Success) {
                        FilledTonalButton(
                            onClick = {
                                val mangaId = (uiState as MangaDetailsUiState.Success).details.id
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://myanimelist.net/manga/$mangaId")
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
                    is MangaDetailsUiState.Loading -> {
                        if (!isPullRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is MangaDetailsUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is MangaDetailsUiState.Success -> {
                        MangaDetailsContent(
                            details = state.details,
                            cardMeta = state.cardMeta,
                            recommendations = state.recommendations,
                            reviews = state.reviews,
                            characters = state.characters,
                            allReviewsCount = state.allReviewsCount,
                            isRecommendationsLoaded = state.isRecommendationsLoaded,
                            isRecommendationsLoading = state.isRecommendationsLoading,
                            isReviewsLoaded = state.isReviewsLoaded,
                            isReviewsLoading = state.isReviewsLoading,
                            isCharactersLoaded = state.isCharactersLoaded,
                            isCharactersLoading = state.isCharactersLoading,
                            isCommunityStatsRefreshing = state.isCommunityStatsRefreshing,
                            scoreDistribution = state.scoreDistribution,
                            isScoreDistributionLoading = state.isScoreDistributionLoading,
                            recommendationsError = state.recommendationsError,
                            reviewsError = state.reviewsError,
                            charactersError = state.charactersError,
                            communityStatsError = state.communityStatsError,
                            scoreDistributionError = state.scoreDistributionError,
                            titleLanguage = titleLanguage,
                            onLoadRecommendations = { viewModel.loadRecommendations(forceRefresh = false) },
                            onLoadReviews = { viewModel.loadReviews(forceRefresh = false) },
                            onRefreshRecommendations = { viewModel.refreshRecommendations() },
                            onRefreshReviews = { viewModel.refreshReviews() },
                            onLoadCharacters = { viewModel.loadCharacters(forceRefresh = false) },
                            onRefreshCharacters = { viewModel.refreshCharactersSection() },
                            onRefreshCommunityStats = { viewModel.refreshCommunityStats() },
                            onMangaClick = onMangaClick,
                            onUpdateStatus = { status, isRereading, score, vols, chaps, priority, timesReread, rereadVal, tags, comments, start, finish ->
                                viewModel.updateListStatus(status, isRereading, score, vols, chaps, priority, timesReread, rereadVal, tags, comments, start, finish)
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
fun MangaDetailsContent(
    details: MangaDetailsResponse, 
    cardMeta: Map<Int, MangaCardMeta> = emptyMap(),
    recommendations: List<MangaRecommendation>,
    reviews: List<JikanReviewData>,
    characters: List<JikanCharacterData>,
    allReviewsCount: Int,
    isRecommendationsLoaded: Boolean,
    isRecommendationsLoading: Boolean,
    isReviewsLoaded: Boolean,
    isReviewsLoading: Boolean,
    isCharactersLoaded: Boolean,
    isCharactersLoading: Boolean,
    isCommunityStatsRefreshing: Boolean,
    scoreDistribution: List<JikanAnimeScoreBucket>,
    isScoreDistributionLoading: Boolean,
    recommendationsError: String?,
    reviewsError: String?,
    charactersError: String?,
    communityStatsError: String?,
    scoreDistributionError: String?,
    titleLanguage: TitleLanguage,
    onLoadRecommendations: () -> Unit,
    onLoadReviews: () -> Unit,
    onRefreshRecommendations: () -> Unit,
    onRefreshReviews: () -> Unit,
    onLoadCharacters: () -> Unit,
    onRefreshCharacters: () -> Unit,
    onRefreshCommunityStats: () -> Unit,
    onMangaClick: (Int) -> Unit,
    onUpdateStatus: (String?, Boolean?, Int?, Int?, Int?, Int?, Int?, Int?, String?, String?, String?, String?) -> Unit,
    onDeleteStatus: () -> Unit
) {
    val context = LocalContext.current
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var selectedPicture by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRelatedPopup by remember { mutableStateOf(false) }
    var showRecommendationsPopup by remember { mutableStateOf(false) }
    var showAllCharactersPopup by remember { mutableStateOf(false) }
    var communityStatsInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(isCharactersLoaded, isCharactersLoading) {
        if (!isCharactersLoaded && !isCharactersLoading) {
            onLoadCharacters()
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete from list") },
            text = { Text("Are you sure you want to delete this manga from your list?") },
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

    if (showRelatedPopup) {
        MangaDetailsGridPopup(
            title = "Related Manga",
            nodes = details.relatedManga.orEmpty().map { it.node },
            cardMeta = cardMeta,
            titleLanguage = titleLanguage,
            onMangaClick = onMangaClick,
            onDismiss = { showRelatedPopup = false }
        )
    }

    if (showRecommendationsPopup) {
        MangaDetailsGridPopup(
            title = "Recommendations",
            nodes = recommendations.map { it.node },
            cardMeta = cardMeta,
            titleLanguage = titleLanguage,
            isLoading = isRecommendationsLoading,
            onMangaClick = onMangaClick,
            onDismiss = { showRecommendationsPopup = false }
        )
    }

    if (showAllCharactersPopup) {
        Dialog(
            onDismissRequest = { showAllCharactersPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("All Characters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAllCharactersPopup = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        gridItems(characters) { entry ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(entry.character.url)))
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = entry.character.images.jpg?.image_url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.character.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = entry.role.ifBlank { "Unknown" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
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
                        if (details.numVolumes != null && details.numVolumes > 0) {
                            Text(
                                text = "${details.numVolumes} Volumes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                        if (details.numChapters != null && details.numChapters > 0) {
                            Text(
                                text = "${details.numChapters} Chapters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons / My List Status
        item {
            if (details.myListStatus != null) {
                val statusLabel = details.myListStatus.status
                    ?.replace("_", " ")
                    ?.uppercase()
                    ?: "UNKNOWN"
                val progressLabel = "${details.myListStatus.numVolumesRead} vols, ${details.myListStatus.numChaptersRead} ch"
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                    Spacer(modifier = Modifier.height(16.dp))
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
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Characters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onRefreshCharacters, enabled = !isCharactersLoading) {
                        if (isCharactersLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh characters")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    isCharactersLoading && characters.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                    characters.isNotEmpty() -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(characters.take(5)) { entry ->
                                Column(
                                    modifier = Modifier
                                        .width(92.dp)
                                        .clickable {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(entry.character.url)))
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = entry.character.images.jpg?.image_url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = entry.character.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = entry.role.ifBlank { "Unknown" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (characters.size > 5) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .width(92.dp)
                                            .height(120.dp)
                                            .clickable { showAllCharactersPopup = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
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
                    else -> {
                        MangaSectionFallbackCard(
                            title = "Characters Unavailable",
                            message = charactersError ?: "Character entries are not available for this manga right now. Try reloading this section in a moment."
                        )
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
                
                InfoRow("English", details.alternativeTitles?.en ?: "Unknown", copyOnLongPress = true)
                InfoRow("Japanese", details.alternativeTitles?.ja ?: "Unknown", copyOnLongPress = true)
                InfoRow("Romaji", details.title.takeIf { it.isNotBlank() } ?: "Unknown", copyOnLongPress = true)
                InfoRow("Synonyms", details.alternativeTitles?.synonyms?.joinToString()?.takeIf { it.isNotEmpty() } ?: "Unknown")
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("Rank", "#${details.rank ?: "Unknown"}")
                InfoRow("Popularity", "#${details.popularity ?: "Unknown"}")
                InfoRow("Published", "${details.startDate ?: "Unknown"} to ${details.endDate ?: "Unknown"}")
                InfoRow("Authors", details.authors?.joinToString { "${it.node.firstName} ${it.node.lastName} (${it.role})" } ?: "Unknown")
                InfoRow("Serialization", details.serialization?.joinToString { it.node.name } ?: "Unknown")
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
                    enabled = !details.relatedManga.isNullOrEmpty()
                ) {
                    Text("Related Manga")
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
            if (!recommendationsError.isNullOrBlank()) {
                MangaSectionFallbackCard(
                    title = "Recommendations Unavailable",
                    message = recommendationsError
                )
            }
        }

        // Reviews
        item {
            FilledTonalButton(
                onClick = onLoadReviews,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isReviewsLoading && !isReviewsLoaded) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
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
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    review.user.username,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(review.date.take(10), style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val tag = review.tags.firstOrNull() ?: ""
                                if (tag.isNotEmpty()) {
                                    val tagColor = when {
                                        tag.contains("Not", ignoreCase = true) -> Color.Red
                                        tag.contains("Mixed", ignoreCase = true) -> Color.Gray
                                        else -> Color(0xFF4CAF50)
                                    }
                                    Text(
                                        text = tag,
                                        color = tagColor,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Score",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
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
        } else if (isReviewsLoading || isReviewsLoaded || !reviewsError.isNullOrBlank()) {
            item {
                val reviewMessage = when {
                    isReviewsLoading -> "Fetching reviews from Jikan..."
                    !reviewsError.isNullOrBlank() -> "Could not refresh reviews right now. Please try again."
                    allReviewsCount > 0 -> "Jikan reports %,d reviews, but the review preview did not return entries yet.".format(allReviewsCount)
                    else -> "No public reviews are available for this manga yet."
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

        // Stats Donut Chart
        if (details.statistics?.status != null || scoreDistribution.isNotEmpty() || (details.numScoringUsers ?: 0) > 0 || !communityStatsError.isNullOrBlank()) {
            item {
                val stats = details.statistics?.status
                val scoringUsers = details.numScoringUsers ?: 0
                val hasScoreDistribution = scoreDistribution.any { it.votes > 0 || it.percentage > 0.0 }
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Community Stats",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
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
                    Spacer(modifier = Modifier.height(16.dp))

                    if (stats == null) {
                        MangaSectionFallbackCard(
                            title = "Community Stats Unavailable",
                            message = communityStatsError ?: "Community status distribution is not available for this title yet."
                        )
                        return@Column
                    }

                    val watching = stats.watching ?: 0
                    val completed = stats.completed ?: 0
                    val onHold = stats.onHold ?: 0
                    val dropped = stats.dropped ?: 0
                    val planToWatch = stats.planToWatch ?: 0
                    val total = watching + completed + onHold + dropped + planToWatch
                    if (total > 0 || hasScoreDistribution || scoringUsers > 0) {
                        fun pct(value: Int): Float = if (total > 0) (value * 100f / total) else 0f
                    val completionRate = pct(completed)
                    val dropRate = pct(dropped)
                    val watchingRate = pct(watching)
                    val planRate = pct(planToWatch)
                    val onHoldRate = pct(onHold)
                    val members = details.statistics?.numListUsers ?: details.numListUsers ?: total

                    val readingRate = watchingRate
                    val showTrendingCard = readingRate >= 25f || (readingRate >= 20f && planRate >= 20f)
                    val showCompletionCard = completionRate >= 40f && dropRate <= 10f
                    val showDropCard = dropRate >= 16f && dropRate >= completionRate * 0.35f
                    val showOnHoldCard = onHoldRate >= 15f
                    val showPlannedCard = planRate >= 60f

                    val scoreBuckets = scoreDistribution.associateBy { it.score }
                    fun scoreShare(vararg scores: Int): Float {
                        return scores.sumOf { score -> scoreBuckets[score]?.percentage ?: 0.0 }.toFloat()
                    }
                    val highScoreShare = scoreShare(9, 10)
                    val score5to8Share = scoreShare(5, 6, 7, 8)
                    val score5to10Share = scoreShare(5, 6, 7, 8, 9, 10)
                    val score5to7Share = scoreShare(5, 6, 7)
                    val score5to6Share = scoreShare(5, 6)
                    val lowScoreShare = scoreShare(1, 2, 3, 4)
                    val largestBucketShare = scoreDistribution.maxOfOrNull { it.percentage }?.toFloat() ?: 0f

                    val showCommunityLovedCard = highScoreShare >= 40f && lowScoreShare <= 8f
                    val showHiddenGemCard = highScoreShare >= 38f && lowScoreShare <= 8f && members < 70_000
                    val showPolarizingCard = highScoreShare >= 25f && score5to8Share >= 45f && lowScoreShare >= 7f
                    val showColdReceptionCard = lowScoreShare >= 18f
                    val showMixedReceptionCard = largestBucketShare < 35f && score5to6Share >= 15f && lowScoreShare >= 7f
                    val showMostlyMidCard = score5to7Share >= 50f && highScoreShare < 30f && lowScoreShare < 15f
                    val showSafePickCard = lowScoreShare <= 5f && score5to10Share >= 92f
                    val showNicheAppealCard = highScoreShare >= 25f && members < 150_000 && lowScoreShare >= 4f
                    val showObscureCard = members < 12_000
                    val showSlowBurnCard = highScoreShare >= 20f && highScoreShare < 32f && dropRate <= 5f && lowScoreShare <= 5f && members > 15_000

                    val allMatchingCards = listOfNotNull(
                        if (showTrendingCard) MangaStatsPillData("Trending", Icons.Default.TrendingUp, Color(0xFF1E88E5), Color.White, "Trending", "This manga is seeing a lot of active attention right now, often because it is currently popular or gaining momentum within the community.") else null,
                        if (showCommunityLovedCard) MangaStatsPillData("Beloved", Icons.Default.Favorite, Color(0xFFE91E63), Color.White, "Beloved", "Readers who rate this manga tend to rate it very highly, giving it a reputation for strong emotional impact and broad fan appreciation.") else null,
                        if (showHiddenGemCard) MangaStatsPillData("HiddenGem", Icons.Default.AutoAwesome, Color(0xFF00796B), Color.White, "HiddenGem", "Despite its smaller audience, this manga receives unusually strong praise from the people who read it.") else null,
                        if (showPolarizingCard) MangaStatsPillData("Polarizing", Icons.Default.CompareArrows, Color(0xFF8E24AA), Color.White, "Polarizing", "Reactions to this manga vary heavily between readers, with strong praise and noticeable disagreement.") else null,
                        if (showDropCard) MangaStatsPillData("High Dropoff", Icons.Default.ExitToApp, Color(0xFFD32F2F), Color.White, "High Dropoff", "A noticeable number of users stop reading before completion.") else null,
                        if (showColdReceptionCard) MangaStatsPillData("Disliked", Icons.Default.ThumbDown, Color(0xFF546E7A), Color.White, "Disliked", "Lower ratings appear more frequently here than usual, suggesting weaker overall reception.") else null,
                        if (showCompletionCard) MangaStatsPillData("High Retention", Icons.Default.TaskAlt, Color(0xFF43A047), Color.White, "High Retention", "People who start this manga tend to stay with it through completion.") else null,
                        if (showMixedReceptionCard) MangaStatsPillData("Mixed", Icons.Default.Shuffle, Color(0xFF78909C), Color.White, "Mixed", "Reader opinions are spread across several score ranges rather than clustering around one clear consensus.") else null,
                        if (showOnHoldCard) MangaStatsPillData("Stalled", Icons.Default.PauseCircle, Color(0xFFFDD835), Color(0xFF212121), "Stalled", "Readers place this manga on hold more often than usual.") else null,
                        if (showMostlyMidCard) MangaStatsPillData("Mid", Icons.Default.Remove, Color(0xFF757575), Color.White, "Mid", "Most reactions land around the middle rather than extremes.") else null,
                        if (showSafePickCard) MangaStatsPillData("Broad Appeal", Icons.Default.Verified, Color(0xFF66BB6A), Color.White, "Broad Appeal", "Very few readers strongly dislike this manga, making it broadly approachable.") else null,
                        if (showPlannedCard) MangaStatsPillData("High Interest", Icons.Default.Bookmark, Color(0xFF607D8B), Color.White, "High Interest", "A large number of users have this manga saved for later, suggesting strong curiosity.") else null,
                        if (showNicheAppealCard) MangaStatsPillData("Niche", Icons.Default.Tune, Color(0xFF26A69A), Color.White, "Niche", "This manga resonates strongly with a more specific audience.") else null,
                        if (showObscureCard) MangaStatsPillData("Obscure", Icons.Default.VisibilityOff, Color(0xFF6D4C41), Color.White, "Obscure", "Relatively few users have discovered or added this manga.") else null,
                        if (showSlowBurnCard) MangaStatsPillData("Slowburn", Icons.Default.HourglassTop, Color(0xFF8D6E63), Color.White, "Slowburn", "This manga tends to build appreciation gradually over time.") else null
                    )

                    val suppressionMap = mapOf(
                        "Beloved" to setOf("Broad Appeal"),
                        "Polarizing" to setOf("Broad Appeal", "Mixed"),
                        "HiddenGem" to setOf("Obscure"),
                        "Disliked" to setOf("Mid")
                    )
                    val priorityMap = mapOf(
                        "Trending" to 1, "Beloved" to 2, "HiddenGem" to 3, "Polarizing" to 4, "High Dropoff" to 5,
                        "Disliked" to 6, "High Retention" to 7, "Mixed" to 8, "Stalled" to 9, "Mid" to 10,
                        "Broad Appeal" to 11, "High Interest" to 12, "Niche" to 13, "Obscure" to 14, "Slowburn" to 15
                    )
                    val activeTitles = allMatchingCards.map { it.label }.toSet()
                    val suppressedTitles = activeTitles.flatMap { title -> suppressionMap[title] ?: emptySet() }.toSet()
                    val statPills = allMatchingCards
                        .filterNot { card -> card.label in suppressedTitles }
                        .sortedBy { card -> priorityMap[card.label] ?: Int.MAX_VALUE }
                        .take(5)

                    
                        if (statPills.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(end = 16.dp)
                            ) {
                                items(statPills) { pill ->
                                    MangaStatsTrendPill(
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
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var currentStartAngle = -90f
                                    val strokeWidth = 24.dp.toPx()
                                    
                                    val pieces = listOf(
                                        Pair(completed, Color(0xFF4CAF50)),
                                        Pair(watching, Color(0xFF2196F3)),
                                        Pair(planToWatch, Color(0xFF9E9E9E)),
                                        Pair(onHold, Color(0xFFFFC107)),
                                        Pair(dropped, Color(0xFFF44336))
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
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem(Color(0xFF4CAF50), "Completed", completed, pct(completed))
                                LegendItem(Color(0xFF2196F3), "Reading", watching, pct(watching))
                                LegendItem(Color(0xFF9E9E9E), "Plan to Read", planToWatch, pct(planToWatch))
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
                            if (isScoreDistributionLoading) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Refreshing score distribution...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                isScoreDistributionLoading -> "Refreshing score distribution..."
                                !scoreDistributionError.isNullOrBlank() && scoringUsers >= 25 ->
                                    "MAL reports %,d scoring users, but the score breakdown source is temporarily unavailable. Tap refresh to retry.".format(scoringUsers)
                                !scoreDistributionError.isNullOrBlank() && scoringUsers > 0 ->
                                    "Only %,d MAL scoring users are available right now. More scorers are needed for a reliable score breakdown.".format(scoringUsers)
                                !scoreDistributionError.isNullOrBlank() && details.mean != null ->
                                    "MAL has an average score, but score breakdown data is temporarily unavailable."
                                !scoreDistributionError.isNullOrBlank() ->
                                    "Score breakdown data is temporarily unavailable. Tap refresh to retry."
                                scoringUsers >= 25 -> "MAL reports %,d scoring users, but the score buckets source is temporarily unavailable. Tap refresh to retry.".format(scoringUsers)
                                scoringUsers > 0 -> "Only %,d MAL scoring users are available right now. More scorers are needed for a reliable score breakdown.".format(scoringUsers)
                                details.mean != null -> "MAL has an average score, but no score bucket breakdown is available yet."
                                else -> "No public score distribution is available for this manga yet."
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
                            downloadMangaImage(context, selectedPicture!!, details.getPreferredTitle(titleLanguage))
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

    if (showEditDialog) {
        EditMangaListStatusDialog(
            currentStatus = details.myListStatus ?: MyMangaListStatus(),
            maxVolumes = details.numVolumes ?: 0,
            maxChapters = details.numChapters ?: 0,
            onDismiss = { showEditDialog = false },
            onSave = { status, isRereading, score, vols, chaps, priority, timesReread, rereadVal, tags, comments, start, finish ->
                onUpdateStatus(status, isRereading, score, vols, chaps, priority, timesReread, rereadVal, tags, comments, start, finish)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun MangaDetailsGridPopup(
    title: String,
    nodes: List<MangaNode>,
    cardMeta: Map<Int, MangaCardMeta>,
    titleLanguage: TitleLanguage,
    isLoading: Boolean = false,
    onMangaClick: (Int) -> Unit,
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
                    isLoading && nodes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    nodes.isEmpty() -> {
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
                                items = nodes,
                                key = { it.id }
                            ) { node ->
                                MangaGridCard(
                                    manga = node,
                                    meta = cardMeta[node.id],
                                    titleLanguage = titleLanguage,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onDismiss()
                                        onMangaClick(node.id)
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

@Composable
private fun MangaGridCard(
    manga: MangaNode,
    meta: MangaCardMeta? = null,
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
        val resolvedListStatus = manga.myListStatus ?: meta?.myListStatus
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = manga.mainPicture?.medium,
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
                        Icon(
                            imageVector = getMangaStatusIcon(status.status),
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
                    text = manga.getPreferredTitle(titleLanguage),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                val resolvedMean = meta?.mean ?: manga.meanScore
                val resolvedMembers = meta?.members ?: manga.numListUsers
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

private fun getMangaStatusIcon(status: String?): ImageVector {
    return when (status) {
        "reading" -> Icons.Default.Visibility
        "completed" -> Icons.Default.CheckCircle
        "on_hold" -> Icons.Default.PauseCircle
        "plan_to_read" -> Icons.Default.Schedule
        "dropped" -> Icons.Default.Cancel
        else -> Icons.Default.Visibility
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
fun EditMangaListStatusDialog(
    currentStatus: MyMangaListStatus,
    maxVolumes: Int,
    maxChapters: Int,
    onDismiss: () -> Unit,
    onSave: (String?, Boolean?, Int?, Int?, Int?, Int?, Int?, Int?, String?, String?, String?, String?) -> Unit
) {
    var status by remember { mutableStateOf(currentStatus.status ?: "plan_to_read") }
    var score by remember { mutableIntStateOf(currentStatus.score) }
    var volumes by remember { mutableIntStateOf(currentStatus.numVolumesRead) }
    var chapters by remember { mutableIntStateOf(currentStatus.numChaptersRead) }
    var startDate by remember { mutableStateOf(currentStatus.startDate ?: "") }
    var endDate by remember { mutableStateOf(currentStatus.finishDate ?: "") }
    var isRereading by remember { mutableStateOf(currentStatus.isRereading) }
    var priority by remember { mutableIntStateOf(currentStatus.priority) }
    var timesReread by remember { mutableIntStateOf(currentStatus.numTimesReread) }
    var rereadValue by remember { mutableIntStateOf(currentStatus.rereadValue) }
    var tags by remember { mutableStateOf(currentStatus.tags.joinToString(", ")) }
    var notes by remember { mutableStateOf(currentStatus.comments ?: "") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try { dateFormat.parse(startDate)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = dateFormat.format(Date(it)) }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try { dateFormat.parse(endDate)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = dateFormat.format(Date(it)) }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit List Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Status", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatusIcon(Icons.Default.Visibility, "Reading", status == "reading") {
                            status = "reading"
                            if (startDate.isEmpty()) startDate = dateFormat.format(Calendar.getInstance().time)
                        }
                        StatusIcon(Icons.Default.CheckCircle, "Completed", status == "completed") {
                            status = "completed"
                            if (maxVolumes > 0) volumes = maxVolumes
                            if (maxChapters > 0) chapters = maxChapters
                            if (endDate.isEmpty()) endDate = dateFormat.format(Calendar.getInstance().time)
                        }
                        StatusIcon(Icons.Default.PauseCircle, "On-Hold", status == "on_hold") {
                            status = "on_hold"
                        }
                        StatusIcon(Icons.Default.Schedule, "Planned", status == "plan_to_read") { status = "plan_to_read" }
                        StatusIcon(Icons.Default.Cancel, "Dropped", status == "dropped") { status = "dropped" }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CounterField(label = "Volumes Read", value = volumes, onValueChange = { volumes = it.coerceAtLeast(0) }, max = if (maxVolumes > 0) maxVolumes else null)
                    CounterField(label = "Chapters Read", value = chapters, onValueChange = { chapters = it.coerceAtLeast(0) }, max = if (maxChapters > 0) maxChapters else null)
                    CounterField(label = "Score", value = score, onValueChange = { score = it.coerceIn(0, 10) }, max = 10)

                    Text("Start Date", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, modifier = Modifier.weight(1f), readOnly = true, 
                            trailingIcon = { IconButton(onClick = { showStartDatePicker = true }) { Icon(Icons.Default.DateRange, null) } })
                        TextButton(onClick = { startDate = dateFormat.format(Date()) }) { Text("Today") }
                        IconButton(onClick = { startDate = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear start date") }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Finish Date", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, modifier = Modifier.weight(1f), readOnly = true, 
                            trailingIcon = { IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Default.DateRange, null) } })
                        TextButton(onClick = { endDate = dateFormat.format(Date()) }) { Text("Today") }
                        IconButton(onClick = { endDate = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear finish date") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Priority", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PriorityChip("Low", priority == 0) { priority = 0 }
                        PriorityChip("Medium", priority == 1) { priority = 1 }
                        PriorityChip("High", priority == 2) { priority = 2 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRereading, onCheckedChange = { isRereading = it })
                        Text("Rereading")
                    }
                    CounterField(label = "Total Rereads", value = timesReread, onValueChange = { timesReread = it.coerceAtLeast(0) })
                    CounterField(label = "Reread Value", value = rereadValue, onValueChange = { rereadValue = it.coerceIn(0, 5) }, max = 5)

                    OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }

                Button(onClick = { onSave(status, isRereading, score, volumes, chapters, priority, timesReread, rereadValue, tags, notes, startDate, endDate) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
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

private data class MangaStatsPillData(
    val label: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val dialogTitle: String,
    val infoText: String
)

@Composable
private fun MangaStatsTrendPill(
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
private fun MangaSectionFallbackCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, count: Int, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(
            text = "%,d (%.1f%%)".format(count, percentage),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
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
    val canCopy = copyOnLongPress && value.isNotBlank() && value != "N/A" && value != "Unknown"
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
