package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "main_panel_circuits",
    indices = [Index(value = ["inspection_id"]), Index(value = ["inspection_id", "sort_order"])],
)
data class MainPanelCircuitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "inspection_id") val inspectionId: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    val destination: String,
    @ColumnInfo(name = "destination_other") val destinationOther: String?,
    @ColumnInfo(name = "breaker_amps") val breakerAmps: Int?,
    @ColumnInfo(name = "breaker_other_amps") val breakerOtherAmps: Int?,
    @ColumnInfo(name = "breaker_curve") val breakerCurve: String,
    @ColumnInfo(name = "conductor_section_mm2") val conductorSectionMm2: Double?,
    @ColumnInfo(name = "conductor_other_section_mm2") val conductorOtherSectionMm2: Double?,
    @ColumnInfo(name = "conductor_material") val conductorMaterial: String,
    @ColumnInfo(name = "conductor_material_other") val conductorMaterialOther: String?,
    @ColumnInfo(name = "consumption_amps") val consumptionAmps: Double?,
    @ColumnInfo(name = "consumption_origin") val consumptionOrigin: String,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
