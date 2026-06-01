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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
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
import androidx.compose.ui.unit.Dp
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
                            friendsLoading = state.friendsLoading,
                            friendsError = state.friendsError,
                            favoritesMetaLoading = state.favoriteMetaLoading,
                            favoritesMetaError = state.favoriteMetaError,
                            animeFavoriteMeta = state.animeFavoriteMeta,
                            mangaFavoriteMeta = state.mangaFavoriteMeta,
                            onLoadFriends = { viewModel.loadFriends(username) },
                            onRefreshFriends = { viewModel.loadFriends(username, forceRefresh = true) },
                            onLoadFavorites = { viewModel.loadFavoriteMeta(username) },
                            onRefreshFavorites = { viewModel.loadFavoriteMeta(username, forceRefresh = true) },
                            onAnimeClick = onAnimeClick,
                            onMangaClick = onMangaClick,
                            onUserClick = onUserClick,
                            isOwnProfile = state.isOwnProfile,
                            viewerIsFriendWithProfileOwner = state.viewerIsFriendWithProfileOwner,
                            onFindUserClick = { showSearchDialog = true }
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
                            Button(onClick = { viewModel.retryProfile(username) }) {
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
private fun UserAvatar(
    imageUrl: String?,
    contentDescription: String?,
    size: Dp
) {
    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
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
    friendsLoading: Boolean,
    friendsError: String?,
    favoritesMetaLoading: Boolean,
    favoritesMetaError: String?,
    animeFavoriteMeta: Map<Int, FavoriteMediaMeta>,
    mangaFavoriteMeta: Map<Int, FavoriteMediaMeta>,
    onLoadFriends: () -> Unit,
    onRefreshFriends: () -> Unit,
    onLoadFavorites: () -> Unit,
    onRefreshFavorites: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    onMangaClick: (Int) -> Unit,
    onUserClick: (String) -> Unit,
    isOwnProfile: Boolean,
    viewerIsFriendWithProfileOwner: Boolean?,
    onFindUserClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Anime", "Manga")
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showFriendsSection by remember { mutableStateOf(false) }
    var showFavoritesSection by remember { mutableStateOf(false) }
    var selectedSignal by remember { mutableStateOf<ProfileSignalCard?>(null) }
    val signalCards = remember(jikanUser.statistics) { buildProfileSignalCards(jikanUser.statistics) }

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
    selectedSignal?.let { signal ->
        AlertDialog(
            onDismissRequest = { selectedSignal = null },
            title = { Text(signal.title) },
            text = { Text(signal.description) },
            confirmButton = {
                TextButton(onClick = { selectedSignal = null }) { Text("Close") }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalIconButton(
                        onClick = onFindUserClick,
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.CenterStart)
                            .offset(x = 8.dp)
                            .zIndex(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Find user"
                            )
                            Text(
                                text = when {
                                    isOwnProfile -> "You"
                                    viewerIsFriendWithProfileOwner == true -> "Friend"
                                    viewerIsFriendWithProfileOwner == false -> "Not friends"
                                    else -> "Friend status"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .zIndex(2f),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        UserAvatar(
                            imageUrl = malUser?.picture ?: jikanUser.images?.jpg?.image_url,
                            contentDescription = "Profile Picture",
                            size = 118.dp
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

                    FilledTonalIconButton(
                        onClick = {
                            val url = jikanUser.url ?: "https://myanimelist.net/profile/${jikanUser.username}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = (-8).dp)
                            .zIndex(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Visit MAL"
                            )
                            Text(
                                text = "Visit MAL",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
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

                val validExternalLinks = jikanUser.external
                    .orEmpty()
                    .filter { link ->
                        val url = link.url?.trim().orEmpty()
                        val name = link.name?.trim().orEmpty()
                        val lowerName = name.lowercase()
                        val lowerUrl = url.lowercase()
                        val isRssLike = lowerName.contains("rss") ||
                            lowerName.contains("feed") ||
                            lowerUrl.contains("rss") ||
                            lowerUrl.contains("feed")

                        url.startsWith("http", ignoreCase = true) &&
                            name.isNotBlank() &&
                            !isRssLike
                    }

                if (validExternalLinks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(validExternalLinks) { link ->
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

                Spacer(modifier = Modifier.height(14.dp))

                FilledTonalButton(
                    onClick = {
                        if (showFriendsSection) {
                            showFriendsSection = false
                        } else {
                            onLoadFriends()
                            showFriendsSection = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (friendsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Icon(
                            imageVector = if (showFriendsSection) Icons.Default.Group else Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(if (showFriendsSection) "Hide Friends" else "Load Friends")
                }

            }
        }
        // Friends section remains below the top profile action row.

        if (showFriendsSection) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Friends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                            AssistChip(
                                onClick = onRefreshFriends,
                                enabled = !friendsLoading,
                                label = { Text("Refresh") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    when {
                        friendsLoading && friends.isEmpty() -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        friendsError != null -> {
                            Text(text = friendsError, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onRefreshFriends) { Text("Retry Friends") }
                        }
                        friends.isEmpty() -> Text("No friends to show.")
                        else -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(friends.take(8)) { friend ->
                                    Column(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .clickable { onUserClick(friend.user.username) },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        UserAvatar(
                                            imageUrl = friend.user.images.jpg?.image_url,
                                            contentDescription = friend.user.username,
                                            size = 60.dp
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
                signalCards = signalCards,
                onSignalClick = { selectedSignal = it },
                animeFavoriteMeta = animeFavoriteMeta,
                onAnimeClick = onAnimeClick
            )
        } else {
            mangaProfileItems(
                user = jikanUser,
                malStats = malUser?.mangaStatistics,
                signalCards = signalCards,
                onSignalClick = { selectedSignal = it },
                mangaFavoriteMeta = mangaFavoriteMeta,
                onMangaClick = onMangaClick
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                FilledTonalButton(
                    onClick = {
                        if (showFavoritesSection) {
                            showFavoritesSection = false
                        } else {
                            showFavoritesSection = true
                            onLoadFavorites()
                        }
                    },
                    enabled = !favoritesMetaLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (favoritesMetaLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading Favorites")
                    } else {
                        Text(if (showFavoritesSection) "Hide Favorites" else "Load Favorites")
                    }
                }
            }
        }

        if (showFavoritesSection) {
            if (favoritesMetaError != null) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(text = favoritesMetaError, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRefreshFavorites) { Text("Retry Favorites") }
                    }
                }
            }
            jikanUser.favorites?.let { favs ->
            favs.characters?.takeIf { it.isNotEmpty() }?.let {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FavoriteCharactersSection(
                            items = it,
                            onRefresh = onRefreshFavorites,
                            refreshEnabled = !favoritesMetaLoading
                        )
                    }
                }
            }
            favs.people?.takeIf { it.isNotEmpty() }?.let {
                item {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 16.dp)) {
                        FavoritePeopleSection(
                            items = it,
                            onRefresh = onRefreshFavorites,
                            refreshEnabled = !favoritesMetaLoading
                        )
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
}

data class ProfileSignalCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun ProfileSignalPill(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
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

private fun buildProfileSignalCards(stats: com.example.myapplication.data.model.JikanUserStatistics?): List<ProfileSignalCard> {
    val anime = stats?.anime
    val manga = stats?.manga
    if (anime == null && manga == null) return emptyList()

    val cards = mutableListOf<ProfileSignalCard>()
    val animeTotal = anime?.total_entries ?: 0
    val mangaTotal = manga?.total_entries ?: 0
    val total = animeTotal + mangaTotal

    if (total > 0) {
        val completedTotal = (anime?.completed ?: 0) + (manga?.completed ?: 0)
        val plannedTotal = (anime?.plan_to_watch ?: 0) + (manga?.plan_to_read ?: 0)
        val rewatchTotal = (anime?.rewatched ?: 0) + (manga?.reread ?: 0)
        val currentActive = (anime?.watching ?: 0) + (manga?.reading ?: 0)
        val daysTotal = (anime?.days_watched ?: 0f) + (manga?.days_read ?: 0f)
        val completionRate = (completedTotal * 100f) / total
        val plannedRate = (plannedTotal * 100f) / total
        val activeRatio = (currentActive * 100f) / total
        val rewatchRate = if (completedTotal > 0) (rewatchTotal * 100f) / completedTotal else 0f
        val animeShare = (animeTotal * 100f) / total
        val mangaShare = (mangaTotal * 100f) / total
        val combinedMeanScore = buildList {
            if (animeTotal > 0) add(anime?.mean_score ?: 0f)
            if (mangaTotal > 0) add(manga?.mean_score ?: 0f)
        }.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f

        if (completionRate >= 65f && total >= 150) {
            cards += ProfileSignalCard("Completionist", "Finishing what they start seems to be a consistent habit here, with plenty of titles carried through to completion.", Icons.Default.TaskAlt, Color(0xFF2E7D32))
        }
        if (plannedRate >= 55f && total >= 100) {
            cards += ProfileSignalCard("Archivist", "A growing backlog seems to build faster than titles are completed, giving the profile a very collector-like feel.", Icons.Default.Inventory2, Color(0xFF546E7A))
        }
        if (activeRatio <= 3f && completedTotal >= 100) {
            cards += ProfileSignalCard("Binge Watcher", "Titles seem to get consumed in bigger bursts rather than slowly followed over time, giving the profile a more finish-first rhythm.", Icons.Default.Bolt, Color(0xFF1E88E5))
        }
        if (total <= 35 && combinedMeanScore >= 8.3f) {
            cards += ProfileSignalCard("Curator", "Choices here feel fairly intentional, with a smaller and more carefully rated collection shaping the overall taste profile.", Icons.Default.AutoAwesome, Color(0xFFAB47BC))
        }
        if (total >= 400) {
            cards += ProfileSignalCard("Veteran", "Years of activity and a large overall footprint give the profile the feel of a long-running MAL history.", Icons.Default.WorkspacePremium, Color(0xFF78909C))
        }
        if (total >= 180 && animeShare in 45f..55f) {
            cards += ProfileSignalCard("Explorer", "Activity stays fairly balanced across anime and manga, suggesting broad curiosity rather than sticking to one lane.", Icons.Default.TravelExplore, Color(0xFF26A69A))
        }
        if (mangaShare >= 75f && mangaTotal >= 100) {
            cards += ProfileSignalCard("Dedicated Reader", "Most of the activity leans heavily toward manga, with enough consistency to suggest a deep and long-term reading habit.", Icons.Default.MenuBook, Color(0xFF7E57C2))
        }
        if (total <= 15 && completionRate <= 35f) {
            cards += ProfileSignalCard("Casual Viewer", "The smaller overall footprint gives the profile a lighter and more relaxed approach to tracking media.", Icons.Default.Chair, Color(0xFF90A4AE))
        }
        if (combinedMeanScore <= 6.2f && completionRate >= 65f) {
            cards += ProfileSignalCard("Critical Rater", "Scores tend to land lower than average even across a fairly completed history, suggesting a more critical reaction style.", Icons.Default.Gavel, Color(0xFFE53935))
        }
        if (rewatchRate >= 10f && completedTotal >= 100) {
            cards += ProfileSignalCard("Rewatcher", "Favorite titles seem worth revisiting here, with enough repeat watching to suggest a strong attachment to familiar series.", Icons.Default.Replay, Color(0xFF00897B))
        }
        if (daysTotal >= 700f && completionRate >= 60f) {
            cards += ProfileSignalCard("Longform Fan", "A large amount of accumulated time points toward comfort with long-running series and bigger commitments.", Icons.Default.LocalFireDepartment, Color(0xFFF4511E))
        }
        if (animeShare >= 75f) {
            cards += ProfileSignalCard("Dedicated Viewer", "Most tracked activity revolves around anime rather than manga, giving the profile a strong viewer-first identity.", Icons.Default.LiveTv, Color(0xFF42A5F5))
        }
    }

    anime?.let {
        val completionRate = if (it.total_entries > 0) (it.completed * 100f) / it.total_entries else 0f
        val dropRate = if (it.total_entries > 0) (it.dropped * 100f) / it.total_entries else 0f
    }

    manga?.let {
        val readingRate = if (it.total_entries > 0) (it.reading * 100f) / it.total_entries else 0f
        if (readingRate >= 35f) {
            cards += ProfileSignalCard("Active Reader", "A noticeable amount of manga stays active at once, suggesting an ongoing and fairly consistent reading habit.", Icons.Default.ImportContacts, Color(0xFFFF7043))
        }
    }

    return cards.distinctBy { it.title }.take(5)
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
                            UserAvatar(
                                imageUrl = friend.user.images.jpg?.image_url,
                                contentDescription = friend.user.username,
                                size = 56.dp
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
    signalCards: List<ProfileSignalCard>,
    onSignalClick: (ProfileSignalCard) -> Unit,
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
    if (signalCards.isNotEmpty()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(signalCards) { signal ->
                        ProfileSignalPill(
                            modifier = Modifier.width(154.dp),
                            label = signal.title,
                            icon = signal.icon,
                            containerColor = signal.color,
                            contentColor = Color.White,
                            onClick = { onSignalClick(signal) }
                        )
                    }
                }
            }
        }
    }
    item { Spacer(modifier = Modifier.height(10.dp)) }

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
    signalCards: List<ProfileSignalCard>,
    onSignalClick: (ProfileSignalCard) -> Unit,
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
    if (signalCards.isNotEmpty()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(signalCards) { signal ->
                        ProfileSignalPill(
                            modifier = Modifier.width(154.dp),
                            label = signal.title,
                            icon = signal.icon,
                            containerColor = signal.color,
                            contentColor = Color.White,
                            onClick = { onSignalClick(signal) }
                        )
                    }
                }
            }
        }
    }
    item { Spacer(modifier = Modifier.height(10.dp)) }

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
fun FavoriteCharactersSection(
    items: List<JikanFavoriteItem>,
    onRefresh: () -> Unit,
    refreshEnabled: Boolean
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Favorite Characters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AssistChip(
                onClick = onRefresh,
                enabled = refreshEnabled,
                label = { Text("Refresh") }
            )
        }
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
fun FavoritePeopleSection(
    items: List<JikanFavoriteItem>,
    onRefresh: () -> Unit,
    refreshEnabled: Boolean
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Favorite People", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AssistChip(
                onClick = onRefresh,
                enabled = refreshEnabled,
                label = { Text("Refresh") }
            )
        }
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
                        if ((meta?.userScore ?: 0) > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2B2B2B).copy(alpha = 0.9f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "User Score",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "${meta?.userScore}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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
                        meta?.userStatus?.let { status ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFavoriteStatusIcon(status),
                                    contentDescription = "User Status",
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getFavoriteStatusIcon(status: String?): ImageVector {
    return when (status?.lowercase()) {
        "watching", "reading" -> Icons.Default.Visibility
        "completed" -> Icons.Default.CheckCircle
        "on_hold" -> Icons.Default.PauseCircle
        "plan_to_watch", "plan_to_read" -> Icons.Default.Schedule
        "dropped" -> Icons.Default.Cancel
        else -> Icons.Default.Info
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

