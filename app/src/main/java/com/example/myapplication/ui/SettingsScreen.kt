package com.example.myapplication.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.data.local.DefaultSection
import com.example.myapplication.data.local.ThemePreference
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.notifications.AiringNotificationScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onFeedbackClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val theme by viewModel.themePreference.collectAsState()
    val titleLang by viewModel.titleLanguage.collectAsState()
    val defaultSection by viewModel.defaultSection.collectAsState()
    val defaultAnimeStatus by viewModel.defaultAnimeStatus.collectAsState()
    val defaultMangaStatus by viewModel.defaultMangaStatus.collectAsState()
    val defaultAnimeSort by viewModel.defaultAnimeSort.collectAsState()
    val defaultMangaSort by viewModel.defaultMangaSort.collectAsState()
    val defaultAnimeDiscoverySort by viewModel.defaultAnimeDiscoverySort.collectAsState()
    val defaultMangaDiscoverySort by viewModel.defaultMangaDiscoverySort.collectAsState()
    val defaultAnimeListStyleIsGrid by viewModel.defaultAnimeListStyleIsGrid.collectAsState()
    val defaultMangaListStyleIsGrid by viewModel.defaultMangaListStyleIsGrid.collectAsState()
    val defaultAnimeDiscoveryStyleIsGrid by viewModel.defaultAnimeDiscoveryStyleIsGrid.collectAsState()
    val defaultMangaDiscoveryStyleIsGrid by viewModel.defaultMangaDiscoveryStyleIsGrid.collectAsState()
    val episodeNotificationsEnabled by viewModel.episodeNotificationsEnabled.collectAsState()
    val homeContinueWatchingEnabled by viewModel.homeContinueWatchingEnabled.collectAsState()
    val homeContinueReadingEnabled by viewModel.homeContinueReadingEnabled.collectAsState()
    val homeDiscoveryButtonsEnabled by viewModel.homeDiscoveryButtonsEnabled.collectAsState()
    val homeRandomAnimeEnabled by viewModel.homeRandomAnimeEnabled.collectAsState()
    val homeAnimePicksEnabled by viewModel.homeAnimePicksEnabled.collectAsState()
    val homeMangaPicksEnabled by viewModel.homeMangaPicksEnabled.collectAsState()
    val nsfwEnabled by viewModel.nsfwEnabled.collectAsState()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setEpisodeNotificationsEnabled(true)
            AiringNotificationScheduler.schedule(context)
        } else {
            viewModel.setEpisodeNotificationsEnabled(false)
        }
    }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showSectionDialog by remember { mutableStateOf(false) }
    var showAnimeStatusDialog by remember { mutableStateOf(false) }
    var showMangaStatusDialog by remember { mutableStateOf(false) }
    var showAnimeSortDialog by remember { mutableStateOf(false) }
    var showMangaSortDialog by remember { mutableStateOf(false) }
    var showAnimeDiscoverySortDialog by remember { mutableStateOf(false) }
    var showMangaDiscoverySortDialog by remember { mutableStateOf(false) }
    var showAnimeListStyleDialog by remember { mutableStateOf(false) }
    var showMangaListStyleDialog by remember { mutableStateOf(false) }
    var showAnimeDiscoveryStyleDialog by remember { mutableStateOf(false) }
    var showMangaDiscoveryStyleDialog by remember { mutableStateOf(false) }
    var showHomeSectionsDialog by remember { mutableStateOf(false) }
    var showAppInfoDialog by remember { mutableStateOf(false) }
    var showDefaultsPage by remember { mutableStateOf(false) }
    var startupParentSelection by remember { mutableStateOf<StartupParentOption?>(null) }
    var pendingStartupSection by remember { mutableStateOf<DefaultSection?>(null) }
    var pendingNsfwEnabled by remember { mutableStateOf<Boolean?>(null) }
    var pendingHomeSectionsConfig by remember { mutableStateOf<HomeSectionsConfig?>(null) }

    if (showDefaultsPage) {
        DefaultsScreen(
            defaultAnimeStatus = defaultAnimeStatus,
            defaultMangaStatus = defaultMangaStatus,
            defaultAnimeSort = defaultAnimeSort,
            defaultMangaSort = defaultMangaSort,
            defaultAnimeDiscoverySort = defaultAnimeDiscoverySort,
            defaultMangaDiscoverySort = defaultMangaDiscoverySort,
            defaultAnimeListStyleIsGrid = defaultAnimeListStyleIsGrid,
            defaultMangaListStyleIsGrid = defaultMangaListStyleIsGrid,
            defaultAnimeDiscoveryStyleIsGrid = defaultAnimeDiscoveryStyleIsGrid,
            defaultMangaDiscoveryStyleIsGrid = defaultMangaDiscoveryStyleIsGrid,
            onBack = { showDefaultsPage = false },
            onDefaultAnimeStatusClick = { showAnimeStatusDialog = true },
            onDefaultMangaStatusClick = { showMangaStatusDialog = true },
            onDefaultAnimeListStyleClick = { showAnimeListStyleDialog = true },
            onDefaultMangaListStyleClick = { showMangaListStyleDialog = true },
            onDefaultAnimeDiscoveryStyleClick = { showAnimeDiscoveryStyleDialog = true },
            onDefaultMangaDiscoveryStyleClick = { showMangaDiscoveryStyleDialog = true },
            onDefaultAnimeSortClick = { showAnimeSortDialog = true },
            onDefaultMangaSortClick = { showMangaSortDialog = true },
            onDefaultAnimeDiscoverySortClick = { showAnimeDiscoverySortDialog = true },
            onDefaultMangaDiscoverySortClick = { showMangaDiscoverySortDialog = true }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingClickableItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )

                SettingClickableItem(
                    icon = Icons.Default.Language,
                    title = "Title Language",
                    subtitle = titleLang.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showLangDialog = true }
                )

                SettingClickableItem(
                    icon = Icons.Default.Home,
                    title = "Startup Page",
                    subtitle = startupPageLabel(defaultSection),
                    onClick = { showSectionDialog = true }
                )

                SettingClickableItem(
                    icon = Icons.Default.Tune,
                    title = "Defaults",
                    subtitle = "List section, style, and anime/manga sorting",
                    onClick = { showDefaultsPage = true }
                )

                SettingClickableItem(
                    icon = Icons.Default.Home,
                    title = "Home Sections",
                    subtitle = "Customize which home tabs are visible",
                    onClick = { showHomeSectionsDialog = true }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            toggleEpisodeNotifications(
                                enabled = !episodeNotificationsEnabled,
                                viewModel = viewModel,
                                context = context,
                                permissionLauncher = notificationPermissionLauncher
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(
                                text = "Notify When New Episode Releases",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "For anime in your watching list",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = episodeNotificationsEnabled,
                        onCheckedChange = {
                            toggleEpisodeNotifications(
                                enabled = it,
                                viewModel = viewModel,
                                context = context,
                                permissionLauncher = notificationPermissionLauncher
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
                        toggleEpisodeNotifications(
                            enabled = true,
                            viewModel = viewModel,
                            context = context,
                            permissionLauncher = notificationPermissionLauncher,
                            onEnabled = { AiringNotificationScheduler.triggerTest(context) }
                        )
                    }
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
                            tint = MaterialTheme.colorScheme.primary,
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

                SettingClickableItem(
                    icon = Icons.Default.List,
                    title = "Send Feedback",
                    subtitle = "Report issues or suggest new features",
                    onClick = onFeedbackClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "App Info",
                    subtitle = "View logo, version and creator",
                    onClick = { showAppInfoDialog = true }
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            currentValue = theme,
            onDismiss = { showThemeDialog = false },
            onSelect = { 
                viewModel.setThemePreference(it)
                showThemeDialog = false 
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

    if (showAppInfoDialog) {
        AppInfoDialog(onDismiss = { showAppInfoDialog = false })
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

    if (showAnimeStatusDialog) {
        StatusDialog(
            title = "Default Anime List Section",
            options = listOf("all", "watching", "completed", "on_hold", "plan_to_watch", "dropped"),
            currentValue = defaultAnimeStatus,
            onDismiss = { showAnimeStatusDialog = false },
            onSelect = { 
                viewModel.setDefaultAnimeStatus(it)
                showAnimeStatusDialog = false 
            }
        )
    }

    if (showMangaStatusDialog) {
        StatusDialog(
            title = "Default Manga List Section",
            options = listOf("all", "reading", "completed", "on_hold", "plan_to_read", "dropped"),
            currentValue = defaultMangaStatus,
            onDismiss = { showMangaStatusDialog = false },
            onSelect = {
                viewModel.setDefaultMangaStatus(it)
                showMangaStatusDialog = false
            }
        )
    }

    if (showAnimeListStyleDialog) {
        ListStyleDialog(
            title = "Default Anime List Style",
            currentIsGrid = defaultAnimeListStyleIsGrid,
            onDismiss = { showAnimeListStyleDialog = false },
            onSelect = {
                viewModel.setDefaultAnimeListStyle(it)
                showAnimeListStyleDialog = false
            }
        )
    }

    if (showMangaListStyleDialog) {
        ListStyleDialog(
            title = "Default Manga List Style",
            currentIsGrid = defaultMangaListStyleIsGrid,
            onDismiss = { showMangaListStyleDialog = false },
            onSelect = {
                viewModel.setDefaultMangaListStyle(it)
                showMangaListStyleDialog = false
            }
        )
    }

    if (showAnimeDiscoveryStyleDialog) {
        ListStyleDialog(
            title = "Default Anime Discovery Style",
            currentIsGrid = defaultAnimeDiscoveryStyleIsGrid,
            onDismiss = { showAnimeDiscoveryStyleDialog = false },
            onSelect = {
                viewModel.setDefaultAnimeDiscoveryStyle(it)
                showAnimeDiscoveryStyleDialog = false
            }
        )
    }

    if (showMangaDiscoveryStyleDialog) {
        ListStyleDialog(
            title = "Default Manga Discovery Style",
            currentIsGrid = defaultMangaDiscoveryStyleIsGrid,
            onDismiss = { showMangaDiscoveryStyleDialog = false },
            onSelect = {
                viewModel.setDefaultMangaDiscoveryStyle(it)
                showMangaDiscoveryStyleDialog = false
            }
        )
    }

    if (showAnimeSortDialog) {
        SortDialog(
            title = "Default List Sorting (Anime)",
            currentValue = defaultAnimeSort,
            onDismiss = { showAnimeSortDialog = false },
            onSelect = {
                viewModel.setDefaultAnimeSort(
                    when (it) {
                        "title" -> "anime_title"
                        "start_date" -> "anime_start_date"
                        else -> it
                    }
                )
                showAnimeSortDialog = false
            }
        )
    }

    if (showMangaSortDialog) {
        SortDialog(
            title = "Default List Sorting (Manga)",
            currentValue = defaultMangaSort,
            onDismiss = { showMangaSortDialog = false },
            onSelect = {
                viewModel.setDefaultMangaSort(
                    when (it) {
                        "title" -> "manga_title"
                        "start_date" -> "manga_start_date"
                        else -> it
                    }
                )
                showMangaSortDialog = false
            }
        )
    }

    if (showAnimeDiscoverySortDialog) {
        DiscoverySortDialog(
            title = "Default Anime Discovery Sorting",
            currentValue = defaultAnimeDiscoverySort,
            onDismiss = { showAnimeDiscoverySortDialog = false },
            onSelect = {
                viewModel.setDefaultAnimeDiscoverySort(it)
                showAnimeDiscoverySortDialog = false
            }
        )
    }

    if (showMangaDiscoverySortDialog) {
        DiscoverySortDialog(
            title = "Default Manga Discovery Sorting",
            currentValue = defaultMangaDiscoverySort,
            onDismiss = { showMangaDiscoverySortDialog = false },
            onSelect = {
                viewModel.setDefaultMangaDiscoverySort(it)
                showMangaDiscoverySortDialog = false
            }
        )
    }

}

@Composable
private fun AppInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("App Info") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maltrack_logo),
                    contentDescription = "MALTrack Logo",
                    modifier = Modifier
                        .height(56.dp)
                        .padding(bottom = 10.dp)
                )
                Text(
                    text = "MALTrack v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Created by - Avinash Singh",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
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
            color = MaterialTheme.colorScheme.primary,
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
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
                tint = MaterialTheme.colorScheme.primary,
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
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
            TextButton(
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultsScreen(
    defaultAnimeStatus: String,
    defaultMangaStatus: String,
    defaultAnimeSort: String,
    defaultMangaSort: String,
    defaultAnimeDiscoverySort: String,
    defaultMangaDiscoverySort: String,
    defaultAnimeListStyleIsGrid: Boolean,
    defaultMangaListStyleIsGrid: Boolean,
    defaultAnimeDiscoveryStyleIsGrid: Boolean,
    defaultMangaDiscoveryStyleIsGrid: Boolean,
    onBack: () -> Unit,
    onDefaultAnimeStatusClick: () -> Unit,
    onDefaultMangaStatusClick: () -> Unit,
    onDefaultAnimeListStyleClick: () -> Unit,
    onDefaultMangaListStyleClick: () -> Unit,
    onDefaultAnimeDiscoveryStyleClick: () -> Unit,
    onDefaultMangaDiscoveryStyleClick: () -> Unit,
    onDefaultAnimeSortClick: () -> Unit,
    onDefaultMangaSortClick: () -> Unit,
    onDefaultAnimeDiscoverySortClick: () -> Unit,
    onDefaultMangaDiscoverySortClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Defaults") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Anime") {
                SettingClickableItem(
                    icon = Icons.Default.List,
                    title = "Default List Section",
                    subtitle = defaultAnimeStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    onClick = onDefaultAnimeStatusClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.GridView,
                    title = "Default List Style",
                    subtitle = if (defaultAnimeListStyleIsGrid) "Grid" else "List",
                    onClick = onDefaultAnimeListStyleClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.GridView,
                    title = "Default Discovery List Style",
                    subtitle = if (defaultAnimeDiscoveryStyleIsGrid) "Grid" else "List",
                    onClick = onDefaultAnimeDiscoveryStyleClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.Sort,
                    title = "Default List Sorting",
                    subtitle = sortLabel(defaultAnimeSort),
                    onClick = onDefaultAnimeSortClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.Sort,
                    title = "Default Discovery Sorting",
                    subtitle = discoverySortLabel(defaultAnimeDiscoverySort),
                    onClick = onDefaultAnimeDiscoverySortClick
                )
            }

            SettingsSection(title = "Manga") {
                SettingClickableItem(
                    icon = Icons.Default.List,
                    title = "Default List Section",
                    subtitle = defaultMangaStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    onClick = onDefaultMangaStatusClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.GridView,
                    title = "Default List Style",
                    subtitle = if (defaultMangaListStyleIsGrid) "Grid" else "List",
                    onClick = onDefaultMangaListStyleClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.GridView,
                    title = "Default Discovery List Style",
                    subtitle = if (defaultMangaDiscoveryStyleIsGrid) "Grid" else "List",
                    onClick = onDefaultMangaDiscoveryStyleClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.Sort,
                    title = "Default List Sorting",
                    subtitle = sortLabel(defaultMangaSort),
                    onClick = onDefaultMangaSortClick
                )

                HorizontalDivider()

                SettingClickableItem(
                    icon = Icons.Default.Sort,
                    title = "Default Discovery Sorting",
                    subtitle = discoverySortLabel(defaultMangaDiscoverySort),
                    onClick = onDefaultMangaDiscoverySortClick
                )
            }
        }
    }
}

@Composable
fun ThemeDialog(currentValue: ThemePreference, onDismiss: () -> Unit, onSelect: (ThemePreference) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme") },
        text = {
            Column {
                listOf(ThemePreference.SYSTEM, ThemePreference.LIGHT, ThemePreference.DARK).forEach { option ->
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Back") } }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
            TextButton(onClick = onConfirm) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
