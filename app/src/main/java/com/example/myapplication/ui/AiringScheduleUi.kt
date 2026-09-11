package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.example.myapplication.data.model.Broadcast
import com.example.myapplication.data.model.NextAiringEpisode
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun rememberAiringEpochSeconds(): State<Long> = produceState(
    initialValue = System.currentTimeMillis() / 1_000L
) {
    while (true) {
        delay(60_000L)
        value = System.currentTimeMillis() / 1_000L
    }
}

fun formatAiringCountdown(seconds: Long): String = when {
    seconds <= 0L -> "Airing now"
    seconds >= 86_400L -> "${seconds / 86_400}d ${(seconds % 86_400) / 3_600}h"
    else -> "${seconds / 3_600}h ${(seconds % 3_600) / 60}m"
}

fun airingTileLabel(
    schedule: NextAiringEpisode?,
    malStatus: String?,
    broadcast: Broadcast?,
    nowEpochSeconds: Long,
    compactFallback: Boolean = false
): String? {
    // MAL is the authoritative lifecycle gate. Schedule providers can retain
    // stale future entries after MAL has marked a series finished.
    if (malStatus != "currently_airing") return null
    if (schedule != null) {
        return "Next: ${formatAiringCountdown(schedule.airingAt - nowEpochSeconds)}"
    }

    val estimatedAiringAt = broadcast?.toEstimatedAiringEpochSeconds()
    return if (estimatedAiringAt != null) {
        "${if (compactFallback) "Est." else "Estimated"} Next: " +
            formatAiringCountdown(estimatedAiringAt - nowEpochSeconds)
    } else {
        "Airing"
    }
}

private fun Broadcast.toEstimatedAiringEpochSeconds(): Long? {
    val day = runCatching { DayOfWeek.valueOf(dayOfTheWeek.uppercase()) }.getOrNull() ?: return null
    val time = startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return null
    val zone = ZoneId.of("Asia/Tokyo")
    val now = ZonedDateTime.now(zone)
    var next = now.with(day).withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
    if (!next.isAfter(now)) next = next.plusWeeks(1)
    return next.toEpochSecond()
}
