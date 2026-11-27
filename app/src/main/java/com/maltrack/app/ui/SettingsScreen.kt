package com.maltrack.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.maltrack.app.BuildConfig
import com.maltrack.app.R
import com.maltrack.app.data.local.DefaultSection
import com.maltrack.app.data.local.ThemePreference
import com.maltrack.app.data.local.ThemePalette
import com.maltrack.app.data.local.ThemeCustomColors
import com.maltrack.app.data.local.TitleLanguage
import com.maltrack.app.notifications.AiringNotificationScheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onFeedbackClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val theme by viewModel.themePreference.collectAsState()
    val themePalette by viewModel.themePalette.collectAsState()
    val customThemeColors by viewModel.customThemeColors.collectAsState()
    val titleLang by viewModel.titleLanguage.collectAsState()
    val defaultSection by viewModel.defaultSection.collectAsState()
    val defaultAnimeStatus by viewModel.defaultAnimeStatus.collectAsState()
    val defaultMangaStatus by viewModel.defaultMangaStatus.collectAsState()
    val defaultAnimeSort by viewModel.defaultAnimeSort.collectAsState()
    val defaultMangaSort by viewModel.defaultMangaSort.collectAsState()
    val defaultAnimeDiscoverySort by viewModel.defaultAnimeDiscoverySort.collectAsState()
    val defaultMangaDiscoverySort by viewModel.defaultMangaDiscoverySort.collectAsState()
    val listSectionScopeValue by viewModel.listSectionScope.collectAsState()
    val listSortingScopeValue by viewModel.listSortingScope.collectAsState()
    val discoverySortingScopeValue by viewModel.discoverySortingScope.collectAsState()
    val listStyleIsGrid by viewModel.listStyleIsGrid.collectAsState()
    val episodeNotificationsEnabled by viewModel.episodeNotificationsEnabled.collectAsState()
    val homeContinueWatchingEnabled by viewModel.homeContinueWatchingEnabled.collectAsState()
    val homeContinueReadingEnabled by viewModel.homeContinueReadingEnabled.collectAsState()
    val homeDiscoveryButtonsEnabled by viewModel.homeDiscoveryButtonsEnabled.collectAsState()
    val homeRandomAnimeEnabled by viewModel.homeRandomAnimeEnabled.collectAsState()
    val homeAnimePicksEnabled by viewModel.homeAnimePicksEnabled.collectAsState()
    val homeMangaPicksEnabled by viewModel.homeMangaPicksEnabled.collectAsState()
    val homeDefaultSearchAnime by viewModel.homeDefaultSearchAnime.collectAsState()
    val nsfwEnabled by viewModel.nsfwEnabled.collectAsState()
    val showNsfwCount by viewModel.showNsfwCount.collectAsState()
    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager
    var pendingBatteryOptimizationPrompt by remember { mutableStateOf(false) }
    var showBatteryOptimizationWarning by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var pendingTestNotification by remember { mutableStateOf(false) }
    val batteryOptimizationIgnoredNow = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true
        }
    }
    val batteryOptimizationRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (!pendingBatteryOptimizationPrompt) return@rememberLauncherForActivityResult
        if (batteryOptimizationIgnoredNow()) {
            pendingBatteryOptimizationPrompt = false
            showBatteryOptimizationWarning = false
        } else {
            showBatteryOptimizationWarning = true
        }
    }
    val maybePromptBatteryOptimization = {
        if (pendingBatteryOptimizationPrompt && !batteryOptimizationIgnoredNow()) {
            launchBatteryOptimizationRequest(context, batteryOptimizationRequestLauncher)
        } else {
            pendingBatteryOptimizationPrompt = false
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setEpisodeNotificationsEnabled(true)
            AiringNotificationScheduler.schedule(context)
            if (pendingTestNotification) {
                AiringNotificationScheduler.triggerTest(context)
                pendingTestNotification = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Test notification queued") }
            } else {
                AiringNotificationScheduler.triggerNow(context)
            }
            maybePromptBatteryOptimization()
        } else {
            if (pendingTestNotification) {
                pendingTestNotification = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Notification permission denied") }
            }
            viewModel.setEpisodeNotificationsEnabled(false)
            pendingBatteryOptimizationPrompt = false
            showBatteryOptimizationWarning = false
        }
    }

    var showLangDialog by remember { mutableStateOf(false) }
    var showSectionDialog by remember { mutableStateOf(false) }
    var showListSectionDialog by remember { mutableStateOf(false) }
    var showListSortingDialog by remember { mutableStateOf(false) }
    var showDiscoverySortingDialog by remember { mutableStateOf(false) }
    val listSectionScope = settingsMediaScope(listSectionScopeValue)
    val listSortingScope = settingsMediaScope(listSortingScopeValue)
    val discoverySortingScope = settingsMediaScope(discoverySortingScopeValue)
    var showListStyleDialog by remember { mutableStateOf(false) }
    var showHomeSectionsDialog by remember { mutableStateOf(false) }
    var showHomeSearchDefaultDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var selectedSettingsPage by remember { mutableStateOf<String?>(null) }
    var startupParentSelection by remember { mutableStateOf<StartupParentOption?>(null) }
    var pendingStartupSection by remember { mutableStateOf<DefaultSection?>(null) }
    var pendingNsfwEnabled by remember { mutableStateOf<Boolean?>(null) }
    var pendingHomeSectionsConfig by remember { mutableStateOf<HomeSectionsConfig?>(null) }
    val navigateUp = {
        when {
            selectedSettingsPage != null -> selectedSettingsPage = null
            else -> onBack()
        }
    }
    BackHandler {
        navigateUp()
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(selectedSettingsPage ?: "Settings") },
                    navigationIcon = {
                        IconButton(onClick = navigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            val settingsScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(settingsScrollState)
                    .verticalScrollbar(settingsScrollState)
            ) {
                if (selectedSettingsPage == null) {
                    SettingsSection(title = "Preferences") {
                    listOf("Appearance", "Startup & Browsing", "Notifications", "Content").forEachIndexed { index, page ->
                        SettingClickableItem(
                            icon = when (page) {
                                "Appearance" -> Icons.Default.Palette
                                "Startup & Browsing" -> Icons.Default.Home
                                "Notifications" -> Icons.Default.Notifications
                                "Content" -> Icons.Default.Visibility
                                else -> Icons.Default.Info
                            },
                            title = page,
                            subtitle = when (page) {
                                "Appearance" -> "${themeModeLabel(theme)} · ${themePaletteLabel(themePalette)}"
                                "Startup & Browsing" -> "${startupPageLabel(defaultSection)} · ${if (homeDefaultSearchAnime) "Anime" else "Manga"} search"
                                "Notifications" -> if (episodeNotificationsEnabled) "Episode alerts on" else "Episode alerts off"
                                else -> "${titleLang.name.lowercase().replaceFirstChar { it.uppercase() }} titles · 18+ ${if (nsfwEnabled) "on" else "off"} · NSFW count ${if (showNsfwCount) "shown" else "hidden"}"
                            },
                            onClick = { selectedSettingsPage = page }
                        )
                        if (index < 3) HorizontalDivider()
                    }
                    }

                    SettingsSection(title = "Account") {
                        SettingClickableItem(
                            icon = Icons.Default.Logout,
                            title = "Log Out",
                            subtitle = "Sign out of your MyAnimeList account",
                            onClick = { showLogoutConfirmation = true }
                        )
                    }

                    SettingsSection(title = "Support & About") {
                        SettingClickableItem(
                            icon = Icons.Default.List,
                            title = "Send Feedback",
                            subtitle = "Report issues or suggest new features",
                            onClick = onFeedbackClick
                        )
                        HorizontalDivider()
                        SettingClickableItem(
                            icon = Icons.Default.Info,
                            title = "API Credits & Docs",
                            subtitle = "See the services MALTrack uses",
                            onClick = { selectedSettingsPage = "API Credits & Docs" }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.maltrack_logo),
                            contentDescription = "MALTrack logo",
                            modifier = Modifier.size(104.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clickable {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/042avinash/MALTrack/releases/latest/download/app-release.apk")
                                        )
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Latest release",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                if (selectedSettingsPage == "Appearance") {
                SettingsSection(title = "Page Styling") {
                    SettingClickableItem(
                        icon = Icons.Default.GridView,
                        title = "Page Style",
                        subtitle = if (listStyleIsGrid) "Grid · Applied everywhere" else "List · Applied everywhere",
                        onClick = { showListStyleDialog = true }
                    )
                }
                Text(
                    text = "Color Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                val modeOptions = listOf(
                    ThemePreference.SYSTEM to "System",
                    ThemePreference.LIGHT to "Light",
                    ThemePreference.DARK to "Dark",
                    ThemePreference.AMOLED to "Black"
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    modeOptions.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = theme == mode,
                            onClick = { viewModel.setThemePreference(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = modeOptions.size),
                            label = { Text(label) }
                        )
                    }
                }
                Text(
                    text = when (theme) {
                        ThemePreference.SYSTEM -> "Follows your device appearance"
                        ThemePreference.LIGHT -> "Always use light colors"
                        ThemePreference.DARK -> "Always use dark colors"
                        ThemePreference.AMOLED -> "Dark mode with pure black backgrounds"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Text(
                    text = "Color Palette",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
                )
                Text(
                    text = "Choose a palette to change the app's colors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                listOf(
                    ThemePalette.SYSTEM,
                    ThemePalette.WINTER,
                    ThemePalette.SPRING,
                    ThemePalette.SUMMER,
                    ThemePalette.FALL,
                    ThemePalette.ANIME_NIGHT,
                    ThemePalette.MANGA_PAPER,
                    ThemePalette.MAL_INSPIRED,
                    ThemePalette.SOFT_PASTEL,
                    ThemePalette.CUSTOM
                ).forEach { palette ->
                    ThemePaletteCard(
                        palette = palette,
                        selected = themePalette == palette,
                        customColors = if (palette == ThemePalette.CUSTOM) customThemeColors else null,
                        onClick = { viewModel.setThemePalette(palette) },
                        onSwatchClick = if (palette == ThemePalette.CUSTOM) {
                            { showCustomColorDialog = true }
                        } else null
                    )
                }
                if (showCustomColorDialog) {
                    CustomThemeColorDialog(
                        initialColor = customThemeColors.primary,
                        onDismiss = { showCustomColorDialog = false },
                        onSave = { color ->
                            viewModel.setCustomThemeColor(color)
                            showCustomColorDialog = false
                        }
                    )
                }
                }

                if (selectedSettingsPage == "Startup & Browsing") {
                SettingsSection(title = "Startup") {
                    SettingClickableItem(
                        icon = Icons.Default.Home,
                        title = "Startup Page",
                        subtitle = "${startupPageLabel(defaultSection)} · Restart required",
                        onClick = { showSectionDialog = true }
                    )
                }

                SettingsSection(title = "Home Browsing") {
                    SettingClickableItem(
                        icon = Icons.Default.Home,
                        title = "Home Sections",
                        subtitle = "${listOf(homeContinueWatchingEnabled, homeContinueReadingEnabled, homeDiscoveryButtonsEnabled, homeRandomAnimeEnabled, homeAnimePicksEnabled, homeMangaPicksEnabled).count { it }} of 6 enabled · Restart required",
                        onClick = { showHomeSectionsDialog = true }
                    )
                    HorizontalDivider()
                    SettingClickableItem(
                        icon = Icons.Default.Movie,
                        title = "Default Home Search",
                        subtitle = if (homeDefaultSearchAnime) "Anime" else "Manga",
                        onClick = { showHomeSearchDefaultDialog = true }
                    )
                }

                SettingsSection(title = "List Defaults") {
                    SettingClickableItem(
                        icon = Icons.Default.List,
                        title = "List Section",
                        subtitle = "Anime: ${listSectionLabel(defaultAnimeStatus)} · Manga: ${listSectionLabel(defaultMangaStatus)}",
                        onClick = { showListSectionDialog = true }
                    )
                    HorizontalDivider()
                    SettingClickableItem(
                        icon = Icons.Default.Sort,
                        title = "List Sorting",
                        subtitle = "Anime: ${sortLabel(defaultAnimeSort)} · Manga: ${sortLabel(defaultMangaSort)}",
                        onClick = { showListSortingDialog = true }
                    )
                }

                SettingsSection(title = "Discovery Sorting") {
                    SettingClickableItem(
                        icon = Icons.Default.Sort,
                        title = "Discovery Sorting",
                        subtitle = "Anime: ${discoverySortLabel(defaultAnimeDiscoverySort)} · Manga: ${discoverySortLabel(defaultMangaDiscoverySort)}",
                        onClick = { showDiscoverySortingDialog = true }
                    )
                }

                SettingsSection(title = "Reset") {
                    SettingClickableItem(
                        icon = Icons.Default.Refresh,
                        title = "Reset Defaults",
                        subtitle = "Restore original anime and manga list settings",
                        onClick = { showResetConfirmation = true }
                    )
                }
                }

                if (selectedSettingsPage == "Notifications") {
                SettingsSection(title = "Notifications") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                pendingBatteryOptimizationPrompt = !episodeNotificationsEnabled
                                toggleEpisodeNotifications(
                                    enabled = !episodeNotificationsEnabled,
                                    viewModel = viewModel,
                                    context = context,
                                    permissionLauncher = notificationPermissionLauncher,
                                    onEnabled = maybePromptBatteryOptimization
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text(
                                    text = "Notify When New Episode Releases",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                text = if (episodeNotificationsEnabled) "On · For anime in your watching list" else "Off · For anime in your watching list",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = episodeNotificationsEnabled,
                            onCheckedChange = { checked ->
                                pendingBatteryOptimizationPrompt = checked
                                toggleEpisodeNotifications(
                                    enabled = checked,
                                    viewModel = viewModel,
                                    context = context,
                                    permissionLauncher = notificationPermissionLauncher,
                                    onEnabled = maybePromptBatteryOptimization
                                )
                            }
                        )
                    }

                    HorizontalDivider()

                    SettingClickableItem(
                        icon = Icons.Default.Notifications,
                        title = "Test Notification",
                        subtitle = "Send a sample notification to verify alerts are working",
                        onClick = {
                            pendingTestNotification = true
                            toggleEpisodeNotifications(
                                enabled = true,
                                viewModel = viewModel,
                                context = context,
                                permissionLauncher = notificationPermissionLauncher,
                                onEnabled = {
                                    pendingTestNotification = false
                                    AiringNotificationScheduler.triggerTest(context)
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Test notification queued") }
                                }
                            )
                        }
                    )

                    HorizontalDivider()
                }

                if (showBatteryOptimizationWarning) {
                    AlertDialog(
                        onDismissRequest = {
                            showBatteryOptimizationWarning = false
                            pendingBatteryOptimizationPrompt = false
                        },
                        title = { Text("Battery optimization is still on") },
                        text = {
                            Text(
                                "If battery optimization stays enabled, background notification checks may stop and you may miss airing alerts."
                            )
                        },
                        confirmButton = {
                            AccentTextButton(onClick = {
                                showBatteryOptimizationWarning = false
                                pendingBatteryOptimizationPrompt = true
                                launchBatteryOptimizationRequest(context, batteryOptimizationRequestLauncher)
                            }) {
                                Text("Retry")
                            }
                        },
                        dismissButton = {
                            AccentTextButton(onClick = {
                                showBatteryOptimizationWarning = false
                                pendingBatteryOptimizationPrompt = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                }

                if (selectedSettingsPage == "Content") {
                SettingClickableItem(
                    icon = Icons.Default.Language,
                    title = "Title Language",
                    subtitle = titleLang.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showLangDialog = true }
                )
                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pendingNsfwEnabled = !nsfwEnabled }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(text = "Show 18+ Content", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Include NSFW content in searches and lists",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = nsfwEnabled,
                        onCheckedChange = { pendingNsfwEnabled = it }
                    )
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setShowNsfwCount(!showNsfwCount) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(text = "Show NSFW Count", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Show NSFW details beside Anime and Manga list counts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = showNsfwCount,
                        onCheckedChange = viewModel::setShowNsfwCount
                    )
                }

                HorizontalDivider()
                }

                if (selectedSettingsPage == "API Credits & Docs") {
                SettingsSection(title = "Data Providers") {
                    SettingClickableItem(
                        icon = Icons.Default.Info,
                        title = "MyAnimeList API v2",
                        subtitle = "Account, anime and manga lists · Open documentation",
                        onClick = { openApiDocumentation(context, "https://myanimelist.net/apiconfig/references/api/v2") }
                    )
                    HorizontalDivider()
                    SettingClickableItem(
                        icon = Icons.Default.Info,
                        title = "Jikan Edge",
                        subtitle = "Profile anime and manga favorites · Open service",
                        onClick = { openApiDocumentation(context, "https://jikan.lucashdo.com/") }
                    )
                    HorizontalDivider()
                    SettingClickableItem(
                        icon = Icons.Default.Info,
                        title = "Tenrai",
                        subtitle = "Anime and manga details · Open documentation",
                        onClick = { openApiDocumentation(context, "https://api.tenrai.org/documentation") }
                    )
                    HorizontalDivider()
                    SettingClickableItem(
                        icon = Icons.Default.Info,
                        title = "AnimeSchedule",
                        subtitle = "Airing schedule data · Open documentation",
                        onClick = { openApiDocumentation(context, "https://animeschedule.net/api/v3/documentation") }
                    )
                }
                }
            }
        }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset Defaults?") },
            text = { Text("Restore the original anime and manga list sections, styles, and sorting options?") },
            confirmButton = {
                AccentTextButton(onClick = {
                    viewModel.resetListDefaults()
                    showResetConfirmation = false
                }) { Text("Reset") }
            },
            dismissButton = {
                AccentTextButton(onClick = { showResetConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showLangDialog) {
        LanguageDialog(
            currentValue = titleLang,
            onDismiss = { showLangDialog = false },
            onSelect = { 
                viewModel.setTitleLanguage(it)
                showLangDialog = false 
            }
        )
    }

    if (showSectionDialog) {
        StartupPageDialog(
            currentValue = defaultSection,
            onDismiss = { showSectionDialog = false },
            onSelect = {
                when (it) {
                    StartupParentOption.HOME -> {
                        pendingStartupSection = when (defaultSection) {
                            DefaultSection.HOME_MANGA -> DefaultSection.HOME_MANGA
                            else -> DefaultSection.HOME_ANIME
                        }
                        showSectionDialog = false
                    }
                    StartupParentOption.MY_LIST -> startupParentSelection = it
                    StartupParentOption.PROFILE -> {
                        pendingStartupSection = DefaultSection.PROFILE
                        showSectionDialog = false
                    }
                    StartupParentOption.LAST_OPENED -> {
                        pendingStartupSection = DefaultSection.LAST_USED
                        showSectionDialog = false
                    }
                }
            }
        )
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Log out?") },
            text = { Text("You can sign back in to your MyAnimeList account at any time.") },
            confirmButton = {
                AccentTextButton(onClick = {
                    showLogoutConfirmation = false
                    onLogout()
                }) { Text("Log Out") }
            },
            dismissButton = {
                AccentTextButton(onClick = { showLogoutConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    startupParentSelection?.let { parent ->
        StartupContentDialog(
            parent = parent,
            currentValue = defaultSection,
            onDismiss = { startupParentSelection = null },
            onSelect = { section ->
                pendingStartupSection = section
                startupParentSelection = null
                showSectionDialog = false
            }
        )
    }

    pendingStartupSection?.let { section ->
        RestartRequiredDialog(
            title = "Restart Required",
            message = "Changing Startup Page requires restarting the app to fully apply. Continue?",
            onDismiss = { pendingStartupSection = null },
            onConfirm = {
                viewModel.setDefaultSectionAndThen(section) {
                    pendingStartupSection = null
                    restartApp(context)
                }
            }
        )
    }

    pendingNsfwEnabled?.let { enabled ->
        RestartRequiredDialog(
            title = "Restart Required",
            message = "Changing 18+ content visibility requires restarting the app to fully apply. Continue?",
            onDismiss = { pendingNsfwEnabled = null },
            onConfirm = {
                viewModel.setNsfwEnabledAndThen(enabled) {
                    pendingNsfwEnabled = null
                    restartApp(context)
                }
            }
        )
    }

    pendingHomeSectionsConfig?.let { config ->
        RestartRequiredDialog(
            title = "Restart Required",
            message = "Saving Home Sections requires restarting the app to fully apply. Continue?",
            onDismiss = { pendingHomeSectionsConfig = null },
            onConfirm = {
                viewModel.setHomeSectionsAndThen(
                    continueWatchingEnabled = config.continueWatchingEnabled,
                    continueReadingEnabled = config.continueReadingEnabled,
                    discoveryButtonsEnabled = config.discoveryButtonsEnabled,
                    randomAnimeEnabled = config.randomAnimeEnabled,
                    animePicksEnabled = config.animePicksEnabled,
                    mangaPicksEnabled = config.mangaPicksEnabled
                ) {
                    pendingHomeSectionsConfig = null
                    restartApp(context)
                }
            }
        )
    }

    if (showHomeSectionsDialog) {
        HomeSectionsDialog(
            currentContinueWatchingEnabled = homeContinueWatchingEnabled,
            currentContinueReadingEnabled = homeContinueReadingEnabled,
            currentDiscoveryButtonsEnabled = homeDiscoveryButtonsEnabled,
            currentRandomAnimeEnabled = homeRandomAnimeEnabled,
            currentAnimePicksEnabled = homeAnimePicksEnabled,
            currentMangaPicksEnabled = homeMangaPicksEnabled,
            onDismiss = { showHomeSectionsDialog = false },
            onSave = { config ->
                showHomeSectionsDialog = false
                pendingHomeSectionsConfig = config
            }
        )
    }

    if (showHomeSearchDefaultDialog) {
        StatusDialog(
            title = "Default Home Search",
            options = listOf("anime", "manga"),
            currentValue = if (homeDefaultSearchAnime) "anime" else "manga",
            onDismiss = { showHomeSearchDefaultDialog = false },
            onSelect = {
                viewModel.setHomeDefaultSearchAnime(it == "anime")
                showHomeSearchDefaultDialog = false
            }
        )
    }

    if (showListStyleDialog) {
        ListStyleDialog(
            title = "Page Style",
            currentIsGrid = listStyleIsGrid,
            onDismiss = { showListStyleDialog = false },
            onSelect = {
                viewModel.setListStyle(it)
                showListStyleDialog = false
            }
        )
    }

    if (showListSectionDialog) {
        ListSectionDefaultsDialog(
            animeStatus = defaultAnimeStatus,
            mangaStatus = defaultMangaStatus,
            scope = listSectionScope,
            onScopeChange = { viewModel.setListSectionScope(it.name) },
            onDismiss = { showListSectionDialog = false },
            onSave = { scope, section ->
                if (scope != SettingsMediaScope.MANGA) {
                    viewModel.setDefaultAnimeStatus(sectionForMedia(section, isAnime = true))
                }
                if (scope != SettingsMediaScope.ANIME) {
                    viewModel.setDefaultMangaStatus(sectionForMedia(section, isAnime = false))
                }
                showListSectionDialog = false
            }
        )
    }

    if (showListSortingDialog) {
        ListSortingDefaultsDialog(
            animeSort = defaultAnimeSort,
            mangaSort = defaultMangaSort,
            scope = listSortingScope,
            onScopeChange = { viewModel.setListSortingScope(it.name) },
            onDismiss = { showListSortingDialog = false },
            onSave = { scope, sort ->
                if (scope != SettingsMediaScope.MANGA) {
                    viewModel.setDefaultAnimeSort(sortForMedia(sort, isAnime = true))
                }
                if (scope != SettingsMediaScope.ANIME) {
                    viewModel.setDefaultMangaSort(sortForMedia(sort, isAnime = false))
                }
                showListSortingDialog = false
            }
        )
    }

    if (showDiscoverySortingDialog) {
        DiscoverySortingDefaultsDialog(
            animeSort = defaultAnimeDiscoverySort,
            mangaSort = defaultMangaDiscoverySort,
            scope = discoverySortingScope,
            onScopeChange = { viewModel.setDiscoverySortingScope(it.name) },
            onDismiss = { showDiscoverySortingDialog = false },
            onSave = { scope, sort ->
                if (scope != SettingsMediaScope.MANGA) viewModel.setDefaultAnimeDiscoverySort(sort)
                if (scope != SettingsMediaScope.ANIME) viewModel.setDefaultMangaDiscoverySort(sort)
                showDiscoverySortingDialog = false
            }
        )
    }

}

private fun openNotificationSettings(context: android.content.Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
    }
    context.startActivity(intent)
}

private fun openApiDocumentation(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private fun launchBatteryOptimizationRequest(
    context: android.content.Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    launcher.launch(intent)
}

private fun toggleEpisodeNotifications(
    enabled: Boolean,
    viewModel: SettingsViewModel,
    context: android.content.Context,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    onEnabled: (() -> Unit)? = null
) {
    if (!enabled) {
        viewModel.setEpisodeNotificationsEnabled(false)
        AiringNotificationScheduler.cancel(context)
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                viewModel.setEpisodeNotificationsEnabled(true)
                AiringNotificationScheduler.schedule(context)
                AiringNotificationScheduler.triggerNow(context)
                onEnabled?.invoke()
            }
            else -> permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        viewModel.setEpisodeNotificationsEnabled(true)
        AiringNotificationScheduler.schedule(context)
        AiringNotificationScheduler.triggerNow(context)
        onEnabled?.invoke()
    }
}

private fun restartApp(context: android.content.Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    val component = launchIntent.component ?: run {
        ContextCompat.startActivity(
            context,
            launchIntent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) },
            null
        )
        (context as? Activity)?.finishAffinity()
        return
    }

    val restartIntent = Intent.makeRestartActivityTask(component)
    ContextCompat.startActivity(context, restartIntent, null)
    (context as? Activity)?.finishAffinity()
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingClickableItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private data class HomeSectionsConfig(
    val continueWatchingEnabled: Boolean,
    val continueReadingEnabled: Boolean,
    val discoveryButtonsEnabled: Boolean,
    val randomAnimeEnabled: Boolean,
    val animePicksEnabled: Boolean,
    val mangaPicksEnabled: Boolean
)

@Composable
private fun HomeSectionsDialog(
    currentContinueWatchingEnabled: Boolean,
    currentContinueReadingEnabled: Boolean,
    currentDiscoveryButtonsEnabled: Boolean,
    currentRandomAnimeEnabled: Boolean,
    currentAnimePicksEnabled: Boolean,
    currentMangaPicksEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (HomeSectionsConfig) -> Unit
) {
    var continueWatchingEnabled by remember(currentContinueWatchingEnabled) { mutableStateOf(currentContinueWatchingEnabled) }
    var continueReadingEnabled by remember(currentContinueReadingEnabled) { mutableStateOf(currentContinueReadingEnabled) }
    var discoveryButtonsEnabled by remember(currentDiscoveryButtonsEnabled) { mutableStateOf(currentDiscoveryButtonsEnabled) }
    var randomAnimeEnabled by remember(currentRandomAnimeEnabled) { mutableStateOf(currentRandomAnimeEnabled) }
    var animePicksEnabled by remember(currentAnimePicksEnabled) { mutableStateOf(currentAnimePicksEnabled) }
    var mangaPicksEnabled by remember(currentMangaPicksEnabled) { mutableStateOf(currentMangaPicksEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Home Sections") },
        text = {
            val sectionsScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(sectionsScrollState)
                    .verticalScrollbar(sectionsScrollState)
            ) {
                SettingToggleItem(
                    icon = Icons.Default.Visibility,
                    title = "Continue Watching",
                    subtitle = "Show your active anime row",
                    checked = continueWatchingEnabled,
                    onCheckedChange = { continueWatchingEnabled = it }
                )
                HorizontalDivider()
                SettingToggleItem(
                    icon = Icons.Default.Visibility,
                    title = "Continue Reading",
                    subtitle = "Show your active manga row",
                    checked = continueReadingEnabled,
                    onCheckedChange = { continueReadingEnabled = it }
                )
                HorizontalDivider()
                SettingToggleItem(
                    icon = Icons.Default.GridView,
                    title = "Discovery Buttons",
                    subtitle = "Seasonal chart and Top 100 quick buttons",
                    checked = discoveryButtonsEnabled,
                    onCheckedChange = { discoveryButtonsEnabled = it }
                )
                HorizontalDivider()
                SettingToggleItem(
                    icon = Icons.Default.Movie,
                    title = "Random Anime",
                    subtitle = "Show random anime launcher button",
                    checked = randomAnimeEnabled,
                    onCheckedChange = { randomAnimeEnabled = it }
                )
                HorizontalDivider()
                SettingToggleItem(
                    icon = Icons.Default.Refresh,
                    title = "Anime Picks",
                    subtitle = "Show anime recommendation row",
                    checked = animePicksEnabled,
                    onCheckedChange = { animePicksEnabled = it }
                )
                HorizontalDivider()
                SettingToggleItem(
                    icon = Icons.Default.Refresh,
                    title = "Manga Picks",
                    subtitle = "Show manga recommendation row",
                    checked = mangaPicksEnabled,
                    onCheckedChange = { mangaPicksEnabled = it }
                )
            }
        },
        confirmButton = {
            AccentTextButton(
                onClick = {
                    onSave(
                        HomeSectionsConfig(
                            continueWatchingEnabled = continueWatchingEnabled,
                            continueReadingEnabled = continueReadingEnabled,
                            discoveryButtonsEnabled = discoveryButtonsEnabled,
                            randomAnimeEnabled = randomAnimeEnabled,
                            animePicksEnabled = animePicksEnabled,
                            mangaPicksEnabled = mangaPicksEnabled
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            AccentTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun themeModeLabel(mode: ThemePreference): String = when (mode) {
    ThemePreference.SYSTEM -> "System"
    ThemePreference.LIGHT -> "Light"
    ThemePreference.DARK -> "Dark"
    ThemePreference.AMOLED -> "Black"
}

private fun themePaletteLabel(palette: ThemePalette): String = when (palette) {
    ThemePalette.SYSTEM -> "System colors"
    ThemePalette.WINTER -> "MAL"
    ThemePalette.SPRING -> "Sakura"
    ThemePalette.SUMMER -> "Fern"
    ThemePalette.FALL -> "Ember"
    ThemePalette.ANIME_NIGHT -> "Midnight"
    ThemePalette.MANGA_PAPER -> "Parchment"
    ThemePalette.MAL_INSPIRED -> "Lagoon"
    ThemePalette.SOFT_PASTEL -> "Graphite"
    ThemePalette.CUSTOM -> "Custom"
}

private fun themePaletteDescription(palette: ThemePalette): String = when (palette) {
    ThemePalette.SYSTEM -> "Use colors from your device"
    ThemePalette.WINTER -> "MAL blue with an icy sky highlight"
    ThemePalette.SPRING -> "Cherry blossom pink with a soft blush highlight"
    ThemePalette.SUMMER -> "Fern green with a fresh mint highlight"
    ThemePalette.FALL -> "Ember terracotta with a warm amber highlight"
    ThemePalette.ANIME_NIGHT -> "Violet tones with coral highlights"
    ThemePalette.MANGA_PAPER -> "Warm ink tones with a cream highlight"
    ThemePalette.MAL_INSPIRED -> "Cool teal tones with aqua highlights"
    ThemePalette.SOFT_PASTEL -> "Neutral charcoal tones with silver highlights"
    ThemePalette.CUSTOM -> "Choose one color; its highlight shade is derived automatically"
}

@Composable
private fun ThemePaletteCard(
    palette: ThemePalette,
    selected: Boolean,
    onClick: () -> Unit,
    customColors: ThemeCustomColors? = null,
    onSwatchClick: (() -> Unit)? = null
) {
    val swatches = when (palette) {
        ThemePalette.SYSTEM -> emptyList()
        ThemePalette.WINTER -> listOf(Color(0xFF315B8A), Color(0xFFDCEBFA))
        ThemePalette.SPRING -> listOf(Color(0xFFAD4F75), Color(0xFFF8DDE8))
        ThemePalette.SUMMER -> listOf(Color(0xFF34764A), Color(0xFFDBEFDE))
        ThemePalette.FALL -> listOf(Color(0xFFB95B2A), Color(0xFFFFE3C4))
        ThemePalette.ANIME_NIGHT -> listOf(Color(0xFF59428C), Color(0xFFEBDDFF))
        ThemePalette.MANGA_PAPER -> listOf(Color(0xFF854331), Color(0xFFFFDAD0))
        ThemePalette.MAL_INSPIRED -> listOf(Color(0xFF006A70), Color(0xFF9CF1FA))
        ThemePalette.SOFT_PASTEL -> listOf(Color(0xFF52525B), Color(0xFFE4E4E7))
        ThemePalette.CUSTOM -> listOf(Color(customColors?.primary ?: ThemeCustomColors().primary))
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(themePaletteLabel(palette), style = MaterialTheme.typography.titleSmall)
                Text(
                    themePaletteDescription(palette),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            swatches.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .then(
                            if (onSwatchClick != null) {
                                Modifier
                                    .clickable { onSwatchClick() }
                                    .semantics { contentDescription = "Edit custom accent color" }
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .background(color, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                    Box(
                        Modifier
                            .size(22.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), CircleShape)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun CustomThemeColorDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val hsv = remember(initialColor) { FloatArray(3).also { AndroidColor.colorToHSV(initialColor, it) } }
    var hue by remember(initialColor) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(initialColor) { mutableFloatStateOf(hsv[1]) }
    var brightness by remember(initialColor) { mutableFloatStateOf(hsv[2]) }
    val previewColor = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness))
    val hueGradient = (0..6).map { step ->
        Color(AndroidColor.HSVToColor(floatArrayOf(step * 60f, 1f, 1f)))
    }
    val saturationGradient = listOf(
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 0f, brightness))),
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, brightness)))
    )
    val brightnessGradient = listOf(
        Color.Black,
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 1f)))
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose custom accent color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(previewColor), RoundedCornerShape(12.dp))
                )
                Text("Hue", style = MaterialTheme.typography.labelMedium)
                ColorGradientSlider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    gradientColors = hueGradient
                )
                Text("Saturation", style = MaterialTheme.typography.labelMedium)
                ColorGradientSlider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    gradientColors = saturationGradient
                )
                Text("Brightness", style = MaterialTheme.typography.labelMedium)
                ColorGradientSlider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0f..1f,
                    gradientColors = brightnessGradient
                )
            }
        },
        confirmButton = {
            AccentTextButton(onClick = { onSave(previewColor) }) { Text("Save color") }
        },
        dismissButton = {
            AccentTextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ColorGradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    gradientColors: List<Color>
) {
    Box(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .align(Alignment.Center)
        ) {
            drawRoundRect(
                brush = Brush.horizontalGradient(gradientColors),
                cornerRadius = CornerRadius(size.height / 2)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.9f),
                cornerRadius = CornerRadius(size.height / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.7f),
                cornerRadius = CornerRadius(size.height / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
fun LanguageDialog(currentValue: TitleLanguage, onDismiss: () -> Unit, onSelect: (TitleLanguage) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Title Language") },
        text = {
            Column {
                listOf(TitleLanguage.ROMAJI, TitleLanguage.ENGLISH, TitleLanguage.JAPANESE).forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentValue == option, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private enum class SettingsMediaScope { ANIME, MANGA, BOTH }

private fun settingsMediaScope(value: String): SettingsMediaScope =
    runCatching { SettingsMediaScope.valueOf(value) }.getOrDefault(SettingsMediaScope.ANIME)

private fun listSectionLabel(section: String): String = when (section) {
    "all" -> "All"
    "watching" -> "Watching"
    "reading" -> "Reading"
    "completed" -> "Completed"
    "on_hold" -> "On Hold"
    "plan_to_watch" -> "Plan to Watch"
    "plan_to_read" -> "Plan to Read"
    "dropped" -> "Dropped"
    else -> section.replace("_", " ").replaceFirstChar { it.uppercase() }
}

private fun canonicalListSection(section: String): String = when (section) {
    "watching", "reading" -> "active"
    "plan_to_watch", "plan_to_read" -> "planned"
    else -> section
}

private fun sectionForMedia(section: String, isAnime: Boolean): String = when (section) {
    "active" -> if (isAnime) "watching" else "reading"
    "planned" -> if (isAnime) "plan_to_watch" else "plan_to_read"
    else -> section
}

private fun sortForMedia(sort: String, isAnime: Boolean): String = when (sort) {
    "title" -> if (isAnime) "anime_title" else "manga_title"
    "start_date" -> if (isAnime) "anime_start_date" else "manga_start_date"
    else -> sort
}

private fun canonicalSort(sort: String): String = when (sort) {
    "anime_title", "manga_title" -> "title"
    "anime_start_date", "manga_start_date" -> "start_date"
    else -> sort
}

@Composable
private fun MediaScopeSelector(scope: SettingsMediaScope, onSelect: (SettingsMediaScope) -> Unit) {
    val options = listOf(
        SettingsMediaScope.ANIME to "Anime",
        SettingsMediaScope.MANGA to "Manga",
        SettingsMediaScope.BOTH to "Both"
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (option, label) ->
            SegmentedButton(
                selected = scope == option,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun ListSectionDefaultsDialog(
    animeStatus: String,
    mangaStatus: String,
    scope: SettingsMediaScope,
    onScopeChange: (SettingsMediaScope) -> Unit,
    onDismiss: () -> Unit,
    onSave: (SettingsMediaScope, String) -> Unit
) {
    var selectedSection by remember(animeStatus, mangaStatus) {
        mutableStateOf(canonicalListSection(animeStatus))
    }
    val options = when (scope) {
        SettingsMediaScope.ANIME -> listOf(
            "all" to "All", "watching" to "Watching", "completed" to "Completed",
            "on_hold" to "On Hold", "plan_to_watch" to "Plan to Watch", "dropped" to "Dropped"
        )
        SettingsMediaScope.MANGA -> listOf(
            "all" to "All", "reading" to "Reading", "completed" to "Completed",
            "on_hold" to "On Hold", "plan_to_read" to "Plan to Read", "dropped" to "Dropped"
        )
        SettingsMediaScope.BOTH -> listOf(
            "all" to "All", "active" to "Watching / Reading", "completed" to "Completed",
            "on_hold" to "On Hold", "planned" to "Plan to Watch / Read", "dropped" to "Dropped"
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Section") },
        text = {
            Column {
                Text("Apply to", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                MediaScopeSelector(scope = scope, onSelect = {
                    onScopeChange(it)
                    selectedSection = canonicalListSection(if (it == SettingsMediaScope.MANGA) mangaStatus else animeStatus)
                })
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                options.forEach { (value, label) ->
                    val canonicalValue = canonicalListSection(value)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedSection = canonicalValue }.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSection == canonicalValue,
                            onClick = { selectedSection = canonicalValue }
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = { onSave(scope, selectedSection) }) { Text("Save") } },
        dismissButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ListSortingDefaultsDialog(
    animeSort: String,
    mangaSort: String,
    scope: SettingsMediaScope,
    onScopeChange: (SettingsMediaScope) -> Unit,
    onDismiss: () -> Unit,
    onSave: (SettingsMediaScope, String) -> Unit
) {
    var selectedSort by remember(animeSort, mangaSort) { mutableStateOf(canonicalSort(animeSort)) }
    val options = listOf(
        "list_score" to "Score",
        "list_updated_at" to "Last Updated",
        "title" to "Title",
        "start_date" to "Start Date"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Sorting") },
        text = {
            Column {
                Text("Apply to", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                MediaScopeSelector(scope = scope, onSelect = {
                    onScopeChange(it)
                    selectedSort = canonicalSort(if (it == SettingsMediaScope.MANGA) mangaSort else animeSort)
                })
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedSort = value }.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedSort == value, onClick = { selectedSort = value })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = { onSave(scope, selectedSort) }) { Text("Save") } },
        dismissButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DiscoverySortingDefaultsDialog(
    animeSort: String,
    mangaSort: String,
    scope: SettingsMediaScope,
    onScopeChange: (SettingsMediaScope) -> Unit,
    onDismiss: () -> Unit,
    onSave: (SettingsMediaScope, String) -> Unit
) {
    var selectedSort by remember(animeSort, mangaSort) {
        mutableStateOf(if (animeSort in listOf("members", "score")) animeSort else "members")
    }
    val options = listOf("members" to "Popularity", "score" to "Score")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Discovery Sorting") },
        text = {
            Column {
                Text("Apply to", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                MediaScopeSelector(scope = scope, onSelect = {
                    onScopeChange(it)
                    selectedSort = if (it == SettingsMediaScope.MANGA) mangaSort else animeSort
                })
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedSort = value }.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedSort == value, onClick = { selectedSort = value })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = { onSave(scope, selectedSort) }) { Text("Save") } },
        dismissButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private enum class StartupParentOption {
    HOME,
    MY_LIST,
    PROFILE,
    LAST_OPENED
}

@Composable
private fun StartupPageDialog(currentValue: DefaultSection, onDismiss: () -> Unit, onSelect: (StartupParentOption) -> Unit) {
    val selectedParent = when (currentValue) {
        DefaultSection.HOME_ANIME, DefaultSection.HOME_MANGA -> StartupParentOption.HOME
        DefaultSection.USER_LIST_ANIME, DefaultSection.USER_LIST_MANGA -> StartupParentOption.MY_LIST
        DefaultSection.PROFILE -> StartupParentOption.PROFILE
        DefaultSection.LAST_USED -> StartupParentOption.LAST_OPENED
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Startup Page") },
        text = {
            Column {
                listOf(
                    StartupParentOption.HOME to "Home",
                    StartupParentOption.MY_LIST to "My List",
                    StartupParentOption.PROFILE to "Profile",
                    StartupParentOption.LAST_OPENED to "Last Opened Page"
                ).forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedParent == option, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StartupContentDialog(
    parent: StartupParentOption,
    currentValue: DefaultSection,
    onDismiss: () -> Unit,
    onSelect: (DefaultSection) -> Unit
) {
    val title = when (parent) {
        StartupParentOption.HOME -> "Startup Home"
        StartupParentOption.MY_LIST -> "Startup My List"
        else -> return
    }
    val options = when (parent) {
        StartupParentOption.HOME -> listOf(
            DefaultSection.HOME_ANIME to "Anime",
            DefaultSection.HOME_MANGA to "Manga"
        )
        StartupParentOption.MY_LIST -> listOf(
            DefaultSection.USER_LIST_ANIME to "Anime",
            DefaultSection.USER_LIST_MANGA to "Manga"
        )
        else -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (section, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(section) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentValue == section, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Back") } }
    )
}

private fun startupPageLabel(section: DefaultSection): String = when (section) {
    DefaultSection.HOME_ANIME -> "Home: Anime"
    DefaultSection.HOME_MANGA -> "Home: Manga"
    DefaultSection.USER_LIST_ANIME -> "My List: Anime"
    DefaultSection.USER_LIST_MANGA -> "My List: Manga"
    DefaultSection.PROFILE -> "Profile"
    DefaultSection.LAST_USED -> "Last Opened Page"
}

@Composable
fun StatusDialog(title: String, options: List<String>, currentValue: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentValue == option, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ListStyleDialog(
    title: String,
    currentIsGrid: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                listOf(true to "Grid", false to "List").forEach { (isGrid, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(isGrid) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentIsGrid == isGrid, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun sortLabel(sort: String): String = when (sort) {
    "list_score" -> "Score"
    "list_updated_at" -> "Last Updated"
    "anime_title", "manga_title" -> "Title"
    "anime_start_date", "manga_start_date" -> "Start Date"
    else -> sort
}

private fun discoverySortLabel(sort: String): String = when (sort) {
    "members" -> "Popularity"
    "score" -> "Score"
    else -> sort
}

@Composable
fun SortDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        "list_score" to "Score",
        "list_updated_at" to "Last Updated",
        "title" to "Title",
        "start_date" to "Start Date"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (option, label) ->
                    val isSelected = when (option) {
                        "title" -> currentValue == "anime_title" || currentValue == "manga_title"
                        "start_date" -> currentValue == "anime_start_date" || currentValue == "manga_start_date"
                        else -> currentValue == option
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DiscoverySortDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        "members" to "Popularity",
        "score" to "Score"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentValue == option, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { AccentTextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
private fun RestartRequiredDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            AccentTextButton(onClick = onConfirm) {
                Text("Continue")
            }
        },
        dismissButton = {
            AccentTextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
