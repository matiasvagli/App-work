package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspection_unverified_items",
    indices = [Index(value = ["inspection_id"])],
)
data class InspectionUnverifiedItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "inspection_id") val inspectionId: String,
    val type: String,
    val description: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
