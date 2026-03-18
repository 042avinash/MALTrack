package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.DefaultSection
import com.example.myapplication.data.local.ThemePreference
import com.example.myapplication.data.local.TitleLanguage
import com.example.myapplication.data.local.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: UserPreferencesManager
) : ViewModel() {

    val themePreference = prefsManager.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreference.SYSTEM
    )

    val titleLanguage = prefsManager.titleLanguageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TitleLanguage.ROMAJI
    )

    val defaultSection = prefsManager.defaultSectionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DefaultSection.HOME_ANIME
    )

    val defaultAnimeStatus = prefsManager.defaultAnimeStatusFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "all"
    )

    val defaultMangaStatus = prefsManager.defaultMangaStatusFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "all"
    )

    val defaultAnimeSort = prefsManager.defaultAnimeSortFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "list_score"
    )

    val defaultMangaSort = prefsManager.defaultMangaSortFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "list_score"
    )

    val defaultAnimeDiscoverySort = prefsManager.defaultAnimeDiscoverySortFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "members"
    )

    val defaultMangaDiscoverySort = prefsManager.defaultMangaDiscoverySortFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "members"
    )

    val lastUsedSection = prefsManager.lastUsedSectionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "anime_list?initialTab=0"
    )

    val nsfwEnabled = prefsManager.nsfwFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val defaultAnimeListStyleIsGrid = prefsManager.defaultAnimeListStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val defaultMangaListStyleIsGrid = prefsManager.defaultMangaListStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val defaultAnimeDiscoveryStyleIsGrid = prefsManager.defaultAnimeDiscoveryStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val defaultMangaDiscoveryStyleIsGrid = prefsManager.defaultMangaDiscoveryStyleFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val episodeNotificationsEnabled = prefsManager.episodeNotificationsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val homeContinueWatchingEnabled = prefsManager.homeContinueWatchingEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val homeContinueReadingEnabled = prefsManager.homeContinueReadingEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val homeDiscoveryButtonsEnabled = prefsManager.homeDiscoveryButtonsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val homeRandomAnimeEnabled = prefsManager.homeRandomAnimeEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val homeAnimePicksEnabled = prefsManager.homeAnimePicksEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val homeMangaPicksEnabled = prefsManager.homeMangaPicksEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch { prefsManager.saveTheme(preference) }
    }

    fun setTitleLanguage(language: TitleLanguage) {
        viewModelScope.launch { prefsManager.saveTitleLanguage(language) }
    }

    fun setDefaultSection(section: DefaultSection) {
        viewModelScope.launch { prefsManager.saveDefaultSection(section) }
    }

    fun setDefaultSectionAndThen(section: DefaultSection, onSaved: () -> Unit) {
        viewModelScope.launch {
            prefsManager.saveDefaultSection(section)
            onSaved()
        }
    }

    fun setDefaultAnimeStatus(status: String) {
        viewModelScope.launch { prefsManager.saveDefaultAnimeStatus(status) }
    }

    fun setDefaultMangaStatus(status: String) {
        viewModelScope.launch { prefsManager.saveDefaultMangaStatus(status) }
    }

    fun setDefaultAnimeSort(sort: String) {
        viewModelScope.launch { prefsManager.saveDefaultAnimeSort(sort) }
    }

    fun setDefaultMangaSort(sort: String) {
        viewModelScope.launch { prefsManager.saveDefaultMangaSort(sort) }
    }

    fun setDefaultAnimeDiscoverySort(sort: String) {
        viewModelScope.launch { prefsManager.saveDefaultAnimeDiscoverySort(sort) }
    }

    fun setDefaultMangaDiscoverySort(sort: String) {
        viewModelScope.launch { prefsManager.saveDefaultMangaDiscoverySort(sort) }
    }

    fun setLastUsedSection(route: String) {
        viewModelScope.launch { prefsManager.saveLastUsedSection(route) }
    }

    fun setNsfwEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveNsfwToggle(enabled) }
    }

    fun setNsfwEnabledAndThen(enabled: Boolean, onSaved: () -> Unit) {
        viewModelScope.launch {
            prefsManager.saveNsfwToggle(enabled)
            onSaved()
        }
    }

    fun setDefaultAnimeListStyle(isGrid: Boolean) {
        viewModelScope.launch { prefsManager.saveDefaultAnimeListStyle(isGrid) }
    }

    fun setDefaultMangaListStyle(isGrid: Boolean) {
        viewModelScope.launch { prefsManager.saveDefaultMangaListStyle(isGrid) }
    }

    fun setDefaultAnimeDiscoveryStyle(isGrid: Boolean) {
        viewModelScope.launch { prefsManager.saveDefaultAnimeDiscoveryStyle(isGrid) }
    }

    fun setDefaultMangaDiscoveryStyle(isGrid: Boolean) {
        viewModelScope.launch { prefsManager.saveDefaultMangaDiscoveryStyle(isGrid) }
    }

    fun setEpisodeNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveEpisodeNotificationsEnabled(enabled) }
    }

    fun setHomeContinueWatchingEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveHomeContinueWatchingEnabled(enabled) }
    }

    fun setHomeContinueReadingEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveHomeContinueReadingEnabled(enabled) }
    }

    fun setHomeDiscoveryButtonsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveHomeDiscoveryButtonsEnabled(enabled) }
    }

    fun setHomeRandomAnimeEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveHomeRandomAnimeEnabled(enabled) }
    }

    fun setHomeAnimePicksEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveHomeAnimePicksEnabled(enabled) }
    }

    fun setHomeMangaPicksEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveHomeMangaPicksEnabled(enabled) }
    }

    fun setHomeSectionsAndThen(
        continueWatchingEnabled: Boolean,
        continueReadingEnabled: Boolean,
        discoveryButtonsEnabled: Boolean,
        randomAnimeEnabled: Boolean,
        animePicksEnabled: Boolean,
        mangaPicksEnabled: Boolean,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            prefsManager.saveHomeSections(
                continueWatchingEnabled = continueWatchingEnabled,
                continueReadingEnabled = continueReadingEnabled,
                discoveryButtonsEnabled = discoveryButtonsEnabled,
                randomAnimeEnabled = randomAnimeEnabled,
                animePicksEnabled = animePicksEnabled,
                mangaPicksEnabled = mangaPicksEnabled
            )
            onSaved()
        }
    }
}
