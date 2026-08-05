package com.matiasdev.elecapp.features.reminders.domain

import java.time.Instant

data class VisitReminder(
    val id: String,
    val visitId: String,
    val minutesBefore: Int,
    val enabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
