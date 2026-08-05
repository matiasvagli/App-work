package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "electrical_inspections",
    indices = [
        Index(value = ["visit_id"]),
    ],
)
data class ElectricalInspectionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "visit_id") val visitId: String,
    val status: String,
    @ColumnInfo(name = "inspection_type") val inspectionType: String,
    @ColumnInfo(name = "general_condition") val generalCondition: String,
    @ColumnInfo(name = "supply_type") val supplyType: String,
    @ColumnInfo(name = "property_type") val propertyType: String,
    @ColumnInfo(name = "visit_reason_snapshot") val visitReasonSnapshot: String,
    @ColumnInfo(name = "client_name_snapshot") val clientNameSnapshot: String,
    @ColumnInfo(name = "address_snapshot") val addressSnapshot: String,
    @ColumnInfo(name = "locality_snapshot") val localitySnapshot: String,
    @ColumnInfo(name = "technician_name") val technicianName: String?,
    @ColumnInfo(name = "access_limitations") val accessLimitations: String?,
    @ColumnInfo(name = "original_technical_comment") val originalTechnicalComment: String?,
    @ColumnInfo(name = "final_client_report") val finalClientReport: String?,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
