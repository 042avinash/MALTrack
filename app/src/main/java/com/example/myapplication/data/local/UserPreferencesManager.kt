package com.example.myapplication.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_settings_prefs")

enum class ThemePreference { SYSTEM, LIGHT, DARK }
enum class TitleLanguage { ROMAJI, ENGLISH, JAPANESE }
enum class DefaultSection { 
    HOME_ANIME, 
    HOME_MANGA, 
    USER_LIST_ANIME, 
    USER_LIST_MANGA, 
    PROFILE, 
    LAST_USED 
}

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_preference")
    private val titleLangKey = stringPreferencesKey("title_language")
    private val defaultSectionKey = stringPreferencesKey("default_section")
    private val defaultAnimeStatusKey = stringPreferencesKey("default_anime_status")
    private val defaultMangaStatusKey = stringPreferencesKey("default_manga_status")
    private val defaultAnimeSortKey = stringPreferencesKey("default_anime_sort")
    private val defaultMangaSortKey = stringPreferencesKey("default_manga_sort")
    private val defaultAnimeDiscoverySortKey = stringPreferencesKey("default_anime_discovery_sort")
    private val defaultMangaDiscoverySortKey = stringPreferencesKey("default_manga_discovery_sort")
    private val defaultAnimeListStyleKey = booleanPreferencesKey("default_anime_list_style_grid")
    private val defaultMangaListStyleKey = booleanPreferencesKey("default_manga_list_style_grid")
    private val defaultAnimeDiscoveryStyleKey = booleanPreferencesKey("default_anime_discovery_style_grid")
    private val defaultMangaDiscoveryStyleKey = booleanPreferencesKey("default_manga_discovery_style_grid")
    private val homeContinueWatchingEnabledKey = booleanPreferencesKey("home_continue_watching_enabled")
    private val homeContinueReadingEnabledKey = booleanPreferencesKey("home_continue_reading_enabled")
    private val homeDiscoveryButtonsEnabledKey = booleanPreferencesKey("home_discovery_buttons_enabled")
    private val homeRandomAnimeEnabledKey = booleanPreferencesKey("home_random_anime_enabled")
    private val homeAnimePicksEnabledKey = booleanPreferencesKey("home_anime_picks_enabled")
    private val homeMangaPicksEnabledKey = booleanPreferencesKey("home_manga_picks_enabled")
    private val episodeNotificationsEnabledKey = booleanPreferencesKey("episode_notifications_enabled")
    private val episodeNotificationBaselineKey = stringPreferencesKey("episode_notification_baselines")
    private val recentSearchesKey = stringPreferencesKey("recent_searches")
    private val lastUsedSectionKey = stringPreferencesKey("last_used_section")
    private val nsfwToggleKey = booleanPreferencesKey("nsfw_toggle")
    private val json = Json { ignoreUnknownKeys = true }
    
    val themeFlow: Flow<ThemePreference> = context.userPrefsDataStore.data.map { prefs ->
        ThemePreference.valueOf(prefs[themeKey] ?: ThemePreference.SYSTEM.name)
    }

    val titleLanguageFlow: Flow<TitleLanguage> = context.userPrefsDataStore.data.map { prefs ->
        TitleLanguage.valueOf(prefs[titleLangKey] ?: TitleLanguage.ROMAJI.name)
    }

    val defaultSectionFlow: Flow<DefaultSection> = context.userPrefsDataStore.data.map { prefs ->
        val savedValue = prefs[defaultSectionKey]
        if (savedValue == null) {
            DefaultSection.HOME_ANIME
        } else {
            try {
                DefaultSection.valueOf(savedValue)
            } catch (e: IllegalArgumentException) {
                // Handle migration from old enum values
                when (savedValue) {
                    "HOME" -> DefaultSection.HOME_ANIME
                    "ANIME" -> DefaultSection.USER_LIST_ANIME
                    "MANGA" -> DefaultSection.USER_LIST_MANGA
                    else -> DefaultSection.HOME_ANIME
                }
            }
        }
    }

    val defaultAnimeStatusFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultAnimeStatusKey] ?: "all"
    }

    val defaultMangaStatusFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultMangaStatusKey] ?: "all"
    }

    val defaultAnimeSortFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultAnimeSortKey] ?: "list_score"
    }

    val defaultMangaSortFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultMangaSortKey] ?: "list_score"
    }

    val defaultAnimeDiscoverySortFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultAnimeDiscoverySortKey] ?: "members"
    }

    val defaultMangaDiscoverySortFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultMangaDiscoverySortKey] ?: "members"
    }

    val lastUsedSectionFlow: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[lastUsedSectionKey] ?: "anime_list?initialTab=0"
    }

    val nsfwFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[nsfwToggleKey] ?: false
    }

    val defaultAnimeListStyleFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultAnimeListStyleKey] ?: false
    }

    val defaultMangaListStyleFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultMangaListStyleKey] ?: false
    }

    val defaultAnimeDiscoveryStyleFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultAnimeDiscoveryStyleKey] ?: false
    }

    val defaultMangaDiscoveryStyleFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[defaultMangaDiscoveryStyleKey] ?: false
    }

    val homeContinueWatchingEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[homeContinueWatchingEnabledKey] ?: true
    }

    val homeContinueReadingEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[homeContinueReadingEnabledKey] ?: true
    }

    val homeDiscoveryButtonsEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[homeDiscoveryButtonsEnabledKey] ?: true
    }

    val homeRandomAnimeEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[homeRandomAnimeEnabledKey] ?: true
    }

    val homeAnimePicksEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[homeAnimePicksEnabledKey] ?: true
    }

    val homeMangaPicksEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[homeMangaPicksEnabledKey] ?: true
    }

    val episodeNotificationBaselinesFlow: Flow<Map<String, Int>> = context.userPrefsDataStore.data.map { prefs ->
        prefs[episodeNotificationBaselineKey]?.let { encoded ->
            runCatching {
                json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), encoded)
            }.getOrDefault(emptyMap())
        } ?: emptyMap()
    }

    val episodeNotificationsEnabledFlow: Flow<Boolean> = context.userPrefsDataStore.data.map { prefs ->
        prefs[episodeNotificationsEnabledKey] ?: false
    }

    val recentSearchesFlow: Flow<List<String>> = context.userPrefsDataStore.data.map { prefs ->
        prefs[recentSearchesKey]?.let { encoded ->
            runCatching {
                json.decodeFromString(ListSerializer(String.serializer()), encoded)
            }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    suspend fun saveTheme(theme: ThemePreference) {
        context.userPrefsDataStore.edit { prefs -> prefs[themeKey] = theme.name }
    }

    suspend fun saveTitleLanguage(language: TitleLanguage) {
        context.userPrefsDataStore.edit { prefs -> prefs[titleLangKey] = language.name }
    }

    suspend fun saveDefaultSection(section: DefaultSection) {
        context.userPrefsDataStore.edit { prefs -> 
            prefs[defaultSectionKey] = section.name 
        }
    }

    suspend fun saveDefaultAnimeStatus(status: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultAnimeStatusKey] = status
        }
    }

    suspend fun saveDefaultMangaStatus(status: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultMangaStatusKey] = status
        }
    }

    suspend fun saveDefaultAnimeSort(sortMode: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultAnimeSortKey] = sortMode
        }
    }

    suspend fun saveDefaultMangaSort(sortMode: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultMangaSortKey] = sortMode
        }
    }

    suspend fun saveDefaultAnimeDiscoverySort(sortMode: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultAnimeDiscoverySortKey] = sortMode
        }
    }

    suspend fun saveDefaultMangaDiscoverySort(sortMode: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultMangaDiscoverySortKey] = sortMode
        }
    }

    suspend fun saveLastUsedSection(route: String) {
        context.userPrefsDataStore.edit { prefs -> prefs[lastUsedSectionKey] = route }
    }

    suspend fun saveNsfwToggle(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs -> prefs[nsfwToggleKey] = enabled }
    }

    suspend fun saveDefaultAnimeListStyle(isGrid: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultAnimeListStyleKey] = isGrid
        }
    }

    suspend fun saveDefaultMangaListStyle(isGrid: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultMangaListStyleKey] = isGrid
        }
    }

    suspend fun saveDefaultAnimeDiscoveryStyle(isGrid: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultAnimeDiscoveryStyleKey] = isGrid
        }
    }

    suspend fun saveDefaultMangaDiscoveryStyle(isGrid: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[defaultMangaDiscoveryStyleKey] = isGrid
        }
    }

    suspend fun saveHomeContinueWatchingEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeContinueWatchingEnabledKey] = enabled
        }
    }

    suspend fun saveHomeContinueReadingEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeContinueReadingEnabledKey] = enabled
        }
    }

    suspend fun saveHomeDiscoveryButtonsEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeDiscoveryButtonsEnabledKey] = enabled
        }
    }

    suspend fun saveHomeRandomAnimeEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeRandomAnimeEnabledKey] = enabled
        }
    }

    suspend fun saveHomeAnimePicksEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeAnimePicksEnabledKey] = enabled
        }
    }

    suspend fun saveHomeMangaPicksEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeMangaPicksEnabledKey] = enabled
        }
    }

    suspend fun saveHomeSections(
        continueWatchingEnabled: Boolean,
        continueReadingEnabled: Boolean,
        discoveryButtonsEnabled: Boolean,
        randomAnimeEnabled: Boolean,
        animePicksEnabled: Boolean,
        mangaPicksEnabled: Boolean
    ) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[homeContinueWatchingEnabledKey] = continueWatchingEnabled
            prefs[homeContinueReadingEnabledKey] = continueReadingEnabled
            prefs[homeDiscoveryButtonsEnabledKey] = discoveryButtonsEnabled
            prefs[homeRandomAnimeEnabledKey] = randomAnimeEnabled
            prefs[homeAnimePicksEnabledKey] = animePicksEnabled
            prefs[homeMangaPicksEnabledKey] = mangaPicksEnabled
        }
    }

    suspend fun saveEpisodeNotificationBaselines(baselines: Map<String, Int>) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[episodeNotificationBaselineKey] =
                json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), baselines)
        }
    }

    suspend fun saveEpisodeNotificationsEnabled(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[episodeNotificationsEnabledKey] = enabled
        }
    }

    suspend fun saveRecentSearch(query: String, maxItems: Int = 4) {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return

        context.userPrefsDataStore.edit { prefs ->
            val current = prefs[recentSearchesKey]?.let { encoded ->
                runCatching {
                    json.decodeFromString(ListSerializer(String.serializer()), encoded)
                }.getOrDefault(emptyList())
            } ?: emptyList()

            val updated = (listOf(cleaned) + current.filterNot { it.equals(cleaned, ignoreCase = true) })
                .take(maxItems)

            prefs[recentSearchesKey] =
                json.encodeToString(ListSerializer(String.serializer()), updated)
        }
    }
    
    fun getGridModeFlow(listKey: String): Flow<Boolean> {
        return context.userPrefsDataStore.data.map { prefs ->
            when {
                listKey.startsWith("anime_") -> prefs[defaultAnimeListStyleKey] ?: false
                listKey.startsWith("manga_") -> prefs[defaultMangaListStyleKey] ?: false
                listKey == "seasonal_chart" -> prefs[booleanPreferencesKey("grid_mode_$listKey")] ?: false
                else -> false
            }
        }
    }

    suspend fun saveGridMode(listKey: String, isGrid: Boolean, global: Boolean = false) {
        context.userPrefsDataStore.edit { prefs ->
            when {
                listKey.startsWith("anime_") -> prefs[defaultAnimeListStyleKey] = isGrid
                listKey.startsWith("manga_") -> prefs[defaultMangaListStyleKey] = isGrid
                else -> prefs[booleanPreferencesKey("grid_mode_$listKey")] = isGrid
            }
        }
    }

    fun getSortModeFlow(listKey: String): Flow<String> {
        return context.userPrefsDataStore.data.map { prefs ->
            when {
                listKey.startsWith("anime_") -> prefs[defaultAnimeSortKey] ?: "list_score"
                listKey.startsWith("manga_") -> prefs[defaultMangaSortKey] ?: "list_score"
                else -> "list_score"
            }
        }
    }

    suspend fun saveSortMode(listKey: String, sortMode: String) {
        val key = stringPreferencesKey("sort_mode_$listKey")
        context.userPrefsDataStore.edit { prefs ->
            prefs[key] = sortMode
        }
    }
}
