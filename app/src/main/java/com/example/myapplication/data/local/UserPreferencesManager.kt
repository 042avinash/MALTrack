package com.example.myapplication.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    private val defaultAnimeListStyleKey = booleanPreferencesKey("default_anime_list_style_grid")
    private val defaultMangaListStyleKey = booleanPreferencesKey("default_manga_list_style_grid")
    private val episodeNotificationsEnabledKey = booleanPreferencesKey("episode_notifications_enabled")
    private val episodeNotificationBaselineKey = stringPreferencesKey("episode_notification_baselines")
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
