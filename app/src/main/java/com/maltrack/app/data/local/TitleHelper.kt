package com.maltrack.app.data.local

import com.maltrack.app.data.model.AlternativeTitles
import com.maltrack.app.data.model.AnimeNode
import com.maltrack.app.data.model.MangaNode

fun getPreferredTitle(
    defaultTitle: String,
    alternativeTitles: AlternativeTitles?,
    titleLanguage: TitleLanguage
): String {
    return when (titleLanguage) {
        TitleLanguage.ROMAJI -> defaultTitle
        TitleLanguage.ENGLISH -> alternativeTitles?.en?.takeIf { it.isNotBlank() } ?: defaultTitle
        TitleLanguage.JAPANESE -> alternativeTitles?.ja?.takeIf { it.isNotBlank() } ?: defaultTitle
    }
}

fun AnimeNode.getPreferredTitle(language: TitleLanguage): String {
    return getPreferredTitle(title, alternativeTitles, language)
}

fun MangaNode.getPreferredTitle(language: TitleLanguage): String {
    return getPreferredTitle(title, alternativeTitles, language)
}
