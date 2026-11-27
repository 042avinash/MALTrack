package com.maltrack.app.ui

fun formatMediaType(mediaType: String?, fallback: String = "N/A"): String {
    if (mediaType.isNullOrBlank()) return fallback

    return mediaType
        .replace('_', ' ')
        .split(' ')
        .filter(String::isNotBlank)
        .joinToString(" ") { word ->
            when (word.lowercase()) {
                "tv" -> "TV"
                "ova" -> "OVA"
                "ona" -> "ONA"
                else -> word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
}
