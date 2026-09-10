package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

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
