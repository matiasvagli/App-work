package com.matiasdev.elecapp.features.visits.domain

import java.time.Instant

data class VisitWorkSession(
    val id: String,
    val visitId: String,
    val startedAt: Instant,
    val endedAt: Instant?,
    val status: VisitWorkSessionStatus,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

enum class VisitWorkSessionStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
}
