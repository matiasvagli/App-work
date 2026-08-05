package com.matiasdev.elecapp.features.visits.domain

import java.time.Instant

data class Visit(
    val id: String,
    val clientId: String,
    val scheduledAt: Instant,
    val estimatedDurationMinutes: Int?,
    val reason: String,
    val notes: String?,
    val status: VisitStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
    val startedAt: Instant? = null,
    val completedAt: Instant? = null,
    val completionNotes: String? = null,
    val pendingWorkNotes: String? = null,
)
