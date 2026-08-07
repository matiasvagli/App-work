package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspection_findings",
    indices = [
        Index(value = ["inspection_id"]),
        Index(value = ["inspection_id", "sort_order"]),
    ],
)
data class InspectionFindingEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "inspection_id") val inspectionId: String,
    val category: String,
    val severity: String,
    val title: String,
    val description: String,
    val recommendation: String?,
    @ColumnInfo(name = "source_type") val sourceType: String,
    @ColumnInfo(name = "source_section") val sourceSection: String?,
    @ColumnInfo(name = "source_entity_id") val sourceEntityId: String?,
    @ColumnInfo(name = "source_value") val sourceValue: Double?,
    @ColumnInfo(name = "source_unit") val sourceUnit: String?,
    @ColumnInfo(name = "rule_code") val ruleCode: String?,
    @ColumnInfo(name = "review_status") val reviewStatus: String,
    @ColumnInfo(name = "include_in_report") val includeInReport: Boolean,
    @ColumnInfo(name = "technician_notes") val technicianNotes: String?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
