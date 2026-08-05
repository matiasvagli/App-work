package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pillar_inspections",
    indices = [Index(value = ["inspection_id"])],
)
data class PillarInspectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "inspection_id")
    val inspectionId: String,
    @ColumnInfo(name = "pillar_exists") val exists: Boolean?,
    val accessible: String,
    @ColumnInfo(name = "general_condition") val generalCondition: String,
    @ColumnInfo(name = "main_breaker_present") val mainBreakerPresent: String,
    @ColumnInfo(name = "main_breaker_amps") val mainBreakerAmps: Int?,
    @ColumnInfo(name = "conductor_section_mm2") val conductorSectionMm2: Double?,
    @ColumnInfo(name = "conductor_material") val conductorMaterial: String,
    @ColumnInfo(name = "conductor_condition") val conductorCondition: String,
    @ColumnInfo(name = "neutral_identified") val neutralIdentified: String,
    @ColumnInfo(name = "grounding_visible") val groundingVisible: String,
    @ColumnInfo(name = "protection_compatibility") val protectionCompatibility: String,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
