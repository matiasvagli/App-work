package com.matiasdev.elecapp.features.visits.data

import androidx.room.ColumnInfo

data class WorkHistoryItemEntity(
    @ColumnInfo(name = "visit_id") val visitId: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "client_name") val clientName: String,
    @ColumnInfo(name = "completed_at") val completedAt: Long,
    val reason: String,
    @ColumnInfo(name = "work_type") val workType: String?,
    @ColumnInfo(name = "work_description") val workDescription: String?,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Long?,
)
