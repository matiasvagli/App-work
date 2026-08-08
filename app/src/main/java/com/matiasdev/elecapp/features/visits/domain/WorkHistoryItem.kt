package com.matiasdev.elecapp.features.visits.domain

import java.time.Instant

data class WorkHistoryItem(
    val visitId: String,
    val clientId: String,
    val clientName: String,
    val completedAt: Instant,
    val reason: String,
    val workType: String?,
    val workDescription: String?,
    val durationMinutes: Long?,
)
