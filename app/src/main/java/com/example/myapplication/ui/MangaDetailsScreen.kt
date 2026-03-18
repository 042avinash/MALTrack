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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
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
import com.example.myapplication.data.model.MangaDetailsResponse
import com.example.myapplication.data.model.MyMangaListStatus
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
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                        titleLanguage = titleLanguage,
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
    titleLanguage: TitleLanguage,
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My List Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Row {
                                IconButton(onClick = { showEditDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                IconButton(onClick = { showDeleteConfirm = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Status: ${details.myListStatus.status?.replace("_", " ")?.uppercase() ?: "UNKNOWN"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Score: ${if (details.myListStatus.score > 0) details.myListStatus.score else "-"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Progress: ${details.myListStatus.numVolumesRead} vols, ${details.myListStatus.numChaptersRead} chapters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to List")
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
                            Card(
                                modifier = Modifier.width(120.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = rel.node.mainPicture?.medium,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        Text(
                                            text = rel.node.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = rel.relationTypeFormatted,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recommendations
        if (!details.recommendations.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recommendations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(details.recommendations) { rec ->
                            Card(
                                modifier = Modifier.width(120.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = rec.node.mainPicture?.medium,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = rec.node.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
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

        // Stats Donut Chart
        if (details.statistics?.status != null) {
            item {
                val stats = details.statistics.status
                val watching = stats.watching?.toIntOrNull() ?: 0
                val completed = stats.completed?.toIntOrNull() ?: 0
                val onHold = stats.onHold?.toIntOrNull() ?: 0
                val dropped = stats.dropped?.toIntOrNull() ?: 0
                val planToWatch = stats.planToWatch?.toIntOrNull() ?: 0
                
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
