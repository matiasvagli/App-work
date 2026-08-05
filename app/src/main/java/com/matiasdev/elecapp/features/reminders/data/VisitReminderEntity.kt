package com.matiasdev.elecapp.features.reminders.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visit_reminders",
    indices = [Index(value = ["visit_id"])],
)
data class VisitReminderEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "visit_id") val visitId: String,
    @ColumnInfo(name = "minutes_before") val minutesBefore: Int,
    val enabled: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
