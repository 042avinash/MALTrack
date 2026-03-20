package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.myapplication.data.model.JikanFavoriteItem
import com.example.myapplication.data.model.JikanFullUserProfile
import com.example.myapplication.data.model.UserProfile
import com.example.myapplication.data.remote.JikanFriend
import com.example.myapplication.data.model.AnimeStatistics
import com.example.myapplication.data.model.MangaStatistics
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    username: String? = null,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onListClick: (String) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(username) {
        viewModel.getProfile(username)
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage()
    }

    if (showSearchDialog) {
        SearchUserDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { 
                onUserClick(it)
                showSearchDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (username == null || username == "null") "Profile" else username,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = if (username == null || username == "null") ({}) else ({
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }),
                actions = {
                    val currentProfileUsername = (uiState as? ProfileUiState.Success)?.jikanUser?.username
                    if (currentProfileUsername != null) {
                        TextButton(onClick = { onListClick(currentProfileUsername) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "View List",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View List")
                        }
                    }

                    if (username == null || username == "null") {
                        TextButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSearchDialog = true }) {
                Icon(Icons.Default.Search, contentDescription = "Search User")
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState is ProfileUiState.Loading,
            onRefresh = { viewModel.getProfile(username, forceRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        ProfileLoadingShimmer(modifier = Modifier.fillMaxSize())
                    }
                    is ProfileUiState.Success -> {
                        ProfileContent(
                            malUser = state.malUser,
                            jikanUser = state.jikanUser,
                            friends = state.friends,
                            animeFavoriteMeta = state.animeFavoriteMeta,
                            mangaFavoriteMeta = state.mangaFavoriteMeta,
                            onAnimeClick = onAnimeClick,
                            onMangaClick = onMangaClick,
                            onUserClick = onUserClick
                        )
                    }
                    is ProfileUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getProfile(username) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileLoadingShimmer(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberProfileShimmerBrush()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerBrush)
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )
            }
        }
        items(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(146.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(82.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
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

@Composable
private fun rememberProfileShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "profile_shimmer")
    val offset by transition.animateFloat(
        initialValue = -350f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "profile_shimmer_offset"
    )

    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    return Brush.horizontalGradient(
        colors = listOf(base, highlight, base),
        startX = offset - 220f,
        endX = offset
    )
}

@Composable
fun ProfileContent(
    malUser: UserProfile?, 
    jikanUser: JikanFullUserProfile, 
    friends: List<JikanFriend>,
    animeFavoriteMeta: Map<Int, FavoriteMediaMeta>,
    mangaFavoriteMeta: Map<Int, FavoriteMediaMeta>,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onUserClick: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anime", "Manga")
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFriendsDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(about = jikanUser.about ?: "No about information provided.", onDismiss = { showAboutDialog = false })
    }

    if (showFriendsDialog) {
        FriendsDialog(
            friends = friends,
            onDismiss = { showFriendsDialog = false },
            onUserClick = {
                showFriendsDialog = false
                onUserClick(it)
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = malUser?.picture ?: jikanUser.images?.jpg?.image_url,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = "About Me", 
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(text = jikanUser.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                val infoParts = mutableListOf<String>()
                jikanUser.gender?.let { infoParts.add(it) }
                jikanUser.location?.let { infoParts.add(it) }
                if (infoParts.isNotEmpty()) {
                    Text(
                        text = infoParts.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!jikanUser.external.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(jikanUser.external) { link ->
                            AssistChip(
                                onClick = { 
                                    link.url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                                },
                                label = { Text(link.name ?: "Link") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { 
                        val url = jikanUser.url ?: "https://myanimelist.net/profile/${jikanUser.username}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("View Profile on MAL")
                }
            }
        }

        // Friends Section
        if (friends.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Friends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (friends.size > 8) {
                            AssistChip(
                                onClick = { showFriendsDialog = true },
                                label = { Text("More") },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(friends.take(8)) { friend ->
                            Column(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clickable { onUserClick(friend.user.username) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = friend.user.images.jpg?.image_url,
                                    contentDescription = friend.user.username,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = friend.user.username,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTabIndex == index
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape,
                        label = {
                            Text(
                                text = title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        if (selectedTabIndex == 0) {
            animeProfileItems(
                user = jikanUser,
                malStats = malUser?.animeStatistics,
                animeFavoriteMeta = animeFavoriteMeta,
                onAnimeClick = onAnimeClick
            )
        } else {
            mangaProfileItems(
                user = jikanUser,
                malStats = malUser?.mangaStatistics,
                mangaFavoriteMeta = mangaFavoriteMeta,
                onMangaClick = onMangaClick
            )
        }

        jikanUser.favorites?.let { favs ->
            favs.characters?.takeIf { it.isNotEmpty() }?.let {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FavoriteCharactersSection(items = it)
                    }
                }
            }
            favs.people?.takeIf { it.isNotEmpty() }?.let {
                item {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 16.dp)) {
                        FavoritePeopleSection(items = it)
                    }
                }
            }
            favs.studios?.takeIf { it.isNotEmpty() }?.let {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FavoritesSection("Favorite Studios", it)
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsDialog(
    friends: List<JikanFriend>,
    onDismiss: () -> Unit,
    onUserClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Friends",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(friends) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onUserClick(friend.user.username) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = friend.user.images.jpg?.image_url,
                                contentDescription = friend.user.username,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = friend.user.username,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                friend.last_online?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = formatLastOnline(it),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatLastOnline(raw: String): String {
    val zonedDateTime = runCatching {
        OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault())
    }.getOrElse { return raw }

    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
    return zonedDateTime.format(formatter)
}

fun androidx.compose.foundation.lazy.LazyListScope.animeProfileItems(
    user: JikanFullUserProfile,
    malStats: AnimeStatistics?,
    animeFavoriteMeta: Map<Int, FavoriteMediaMeta>,
    onAnimeClick: (Int) -> Unit
) {
    val stats = user.statistics?.anime ?: return
    
    item {
        Column(modifier = Modifier.padding(16.dp)) {
            val items = listOf(
                StatPiece(malStats?.numWatching ?: stats.watching, Color(0xFF2196F3), "Watching"),
                StatPiece(malStats?.numCompleted ?: stats.completed, Color(0xFF4CAF50), "Completed"),
                StatPiece(malStats?.numOnHold ?: stats.on_hold, Color(0xFFFFC107), "On Hold"),
                StatPiece(malStats?.numDropped ?: stats.dropped, Color(0xFFF44336), "Dropped"),
                StatPiece(malStats?.numPlanToWatch ?: stats.plan_to_watch, Color(0xFF9E9E9E), "Planned")
            )
            StatsDonutChart(title = "Anime Stats", total = malStats?.numItems ?: stats.total_entries, pieces = items)
        }
    }

    item {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Days Watched", "%.1f".format(malStats?.numDaysWatched ?: stats.days_watched))
                DetailRow("Mean Score", (malStats?.meanScore ?: stats.mean_score).toString())
                DetailRow("Episodes", "%,d".format(malStats?.numEpisodes ?: stats.episodes_watched))
                DetailRow("Rewatched", "%,d".format(malStats?.numTimesRewatched ?: stats.rewatched))
                DetailRow("Total Entries", "%,d".format(malStats?.numItems ?: stats.total_entries))
            }
        }
    }

    user.favorites?.let { favs ->
        favs.anime?.takeIf { it.isNotEmpty() }?.let {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    FavoriteMediaSection(
                        title = "Favorite Anime",
                        items = it,
                        metadata = animeFavoriteMeta,
                        onItemClick = onAnimeClick
                    )
                }
            }
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.mangaProfileItems(
    user: JikanFullUserProfile,
    malStats: MangaStatistics?,
    mangaFavoriteMeta: Map<Int, FavoriteMediaMeta>,
    onMangaClick: (Int) -> Unit
) {
    val stats = user.statistics?.manga ?: return

    item {
        Column(modifier = Modifier.padding(16.dp)) {
            val items = listOf(
                StatPiece(malStats?.numReading ?: stats.reading, Color(0xFF2196F3), "Reading"),
                StatPiece(malStats?.numCompleted ?: stats.completed, Color(0xFF4CAF50), "Completed"),
                StatPiece(malStats?.numOnHold ?: stats.on_hold, Color(0xFFFFC107), "On Hold"),
                StatPiece(malStats?.numDropped ?: stats.dropped, Color(0xFFF44336), "Dropped"),
                StatPiece(malStats?.numPlanToRead ?: stats.plan_to_read, Color(0xFF9E9E9E), "Planned")
            )
            StatsDonutChart(title = "Manga Stats", total = malStats?.numItems ?: stats.total_entries, pieces = items)
        }
    }

    item {
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Days Read", "%.1f".format(malStats?.numDaysRead ?: stats.days_read))
                DetailRow("Mean Score", (malStats?.meanScore ?: stats.mean_score).toString())
                DetailRow("Chapters", "%,d".format(malStats?.numChapters ?: stats.chapters_read))
                DetailRow("Volumes", "%,d".format(malStats?.numVolumes ?: stats.volumes_read))
                DetailRow("Reread", "%,d".format(malStats?.numTimesReread ?: stats.reread))
                DetailRow("Total Entries", "%,d".format(malStats?.numItems ?: stats.total_entries))
            }
        }
    }

    user.favorites?.manga?.takeIf { it.isNotEmpty() }?.let {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                FavoriteMediaSection(
                    title = "Favorite Manga",
                    items = it,
                    metadata = mangaFavoriteMeta,
                    onItemClick = onMangaClick
                )
            }
        }
    }
}

@Composable
fun SearchUserDialog(onDismiss: () -> Unit, onSearch: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search User") },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (username.isNotBlank()) onSearch(username) }) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

data class StatPiece(val value: Int, val color: Color, val label: String)

@Composable
fun StatsDonutChart(title: String, total: Int, pieces: List<StatPiece>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var currentStartAngle = -90f
                    val strokeWidth = 20.dp.toPx()
                    
                    pieces.forEach { piece ->
                        if (piece.value > 0) {
                            val sweepAngle = (piece.value.toFloat() / total) * 360f
                            drawArc(
                                color = piece.color,
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("%,d".format(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                pieces.forEach { piece ->
                    LegendItem(piece.color, piece.label, piece.value)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
        Text(text = "%,d".format(count), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FavoritesSection(title: String, items: List<JikanFavoriteItem>) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .clickable { item.url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = item.images?.jpg?.image_url,
                        contentDescription = item.title ?: item.name,
                        modifier = Modifier
                            .size(100.dp, 140.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title ?: item.name ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteCharactersSection(items: List<JikanFavoriteItem>) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Favorite Characters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .width(76.dp)
                        .clickable { item.url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = item.images?.jpg?.image_url,
                        contentDescription = item.title ?: item.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title ?: item.name ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritePeopleSection(items: List<JikanFavoriteItem>) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Favorite People", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .width(76.dp)
                        .clickable { item.url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = item.images?.jpg?.image_url,
                        contentDescription = item.title ?: item.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title ?: item.name ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteMediaSection(
    title: String,
    items: List<JikanFavoriteItem>,
    metadata: Map<Int, FavoriteMediaMeta>,
    onItemClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                val meta = metadata[item.mal_id]
                Card(
                    modifier = Modifier
                        .width(124.dp)
                        .aspectRatio(0.7f)
                        .clickable { onItemClick(item.mal_id) },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.images?.jpg?.image_url,
                            contentDescription = item.title ?: item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
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
                                text = item.title ?: item.name ?: "Unknown",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Members: ${formatMembersCount(meta?.numListUsers)}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp
                            )
                            val malScoreText = meta?.mean?.let { String.format("%.2f", it) } ?: "N/A"
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
        }
    }
}

private fun formatMembersCount(count: Int?): String {
    val value = count ?: return "N/A"
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000f)
        value >= 1_000 -> String.format("%.1fK", value / 1_000f)
        else -> value.toString()
    }
}

@Composable
fun AboutDialog(about: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("About Me", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(text = parseBBCode(about), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}

fun parseBBCode(text: String): AnnotatedString {
    return buildAnnotatedString {
        val pattern = Pattern.compile("\\[(/?[a-zA-Z*]+(?:=[^]]+)?)]")
        val matcher = pattern.matcher(text)
        var lastEnd = 0
        val styleStack = mutableListOf<String>()

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val fullTag = matcher.group(1) ?: ""
            
            append(text.substring(lastEnd, start))

            if (fullTag.startsWith("/")) {
                val tagName = fullTag.substring(1).lowercase()
                val index = styleStack.indexOfLast { it == tagName }
                if (index != -1) {
                    while (styleStack.size > index) {
                        pop()
                        styleStack.removeAt(styleStack.size - 1)
                    }
                }
            } else {
                val parts = fullTag.split("=", limit = 2)
                val tagName = parts[0].lowercase()
                val tagValue = if (parts.size > 1) parts[1] else null

                when (tagName) {
                    "b" -> {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        styleStack.add("b")
                    }
                    "i" -> {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        styleStack.add("i")
                    }
                    "u" -> {
                        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        styleStack.add("u")
                    }
                    "s" -> {
                        pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                        styleStack.add("s")
                    }
                    "color" -> {
                        val color = try { 
                            if (tagValue?.startsWith("#") == true) {
                                val hex = if (tagValue.length == 7) "FF" + tagValue.substring(1) else tagValue.substring(1)
                                Color(hex.toLong(16))
                            } else Color.Unspecified
                        } catch (e: Exception) { Color.Unspecified }
                        pushStyle(SpanStyle(color = color))
                        styleStack.add("color")
                    }
                    "size" -> {
                        val size = tagValue?.toIntOrNull()?.let { (it * 0.8).sp } ?: 14.sp
                        pushStyle(SpanStyle(fontSize = size))
                        styleStack.add("size")
                    }
                    "url" -> {
                        pushStyle(SpanStyle(color = Color(0xFF2196F3), textDecoration = TextDecoration.Underline))
                        styleStack.add("url")
                    }
                    "*" -> {
                        append("• ")
                    }
                }
            }
            lastEnd = end
        }
        append(text.substring(lastEnd))
    }
}
