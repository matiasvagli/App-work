package com.matiasdev.elecapp.features.visits.ui

import java.time.Duration

fun Duration.formatTimerText(): String {
    val seconds = seconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return if (hours == 0L) {
        "%02d:%02d:%02d".format(0, minutes, remainingSeconds)
    } else {
        "${hours} h ${minutes.toString().padStart(2, '0')} min ${remainingSeconds.toString().padStart(2, '0')} s"
    }
}

fun Duration.formatCompactDuration(): String {
    val minutes = toMinutes().coerceAtLeast(0)
    val hours = minutes / 60
    val remaining = minutes % 60
    return when {
        hours > 0 && remaining > 0 -> "${hours} h ${remaining} min"
        hours > 0 -> "${hours} h"
        else -> "$remaining min"
    }
}
