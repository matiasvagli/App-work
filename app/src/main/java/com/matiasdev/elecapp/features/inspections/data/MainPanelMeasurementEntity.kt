package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "main_panel_measurements",
    indices = [Index(value = ["inspection_id"]), Index(value = ["inspection_id", "section"])],
)
data class MainPanelMeasurementEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "inspection_id") val inspectionId: String,
    val section: String,
    val type: String,
    val value: Double?,
    val unit: String,
    val origin: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
