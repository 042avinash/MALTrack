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

    val episodeNotificationsEnabled = prefsManager.episodeNotificationsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
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

    fun setEpisodeNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.saveEpisodeNotificationsEnabled(enabled) }
    }
}
