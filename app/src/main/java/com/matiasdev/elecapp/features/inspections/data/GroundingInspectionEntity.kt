package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grounding_inspections",
    indices = [Index(value = ["inspection_id"])],
)
data class GroundingInspectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "inspection_id")
    val inspectionId: String,
    @ColumnInfo(name = "electrode_present") val electrodePresent: String,
    @ColumnInfo(name = "inspection_chamber_accessible") val inspectionChamberAccessible: String,
    @ColumnInfo(name = "main_ground_conductor_present") val mainGroundConductorPresent: String,
    @ColumnInfo(name = "protective_conductor_continuity") val protectiveConductorContinuity: String,
    @ColumnInfo(name = "resistance_ohms") val resistanceOhms: Double?,
    @ColumnInfo(name = "resistance_origin") val resistanceOrigin: String,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
