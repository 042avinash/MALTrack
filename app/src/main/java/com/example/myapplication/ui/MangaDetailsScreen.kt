package com.example.myapplication.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
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

    val topBarTitle = if (uiState is MangaDetailsUiState.Success) {
        (uiState as MangaDetailsUiState.Success).details.getPreferredTitle(titleLanguage)
    } else "Details"

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MangaDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        allReviewsCount = state.allReviewsCount,
                        isRecommendationsLoaded = state.isRecommendationsLoaded,
                        isRecommendationsLoading = state.isRecommendationsLoading,
                        isReviewsLoaded = state.isReviewsLoaded,
                        isReviewsLoading = state.isReviewsLoading,
                        titleLanguage = titleLanguage,
                        onLoadRecommendations = { viewModel.loadRecommendations() },
                        onLoadReviews = { viewModel.loadReviews() },
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

@Composable
fun MangaDetailsContent(
    details: MangaDetailsResponse, 
    cardMeta: Map<Int, MangaCardMeta> = emptyMap(),
    recommendations: List<MangaRecommendation>,
    reviews: List<JikanReviewData>,
    allReviewsCount: Int,
    isRecommendationsLoaded: Boolean,
    isRecommendationsLoading: Boolean,
    isReviewsLoaded: Boolean,
    isReviewsLoading: Boolean,
    titleLanguage: TitleLanguage,
    onLoadRecommendations: () -> Unit,
    onLoadReviews: () -> Unit,
    onMangaClick: (Int) -> Unit,
    onUpdateStatus: (String?, Boolean?, Int?, Int?, Int?, Int?, Int?, Int?, String?, String?, String?, String?) -> Unit,
    onDeleteStatus: () -> Unit
) {
    val context = LocalContext.current
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var selectedPicture by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

        // Information Details
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow("English", details.alternativeTitles?.en ?: "N/A")
                InfoRow("Japanese", details.alternativeTitles?.ja ?: "N/A")
                InfoRow("Synonyms", details.alternativeTitles?.synonyms?.joinToString()?.takeIf { it.isNotEmpty() } ?: "N/A")
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("Rank", "#${details.rank ?: "N/A"}")
                InfoRow("Popularity", "#${details.popularity ?: "N/A"}")
                InfoRow("Aired", "${details.startDate ?: "?"} to ${details.endDate ?: "?"}")
                InfoRow("Authors", details.authors?.joinToString { "${it.node.firstName} ${it.node.lastName} (${it.role})" } ?: "N/A")
                InfoRow("Serialization", details.serialization?.joinToString { it.node.name } ?: "N/A")
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

        // Related Manga
        if (!details.relatedManga.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Related Manga",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(details.relatedManga) { rel ->
                            Column(
                                modifier = Modifier.width(124.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MangaGridCard(
                                    manga = rel.node,
                                    meta = cardMeta[rel.node.id],
                                    titleLanguage = titleLanguage,
                                    onClick = { onMangaClick(rel.node.id) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    tonalElevation = 1.dp
                                ) {
                                    Text(
                                        text = rel.relationTypeFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recommendations
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recommendations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isRecommendationsLoaded) {
                        FilledTonalButton(
                            onClick = onLoadRecommendations,
                            enabled = !isRecommendationsLoading
                        ) {
                            if (isRecommendationsLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isRecommendationsLoading) "Loading..." else "Load Recommendations",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (isRecommendationsLoaded && recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recommendations) { rec ->
                            MangaGridCard(
                                manga = rec.node,
                                meta = cardMeta[rec.node.id],
                                titleLanguage = titleLanguage,
                                onClick = { onMangaClick(rec.node.id) }
                            )
                        }
                    }
                }
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

        // Reviews
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Reviews",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!isReviewsLoaded) {
                    FilledTonalButton(
                        onClick = onLoadReviews,
                        enabled = !isReviewsLoading
                    ) {
                        if (isReviewsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isReviewsLoading) "Loading..." else "Load Reviews",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (allReviewsCount > 0) {
                    Text(
                        text = "$allReviewsCount total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        }

        // Stats Donut Chart
        if (details.statistics?.status != null) {
            item {
                val stats = details.statistics.status
                val watching = stats.watching ?: 0
                val completed = stats.completed ?: 0
                val onHold = stats.onHold ?: 0
                val dropped = stats.dropped ?: 0
                val planToWatch = stats.planToWatch ?: 0
                
                val total = watching + completed + onHold + dropped + planToWatch
                if (total > 0) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Community Stats",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                                LegendItem(Color(0xFF4CAF50), "Completed", completed)
                                LegendItem(Color(0xFF2196F3), "Reading", watching)
                                LegendItem(Color(0xFF9E9E9E), "Plan to Read", planToWatch)
                                LegendItem(Color(0xFFFFC107), "On Hold", onHold)
                                LegendItem(Color(0xFFF44336), "Dropped", dropped)
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
private fun MangaGridCard(
    manga: MangaNode,
    meta: MangaCardMeta? = null,
    titleLanguage: TitleLanguage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(124.dp)
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Finish Date", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, modifier = Modifier.weight(1f), readOnly = true, 
                            trailingIcon = { IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Default.DateRange, null) } })
                        TextButton(onClick = { endDate = dateFormat.format(Date()) }) { Text("Today") }
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
                    newValue.toIntOrNull()?.let { onValueChange(it) }
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
private fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(text = "%,d".format(count), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
