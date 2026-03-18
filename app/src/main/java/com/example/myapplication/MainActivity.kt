package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.myapplication.data.local.DefaultSection
import com.example.myapplication.data.local.ThemePreference
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MALTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val animeViewModel: AnimeViewModel = hiltViewModel()
            val themePref by settingsViewModel.themePreference.collectAsState()
            val titleLanguage by settingsViewModel.titleLanguage.collectAsState()
            val userPfp by animeViewModel.userPfp.collectAsState()
            
            val isDarkTheme = when (themePref) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            MALTrackTheme(darkTheme = isDarkTheme) {
                loginViewModel = hiltViewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                var lastBottomNavClickAt by remember { mutableLongStateOf(0L) }
                val showBottomBar = currentDestination?.route != "login" && 
                                    currentDestination?.route != "settings" && 
                                    currentDestination?.route != "feedback" && 
                                    currentDestination?.route?.startsWith("anime_details") != true &&
                                    currentDestination?.route?.startsWith("manga_details") != true &&
                                    currentDestination?.route?.startsWith("all_reviews") != true

                val loginUiState by loginViewModel.uiState.collectAsState()
                
                val defaultSection by settingsViewModel.defaultSection.collectAsState()
                val defaultAnimeStatus by settingsViewModel.defaultAnimeStatus.collectAsState()
                val defaultMangaStatus by settingsViewModel.defaultMangaStatus.collectAsState()
                val lastUsedSection by settingsViewModel.lastUsedSection.collectAsState()

                fun navigateToBottomDestination(route: String, destinationPrefix: String) {
                    val now = SystemClock.elapsedRealtime()
                    val isAlreadySelected = currentDestination?.hierarchy?.any {
                        it.route?.startsWith(destinationPrefix) == true
                    } == true

                    if (isAlreadySelected || now - lastBottomNavClickAt < 300) return

                    lastBottomNavClickAt = now
                    runCatching {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
                
                LaunchedEffect(loginUiState) {
                    if (loginUiState is LoginUiState.Success) {
                        if (navController.currentDestination?.route == "login") {
                            val targetRoute = when (defaultSection) {
                                DefaultSection.HOME_ANIME -> "anime_list?initialTab=0"
                                DefaultSection.HOME_MANGA -> "anime_list?initialTab=1"
                                DefaultSection.USER_LIST_ANIME -> {
                                    val subTab = getStatusIndex(defaultAnimeStatus, true)
                                    "user_list?mainTab=0&subTab=$subTab"
                                }
                                DefaultSection.USER_LIST_MANGA -> {
                                    val subTab = getStatusIndex(defaultMangaStatus, false)
                                    "user_list?mainTab=1&subTab=$subTab"
                                }
                                DefaultSection.PROFILE -> "profile_route?username=null"
                                DefaultSection.LAST_USED -> lastUsedSection
                            }
                            
                            navController.navigate(targetRoute) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }

                LaunchedEffect(currentDestination?.route) {
                    val route = currentDestination?.route
                    if (route != null && (route.startsWith("anime_list") || route.startsWith("user_list") || route.startsWith("profile_route"))) {
                        // Special handling to capture arguments if they are part of the last used route
                        val fullRoute = navBackStackEntry?.let { entry ->
                            val routePattern = entry.destination.route ?: return@let route
                            var finalRoute = routePattern
                            entry.arguments?.let { args ->
                                entry.destination.arguments.forEach { (key, argument) ->
                                    val value = args.get(key)
                                    if (value != null) {
                                        finalRoute = finalRoute.replace("{$key}", value.toString())
                                    }
                                }
                            }
                            finalRoute
                        } ?: route
                        
                        settingsViewModel.setLastUsedSection(fullRoute)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = currentDestination?.hierarchy?.any { it.route?.startsWith("anime_list") == true } == true,
                                    onClick = {
                                        navigateToBottomDestination(
                                            route = "anime_list",
                                            destinationPrefix = "anime_list"
                                        )
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "User List") },
                                    label = { Text("My List") },
                                    selected = currentDestination?.hierarchy?.any { it.route?.startsWith("user_list") == true } == true,
                                    onClick = {
                                        val subTab = getStatusIndex(defaultAnimeStatus, true)
                                        navigateToBottomDestination(
                                            route = "user_list?mainTab=0&subTab=$subTab",
                                            destinationPrefix = "user_list"
                                        )
                                    }
                                )
                                NavigationBarItem(
                                    icon = {
                                        if (userPfp.isNullOrBlank()) {
                                            Icon(Icons.Filled.Person, contentDescription = "Profile")
                                        } else {
                                            AsyncImage(
                                                model = userPfp,
                                                contentDescription = "Profile",
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    },
                                    label = { Text("Profile") },
                                    selected = currentDestination?.hierarchy?.any { it.route?.startsWith("profile_route") == true && currentDestination?.route?.contains("username=null") == true } == true,
                                    onClick = {
                                        navigateToBottomDestination(
                                            route = "profile_route?username=null",
                                            destinationPrefix = "profile_route"
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginClick = {
                                    loginViewModel.startLogin(this@MainActivity)
                                }
                            )
                        }
                        composable(
                            route = "anime_list?initialTab={initialTab}",
                            arguments = listOf(
                                navArgument("initialTab") { defaultValue = 0; type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
                            
                            LaunchedEffect(Unit) {
                                if (loginUiState is LoginUiState.Idle) {
                                    navController.navigate("login") { popUpTo(0) }
                                }
                            }

                            AnimeListScreen(
                                viewModel = hiltViewModel(),
                                titleLanguage = titleLanguage,
                                initialTab = initialTab,
                                onAnimeClick = { animeId ->
                                    navController.navigate("anime_details/$animeId")
                                },
                                onMangaClick = { mangaId ->
                                    navController.navigate("manga_details/$mangaId")
                                },
                                onOpenAnimeUserList = {
                                    navigateToBottomDestination(
                                        route = "user_list?mainTab=0&subTab=1",
                                        destinationPrefix = "user_list"
                                    )
                                },
                                onOpenMangaUserList = {
                                    navigateToBottomDestination(
                                        route = "user_list?mainTab=1&subTab=1",
                                        destinationPrefix = "user_list"
                                    )
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable(
                            route = "user_list?mainTab={mainTab}&subTab={subTab}&username={username}",
                            arguments = listOf(
                                navArgument("mainTab") { defaultValue = 0; type = NavType.IntType },
                                navArgument("subTab") { defaultValue = 0; type = NavType.IntType },
                                navArgument("username") { nullable = true; defaultValue = null }
                            )
                        ) { backStackEntry ->
                            val mainTab = backStackEntry.arguments?.getInt("mainTab") ?: 0
                            val subTab = backStackEntry.arguments?.getInt("subTab") ?: 0
                            val username = backStackEntry.arguments?.getString("username")
                            
                            UserListScreen(
                                animeViewModel = hiltViewModel(),
                                mangaViewModel = hiltViewModel(),
                                titleLanguage = titleLanguage,
                                initialMainTab = mainTab,
                                initialSubTab = subTab,
                                username = username,
                                onAnimeClick = { animeId ->
                                    navController.navigate("anime_details/$animeId")
                                },
                                onMangaClick = { mangaId ->
                                    navController.navigate("manga_details/$mangaId")
                                },
                                onOpenSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable(
                            route = "anime_details/{animeId}",
                            arguments = listOf(
                                navArgument("animeId") { type = NavType.IntType }
                            )
                        ) {
                            val viewModel: AnimeDetailsViewModel = hiltViewModel()
                            AnimeDetailsScreen(
                                viewModel = viewModel,
                                titleLanguage = titleLanguage,
                                onBackClick = { navController.popBackStack() },
                                onReviewsClick = { animeId ->
                                    navController.navigate("all_reviews/$animeId")
                                }
                            )
                        }
                        composable(
                            route = "manga_details/{mangaId}",
                            arguments = listOf(
                                navArgument("mangaId") { type = NavType.IntType }
                            )
                        ) {
                            val viewModel: MangaDetailsViewModel = hiltViewModel()
                            MangaDetailsScreen(
                                viewModel = viewModel,
                                titleLanguage = titleLanguage,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "all_reviews/{animeId}",
                            arguments = listOf(
                                navArgument("animeId") { type = NavType.IntType }
                            )
                        ) {
                            val viewModel: AllReviewsViewModel = hiltViewModel()
                            AllReviewsScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "profile_route?username={username}",
                            arguments = listOf(
                                navArgument("username") { nullable = true; defaultValue = null }
                            )
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username")
                            val viewModel: ProfileViewModel = hiltViewModel()
                            ProfileScreen(
                                viewModel = viewModel,
                                username = username,
                                onBack = { navController.popBackStack() },
                                onUserClick = { clickedUser ->
                                    navController.navigate("profile_route?username=$clickedUser")
                                },
                                onListClick = { listUser ->
                                    navController.navigate("user_list?mainTab=0&subTab=0&username=$listUser")
                                },
                                onLogout = {
                                    loginViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = hiltViewModel(),
                                onFeedbackClick = { navController.navigate("feedback") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("feedback") {
                            FeedbackScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getStatusIndex(status: String, isAnime: Boolean): Int {
        val animeStatuses = listOf("all", "watching", "completed", "on_hold", "plan_to_watch", "dropped")
        val mangaStatuses = listOf("all", "reading", "completed", "on_hold", "plan_to_read", "dropped")
        val list = if (isAnime) animeStatuses else mangaStatuses
        
        // Handle semantic mapping for plan_to_watch -> plan_to_read if needed
        val effectiveStatus = when(status) {
            "watching", "reading" -> if (isAnime) "watching" else "reading"
            "plan_to_watch", "plan_to_read" -> if (isAnime) "plan_to_watch" else "plan_to_read"
            else -> status
        }
        
        return list.indexOf(effectiveStatus).coerceAtLeast(0)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            if (uri.scheme == "myanimelist" && uri.host == "auth") {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    loginViewModel.handleAuthRedirect(code)
                }
            }
        }
    }
}
