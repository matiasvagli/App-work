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
    @ColumnInfo(name = "review_status") val reviewStatus: String,
    @ColumnInfo(name = "pillar_exists") val exists: Boolean?,
    @ColumnInfo(name = "property_type") val propertyType: String?,
    @ColumnInfo(name = "property_type_other") val propertyTypeOther: String?,
    @ColumnInfo(name = "supply_type") val supplyType: String?,
    val accessible: String,
    @ColumnInfo(name = "general_condition") val generalCondition: String,
    @ColumnInfo(name = "main_breaker_present") val mainBreakerPresent: String,
    @ColumnInfo(name = "main_breaker_amps") val mainBreakerAmps: Int?,
    @ColumnInfo(name = "main_breaker_other_amps") val mainBreakerOtherAmps: Int?,
    @ColumnInfo(name = "differential_present") val differentialPresent: String,
    @ColumnInfo(name = "differential_rated_amps") val differentialRatedAmps: Int?,
    @ColumnInfo(name = "differential_other_rated_amps") val differentialOtherRatedAmps: Int?,
    @ColumnInfo(name = "differential_sensitivity_ma") val differentialSensitivityMa: Int?,
    @ColumnInfo(name = "differential_other_sensitivity_ma") val differentialOtherSensitivityMa: Int?,
    @ColumnInfo(name = "differential_test_result") val differentialTestResult: String,
    @ColumnInfo(name = "conductor_section_mm2") val conductorSectionMm2: Double?,
    @ColumnInfo(name = "conductor_other_section_mm2") val conductorOtherSectionMm2: Double?,
    @ColumnInfo(name = "conductor_material") val conductorMaterial: String,
    @ColumnInfo(name = "conductor_material_other") val conductorMaterialOther: String?,
    @ColumnInfo(name = "conductor_condition") val conductorCondition: String,
    @ColumnInfo(name = "neutral_identified") val neutralIdentified: String,
    @ColumnInfo(name = "grounding_visible") val groundingVisible: String,
    @ColumnInfo(name = "protection_compatibility") val protectionCompatibility: String,
    @ColumnInfo(name = "protection_compatibility_notes") val protectionCompatibilityNotes: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
