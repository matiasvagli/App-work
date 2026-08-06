package com.matiasdev.elecapp.features.visits.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visits",
    indices = [
        Index(value = ["client_id"]),
        Index(value = ["scheduled_at"]),
        Index(value = ["parent_visit_id"]),
    ],
)
data class VisitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "scheduled_at") val scheduledAt: Long,
    @ColumnInfo(name = "estimated_duration_minutes") val estimatedDurationMinutes: Int?,
    val reason: String,
    val notes: String?,
    val status: String,
    @ColumnInfo(name = "started_at") val startedAt: Long?,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "completion_notes") val completionNotes: String?,
    @ColumnInfo(name = "pending_work_notes") val pendingWorkNotes: String?,
    @ColumnInfo(name = "attention_type") val attentionType: String?,
    @ColumnInfo(name = "parent_visit_id") val parentVisitId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
