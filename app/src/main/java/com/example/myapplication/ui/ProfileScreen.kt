package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.LiveTv
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
import androidx.compose.ui.res.painterResource
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
import com.example.myapplication.data.model.ProfileDisplayData
import com.example.myapplication.data.model.UserProfile
import com.example.myapplication.data.model.AnimeStatistics
import com.example.myapplication.data.model.MangaStatistics
import com.example.myapplication.data.remote.JikanEdgeFavoriteMedia
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    username: String? = null,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val favoriteAnime by viewModel.favoriteAnime.collectAsState()
    val favoriteManga by viewModel.favoriteManga.collectAsState()
    val favoritesLoading by viewModel.favoritesLoading.collectAsState()
    val favoritesUnavailable by viewModel.favoritesUnavailable.collectAsState()
    val context = LocalContext.current
    var selectedTabIndex by remember(username) { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(username) {
        viewModel.getProfile(username)
    }

    LaunchedEffect(username, (uiState as? ProfileUiState.Success)?.malUser?.id) {
        val ownProfile = username == null || username == "null"
        val profileUser = (uiState as? ProfileUiState.Success)?.malUser
        if (ownProfile && profileUser != null) {
            viewModel.loadOwnFavorites(profileUser.name)
        } else if (!ownProfile) {
            viewModel.clearOwnAnimeFavorites()
        }
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearErrorMessage()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            val isOwnProfile = username == null || username == "null"
            val currentProfile = uiState as? ProfileUiState.Success
            val currentProfileUsername = currentProfile?.profileData?.username
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isOwnProfile) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Button(
                        onClick = {
                            selectedTabIndex = 1 - selectedTabIndex
                            val selectedType = if (selectedTabIndex == 0) "Anime" else "Manga"
                            Toast.makeText(context, "Viewing $selectedType stats", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            if (selectedTabIndex == 0) Icons.Default.Movie else Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedTabIndex == 0) "Anime" else "Manga", fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    if (currentProfileUsername != null) {
                        FilledTonalButton(
                            onClick = {
                                val url = currentProfile?.profileData?.url
                                    ?: "https://myanimelist.net/profile/$currentProfileUsername"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Visit MAL", fontSize = 12.sp)
                        }
                    }
                    if (isOwnProfile) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState is ProfileUiState.Loading,
            onRefresh = {
                viewModel.getProfile(username, forceRefresh = true)
                if (username == null || username == "null") {
                    (uiState as? ProfileUiState.Success)?.malUser?.name?.let {
                        viewModel.loadOwnFavorites(it, forceRefresh = true)
                    }
                }
            },
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
                            profileData = state.profileData,
                            selectedTabIndex = selectedTabIndex,
                            favoriteAnime = if (username == null || username == "null") favoriteAnime else emptyList(),
                            favoriteManga = if (username == null || username == "null") favoriteManga else emptyList(),
                            favoritesLoading = favoritesLoading,
                            favoritesUnavailable = favoritesUnavailable,
                            onRetryFavorites = {
                                state.malUser?.name?.let {
                                    viewModel.loadOwnFavorites(it, forceRefresh = true)
                                }
                            }
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
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.verticalScrollbar(listState),
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
    profileData: ProfileDisplayData,
    selectedTabIndex: Int,
    favoriteAnime: List<JikanEdgeFavoriteMedia> = emptyList(),
    favoriteManga: List<JikanEdgeFavoriteMedia> = emptyList(),
    favoritesLoading: Boolean = false,
    favoritesUnavailable: Boolean = false,
    onRetryFavorites: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedSignal by remember { mutableStateOf<ProfileSignalCard?>(null) }
    val signalCards = remember(profileData.statistics, profileData.joined) { buildProfileSignalCards(profileData.statistics, profileData.joined) }
    val profileListState = rememberLazyListState()

    selectedSignal?.let { signal ->
        AlertDialog(
            onDismissRequest = { selectedSignal = null },
            title = { Text(signal.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(signal.description)
                    if (signal.metric.isNotBlank()) {
                        Text(
                            text = signal.metric,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                AccentTextButton(onClick = { selectedSignal = null }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        state = profileListState,
        modifier = Modifier.fillMaxSize().verticalScrollbar(profileListState),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.width(136.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(136.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(136.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .padding(4.dp)
                            ) {
                                UserAvatar(
                                    imageUrl = malUser?.picture ?: profileData.images?.jpg?.image_url,
                                    contentDescription = "Profile Picture",
                                    size = 128.dp
                                )
                            }
                            if (profileData.is_supporter == true) {
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "MAL Supporter",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = profileData.username,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            profileData.gender?.takeIf { it.isNotBlank() }?.let { value ->
                                ProfileInfoItem(icon = genderIcon(value), text = value)
                            }
                            profileData.birthday?.takeIf { it.isNotBlank() }?.let { value ->
                                ProfileInfoItem(icon = Icons.Default.Cake, text = value)
                            }
                            profileData.location?.takeIf { it.isNotBlank() }?.let { value ->
                                ProfileInfoItem(icon = Icons.Default.LocationOn, text = value)
                            }
                        }
                    }
                }

                val validExternalLinks = profileData.external
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

            }
        }

        if (selectedTabIndex == 0) {
            animeProfileItems(
                user = profileData,
                malStats = malUser?.animeStatistics,
                signalCards = signalCards.filter { it.section == null || it.section == "Anime" },
                onSignalClick = { selectedSignal = it },
                favoriteAnime = favoriteAnime.takeIf { malUser != null },
                favoritesLoading = favoritesLoading,
                favoritesUnavailable = favoritesUnavailable,
                onRetryFavorites = onRetryFavorites,
                onFavoriteAnimeClick = { media ->
                    val url = media.url ?: "https://myanimelist.net/anime/${media.malId}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        } else {
            mangaProfileItems(
                user = profileData,
                malStats = malUser?.mangaStatistics,
                signalCards = signalCards.filter { it.section == null || it.section == "Manga" },
                onSignalClick = { selectedSignal = it },
                favoriteManga = favoriteManga.takeIf { malUser != null },
                favoritesLoading = favoritesLoading,
                favoritesUnavailable = favoritesUnavailable,
                onRetryFavorites = onRetryFavorites,
                onFavoriteMangaClick = { media ->
                    val url = media.url ?: "https://myanimelist.net/manga/${media.malId}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        }

    }
}

@Composable
private fun FavoriteMediaSection(
    title: String,
    media: List<JikanEdgeFavoriteMedia>,
    isLoading: Boolean,
    unavailable: Boolean,
    onRetry: () -> Unit,
    onMediaClick: (JikanEdgeFavoriteMedia) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        when {
            isLoading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            unavailable -> Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorites are temporarily unavailable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetry) { Text("Retry") }
            }
            media.isEmpty() -> Text(
                text = "No favorite ${title.removePrefix("Favorite ").lowercase()} found.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
            )
            else -> LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(media, key = { it.malId }) { item ->
                    Card(
                        modifier = Modifier
                            .width(108.dp)
                            .aspectRatio(0.78f)
                            .clickable { onMediaClick(item) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.displayTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item.displayTitle.ifBlank { "Anime ${item.malId}" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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
    val color: Color,
    val metric: String = "",
    val section: String? = null
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

@Composable
private fun ProfileInfoItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun genderIcon(gender: String): ImageVector = when (gender.trim().lowercase()) {
    "male", "man" -> Icons.Default.Male
    "female", "woman" -> Icons.Default.Female
    "trans", "transgender", "nonbinary", "non-binary", "non binary" -> Icons.Default.Transgender
    else -> Icons.Default.Person
}

private fun buildProfileSignalCards(stats: com.example.myapplication.data.model.ProfileStatistics?, joined: String?): List<ProfileSignalCard> {
    val anime = stats?.anime
    val manga = stats?.manga
    if (anime == null && manga == null) return emptyList()

    val cards = mutableListOf<ProfileSignalCard>()
    val animeTotal = anime?.total_entries ?: 0
    val mangaTotal = manga?.total_entries ?: 0
    val total = animeTotal + mangaTotal

    if (total > 0) {
        val animeCompleted = anime?.completed ?: 0
        val animePlanned = anime?.plan_to_watch ?: 0
        val animeWatching = anime?.watching ?: 0
        val animeRewatched = anime?.rewatched ?: 0
        val animeDays = anime?.days_watched ?: 0f
        val animeMeanScore = anime?.mean_score ?: 0f

        val mangaCompleted = manga?.completed ?: 0
        val mangaPlanned = manga?.plan_to_read ?: 0
        val mangaReading = manga?.reading ?: 0
        val mangaReread = manga?.reread ?: 0
        val mangaDays = manga?.days_read ?: 0f
        val mangaMeanScore = manga?.mean_score ?: 0f

        val completedTotal = animeCompleted + mangaCompleted
        val plannedTotal = animePlanned + mangaPlanned
        val rewatchTotal = animeRewatched + mangaReread
        val currentActive = animeWatching + mangaReading
        val daysTotal = animeDays + mangaDays
        val completionRate = (completedTotal * 100f) / total
        val plannedRate = (plannedTotal * 100f) / total
        val activeRatio = (currentActive * 100f) / total
        val rewatchRate = if (completedTotal > 0) (rewatchTotal * 100f) / completedTotal else 0f
        val animeShare = (animeTotal * 100f) / total
        val mangaShare = (mangaTotal * 100f) / total
        val animeCompletionRate = if (animeTotal > 0) (animeCompleted * 100f) / animeTotal else 0f
        val animePlanRate = if (animeTotal > 0) (animePlanned * 100f) / animeTotal else 0f
        val animeActiveRatio = if (animeTotal > 0) (animeWatching * 100f) / animeTotal else 0f
        val animeRewatchRate = if (animeCompleted > 0) (animeRewatched * 100f) / animeCompleted else 0f
        val mangaCompletionRate = if (mangaTotal > 0) (mangaCompleted * 100f) / mangaTotal else 0f
        val mangaPlanRate = if (mangaTotal > 0) (mangaPlanned * 100f) / mangaTotal else 0f
        val mangaActiveRatio = if (mangaTotal > 0) (mangaReading * 100f) / mangaTotal else 0f
        val mangaRewatchRate = if (mangaCompleted > 0) (mangaReread * 100f) / mangaCompleted else 0f
        val joinedDate = joined?.let { parseJoinedDate(it) }
        val yearsOnMAL = joinedDate?.let { parsedDate ->
            val today = LocalDate.now()
            val daysOnMAL = ChronoUnit.DAYS.between(parsedDate, today).coerceAtLeast(0)
            daysOnMAL / 365.25f
        }

        if (anime != null) {
            if (animeCompletionRate >= 65f && animeTotal >= 150) {
                cards += ProfileSignalCard("Completionist", "Finishing what they start seems to be a consistent habit here, with plenty of anime titles carried through to completion.", Icons.Default.TaskAlt, Color(0xFF2E7D32), section = "Anime")
            }
            if (animePlanRate >= 55f && animeTotal >= 100) {
                cards += ProfileSignalCard("Archivist", "A growing anime backlog seems to build faster than titles are completed, giving the profile a very collector-like feel.", Icons.Default.Inventory2, Color(0xFF546E7A), section = "Anime")
            }
            if (animeActiveRatio <= 2f && animeCompleted >= 100) {
                cards += ProfileSignalCard("Binge Watcher", "Anime tends to be watched in focused bursts, with many titles completed and few left actively in progress.", Icons.Default.Bolt, Color(0xFF1E88E5), section = "Anime")
            }
            if (animeMeanScore <= 6f) {
                cards += ProfileSignalCard("Critic", "Anime scores tend to land lower than average here, giving the profile a more critical and skeptical rating style.", Icons.Default.Gavel, Color(0xFFE53935), section = "Anime")
            }
            if (animeMeanScore >= 8f) {
                cards += ProfileSignalCard("Lenient", "Anime scores tend to land high here, suggesting a softer and more generous rating style overall.", Icons.Default.SentimentSatisfiedAlt, Color(0xFFFFC107), section = "Anime")
            }
            if (animeRewatchRate >= 10f && animeCompleted >= 100) {
                cards += ProfileSignalCard("Rewatcher", "Anime favorites seem worth revisiting here, with enough repeat watching to suggest a strong attachment to familiar series.", Icons.Default.Replay, Color(0xFF00897B), section = "Anime")
            }
        }
        if (manga != null) {
            if (mangaCompletionRate >= 65f && mangaTotal >= 150) {
                cards += ProfileSignalCard("Completionist", "Finishing what they start seems to be a consistent habit here, with plenty of manga titles carried through to completion.", Icons.Default.TaskAlt, Color(0xFF2E7D32), section = "Manga")
            }
            if (mangaPlanRate >= 55f && mangaTotal >= 100) {
                cards += ProfileSignalCard("Archivist", "A growing manga backlog seems to build faster than titles are completed, giving the profile a very collector-like feel.", Icons.Default.Inventory2, Color(0xFF546E7A), section = "Manga")
            }
            if (mangaActiveRatio <= 2f && mangaCompleted >= 100) {
                cards += ProfileSignalCard("Binge Reader", "Manga tends to be read in focused bursts, with many titles completed and few left actively in progress.", Icons.Default.Bolt, Color(0xFF1E88E5), section = "Manga")
            }
            if (mangaMeanScore <= 6f) {
                cards += ProfileSignalCard("Critic", "Manga scores tend to land lower than average here, giving the profile a more critical and skeptical rating style.", Icons.Default.Gavel, Color(0xFFE53935), section = "Manga")
            }
            if (mangaMeanScore >= 8f) {
                cards += ProfileSignalCard("Lenient", "Manga scores tend to land high here, suggesting a softer and more generous rating style overall.", Icons.Default.SentimentSatisfiedAlt, Color(0xFFFFC107), section = "Manga")
            }
            if (mangaRewatchRate >= 10f && mangaCompleted >= 100) {
                cards += ProfileSignalCard("Rereader", "Manga favorites seem worth revisiting here, with enough repeat reading to suggest a strong attachment to familiar series.", Icons.Default.AutoStories, Color(0xFF00897B), section = "Manga")
            }
        }
        fun volumeCard(completedCount: Int, section: String): ProfileSignalCard? {
            val medium = section.lowercase(Locale.US)
            val tier = when {
                completedCount >= 5000 -> "5K"
                completedCount >= 2500 -> "2.5K"
                completedCount >= 2000 -> "2K"
                completedCount >= 1500 -> "1.5K"
                completedCount >= 1000 -> "1K"
                completedCount >= 500 -> "500"
                else -> return null
            }
            val milestone = when (tier) {
                "5K" -> "five thousand"
                "2.5K" -> "2,500"
                "2K" -> "two thousand"
                "1.5K" -> "1,500"
                "1K" -> "one thousand"
                else -> "five hundred"
            }
            return ProfileSignalCard(
                "$tier Club",
                "This profile has completed at least $milestone $medium titles.",
                Icons.Default.EmojiEvents,
                Color(0xFF5E35B1),
                section = section
            )
        }
        anime?.let { volumeCard(it.completed, "Anime")?.let(cards::add) }
        manga?.let { volumeCard(it.completed, "Manga")?.let(cards::add) }
        val tenureCard = when {
            joinedDate != null && joinedDate.year <= 2006 -> ProfileSignalCard(
                "Founding Member",
                "This account reaches back to MAL's early years, giving it the feel of a true long-running legacy profile.",
                Icons.Default.Star,
                Color(0xFF5C6BC0)
            )
            yearsOnMAL != null && yearsOnMAL >= 12f -> ProfileSignalCard(
                "Legacy User",
                "This profile has been around long enough to feel like part of MAL's living history.",
                Icons.Default.WorkspacePremium,
                Color(0xFF7B1FA2)
            )
            yearsOnMAL != null && yearsOnMAL >= 8f -> ProfileSignalCard(
                "Old Guard",
                "This user has been around MAL long enough to feel like part of the platform's established core.",
                Icons.Default.Shield,
                Color(0xFF546E7A)
            )
            yearsOnMAL != null && yearsOnMAL >= 5f -> ProfileSignalCard(
                "Veteran",
                "Years on MAL add up to a long-running history here, giving the profile a more seasoned feel.",
                Icons.Default.MilitaryTech,
                Color(0xFF607D8B)
            )
            yearsOnMAL != null && yearsOnMAL > 1f -> ProfileSignalCard(
                "Seasoned",
                "This user has been on MAL long enough to feel established, but not yet in the long-tenure tier.",
                Icons.Default.Schedule,
                Color(0xFF66BB6A)
            )
            else -> ProfileSignalCard(
                "Newbie",
                "This user is still early in their MAL journey, with a profile that feels relatively new to the platform.",
                Icons.Default.PersonAdd,
                Color(0xFF64B5F6)
            )
        }
        cards += tenureCard
        if (total >= 180 && animeShare in 45f..55f) {
            cards += ProfileSignalCard("Explorer", "Activity stays fairly balanced across anime and manga, suggesting broad curiosity rather than sticking to one lane.", Icons.Default.TravelExplore, Color(0xFF26A69A))
        }
        if (mangaShare >= 75f && mangaTotal >= 100) {
            cards += ProfileSignalCard("Dedicated Reader", "Most of the activity leans heavily toward manga, with enough consistency to suggest a deep and long-term reading habit.", Icons.Default.MenuBook, Color(0xFF7E57C2), section = "Manga")
        }
        if (total <= 15 && completionRate <= 35f) {
            cards += ProfileSignalCard("Casual Tracker", "The smaller combined anime and manga list suggests a relaxed approach to tracking both kinds of titles.", Icons.Default.Chair, Color(0xFF90A4AE))
        }
        val timeWastedCard = when {
            daysTotal >= 365f -> {
                val years = (daysTotal / 365f).toInt().coerceAtLeast(1)
                val label = if (years == 1) "1 Year Wasted" else "$years Years Wasted"
                ProfileSignalCard(
                    label,
                    "This profile has spent about $years ${if (years == 1) "year" else "years"} across anime and manga, turning time into a long-running habit.",
                    Icons.Default.LocalFireDepartment,
                    Color(0xFFF4511E)
                )
            }
            daysTotal >= 30f -> {
                val months = (daysTotal / 30f).toInt().coerceAtLeast(1)
                val label = if (months == 1) "1 Month Wasted" else "$months Months Wasted"
                ProfileSignalCard(
                    label,
                    "This profile has spent about $months ${if (months == 1) "month" else "months"} across anime and manga, showing time steadily going into the medium.",
                    Icons.Default.LocalFireDepartment,
                    Color(0xFFF4511E)
                )
            }
            else -> null
        }
        if (timeWastedCard != null) {
            cards += timeWastedCard
        }
        if (animeShare >= 75f) {
            cards += ProfileSignalCard("Dedicated Viewer", "Most tracked activity revolves around anime rather than manga, giving the profile a strong viewer-first identity.", Icons.Default.LiveTv, Color(0xFF42A5F5), section = "Anime")
        }
    }

    anime?.let {
        val watchingRate = if (it.total_entries > 0) (it.watching * 100f) / it.total_entries else 0f
        if (watchingRate >= 35f) {
            cards += ProfileSignalCard("Active Viewer", "A noticeable share of the anime list is currently being watched, suggesting several series are in progress at once.", Icons.Default.LiveTv, Color(0xFF42A5F5), section = "Anime")
        }
    }

    manga?.let {
        val readingRate = if (it.total_entries > 0) (it.reading * 100f) / it.total_entries else 0f
        if (readingRate >= 35f) {
            cards += ProfileSignalCard("Active Reader", "A noticeable amount of manga stays active at once, suggesting an ongoing and fairly consistent reading habit.", Icons.Default.ImportContacts, Color(0xFFFF7043), section = "Manga")
        }
    }

    return cards.distinctBy { it.title to it.section }.map { card ->
        card.copy(metric = profileSignalMetric(card, stats, joined))
    }
}

private fun profileSignalMetric(
    card: ProfileSignalCard,
    stats: com.example.myapplication.data.model.ProfileStatistics?,
    joined: String?
): String {
    val title = card.title
    val anime = stats?.anime
    val manga = stats?.manga
    val animeTotal = anime?.total_entries ?: 0
    val mangaTotal = manga?.total_entries ?: 0
    val total = animeTotal + mangaTotal
    val completedTotal = (anime?.completed ?: 0) + (manga?.completed ?: 0)
    if (total == 0) return ""

    val animeDominant = card.section?.let { it == "Anime" } ?: (animeTotal >= mangaTotal)
    val primaryMedium = if (animeDominant) "Anime" else "Manga"
    val activeVerb = if (animeDominant) "Watching" else "Reading"
    val repeatVerb = if (animeDominant) "Rewatched" else "Reread"
    val primaryTotal = if (animeDominant) animeTotal else mangaTotal
    val completed = if (animeDominant) anime?.completed ?: 0 else manga?.completed ?: 0
    val planned = if (animeDominant) anime?.plan_to_watch ?: 0 else manga?.plan_to_read ?: 0
    val active = if (animeDominant) anime?.watching ?: 0 else manga?.reading ?: 0
    val rewatched = if (animeDominant) anime?.rewatched ?: 0 else manga?.reread ?: 0
    val meanScore = if (animeDominant) anime?.mean_score ?: 0f else manga?.mean_score ?: 0f
    fun percent(value: Int, denominator: Int) = if (denominator > 0) value * 100 / denominator else 0

    return when {
        title == "Completionist" -> "$primaryMedium completed: ${percent(completed, primaryTotal)}% · $completed of $primaryTotal entries"
        title == "Archivist" -> "$primaryMedium planned: ${percent(planned, primaryTotal)}% · $planned of $primaryTotal entries"
        title == "Binge Watcher" || title == "Binge Reader" -> "$activeVerb: ${percent(active, primaryTotal)}% · $active of $primaryTotal entries"
        title == "Critic" || title == "Lenient" -> "$primaryMedium mean score: ${"%.1f".format(Locale.US, meanScore)}"
        title == "Rewatcher" || title == "Rereader" -> "$repeatVerb: ${percent(rewatched, completed)}% · $rewatched of $completed completed"
        title.endsWith("Club") -> when (card.section) {
            "Anime" -> "Completed anime titles: ${anime?.completed ?: 0}"
            "Manga" -> "Completed manga titles: ${manga?.completed ?: 0}"
            else -> "Completed titles: $completedTotal · Anime: ${anime?.completed ?: 0} · Manga: ${manga?.completed ?: 0}"
        }
        title in setOf("Founding Member", "Legacy User", "Old Guard", "Veteran", "Seasoned", "Newbie") -> {
            val years = parseJoinedDate(joined ?: "")?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).coerceAtLeast(0) / 365.25f }
            years?.let { "Time on MAL: ${"%.1f".format(Locale.US, it)} years" } ?: ""
        }
        title == "Explorer" -> "Anime: ${percent(animeTotal, total)}% · Manga: ${percent(mangaTotal, total)}%"
        title == "Dedicated Reader" -> "Manga entries: ${percent(mangaTotal, total)}% · $mangaTotal of $total combined"
        title == "Dedicated Viewer" -> "Anime entries: ${percent(animeTotal, total)}% · $animeTotal of $total combined"
        title == "Casual Tracker" -> "Combined entries: $total · Anime: $animeTotal · Manga: $mangaTotal"
        title == "Active Reader" -> "Reading: ${percent(manga?.reading ?: 0, mangaTotal)}% · ${manga?.reading ?: 0} of $mangaTotal manga entries"
        title == "Active Viewer" -> "Watching: ${percent(anime?.watching ?: 0, animeTotal)}% · ${anime?.watching ?: 0} of $animeTotal anime entries"
        title.endsWith("Wasted") -> "Time logged: ${"%.0f".format(Locale.US, (anime?.days_watched ?: 0f) + (manga?.days_read ?: 0f))} days"
        else -> ""
    }
}

private fun parseJoinedDate(joined: String): LocalDate? {
    val value = joined.trim()
    if (value.isBlank()) return null

    val isoDateTime = runCatching { OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate() }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
    if (isoDateTime != null) {
        return isoDateTime
    }

    val humanParsers = listOf(
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
    )
    for (formatter in humanParsers) {
        val parsed = runCatching { LocalDate.parse(value, formatter) }.getOrNull()
        if (parsed != null) return parsed
    }

    return null
}

fun androidx.compose.foundation.lazy.LazyListScope.animeProfileItems(
    user: ProfileDisplayData,
    malStats: AnimeStatistics?,
    signalCards: List<ProfileSignalCard>,
    onSignalClick: (ProfileSignalCard) -> Unit,
    favoriteAnime: List<JikanEdgeFavoriteMedia>? = null,
    favoritesLoading: Boolean = false,
    favoritesUnavailable: Boolean = false,
    onRetryFavorites: () -> Unit = {},
    onFavoriteAnimeClick: (JikanEdgeFavoriteMedia) -> Unit = {}
) {
    val stats = user.statistics?.anime
    
    if (stats != null) {
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
    }
    if (signalCards.isNotEmpty()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                val badgesRowState = rememberLazyListState()
                LazyRow(
                    state = badgesRowState,
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
                Spacer(modifier = Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).horizontalScrollbar(badgesRowState))
            }
        }
    }
    item { Spacer(modifier = Modifier.height(10.dp)) }

    if (stats != null) {
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
    }

    item {
        if (favoriteAnime != null) {
            FavoriteMediaSection(
                title = "Favorite Anime",
                media = favoriteAnime,
                isLoading = favoritesLoading,
                unavailable = favoritesUnavailable,
                onRetry = onRetryFavorites,
                onMediaClick = onFavoriteAnimeClick
            )
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Favorite Anime", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Favorite anime aren't available from MAL API v2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.mangaProfileItems(
    user: ProfileDisplayData,
    malStats: MangaStatistics?,
    signalCards: List<ProfileSignalCard>,
    onSignalClick: (ProfileSignalCard) -> Unit,
    favoriteManga: List<JikanEdgeFavoriteMedia>? = null,
    favoritesLoading: Boolean = false,
    favoritesUnavailable: Boolean = false,
    onRetryFavorites: () -> Unit = {},
    onFavoriteMangaClick: (JikanEdgeFavoriteMedia) -> Unit = {}
) {
    val stats = user.statistics?.manga

    if (stats != null) {
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
    }
    if (signalCards.isNotEmpty()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                val badgesRowState = rememberLazyListState()
                LazyRow(
                    state = badgesRowState,
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
                Spacer(modifier = Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).horizontalScrollbar(badgesRowState))
            }
        }
    }
    item { Spacer(modifier = Modifier.height(10.dp)) }

    if (stats != null) {
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
    }

    item {
        if (favoriteManga != null) {
            FavoriteMediaSection(
                title = "Favorite Manga",
                media = favoriteManga,
                isLoading = favoritesLoading,
                unavailable = favoritesUnavailable,
                onRetry = onRetryFavorites,
                onMediaClick = onFavoriteMangaClick
            )
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Favorite Manga", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Favorite manga aren't available from MAL API v2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
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
                val aboutScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(aboutScrollState)
                        .verticalScrollbar(aboutScrollState)
                ) {
                    Text(text = parseBBCode(about), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                AccentTextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
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

