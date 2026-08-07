package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "main_panel_inspections",
    indices = [Index(value = ["inspection_id"])],
)
data class MainPanelInspectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "inspection_id")
    val inspectionId: String,
    @ColumnInfo(name = "review_status") val reviewStatus: String,
    val accessible: String,
    @ColumnInfo(name = "general_condition") val generalCondition: String,
    @ColumnInfo(name = "differential_present") val differentialPresent: String,
    @ColumnInfo(name = "differential_rated_amps") val differentialRatedAmps: Int?,
    @ColumnInfo(name = "differential_other_rated_amps") val differentialOtherRatedAmps: Int?,
    @ColumnInfo(name = "differential_sensitivity_ma") val differentialSensitivityMa: Int?,
    @ColumnInfo(name = "differential_other_sensitivity_ma") val differentialOtherSensitivityMa: Int?,
    @ColumnInfo(name = "differential_test_result") val differentialTestResult: String,
    @ColumnInfo(name = "circuit_count") val circuitCount: Int?,
    @ColumnInfo(name = "circuits_identified") val circuitsIdentified: String,
    @ColumnInfo(name = "neutral_bar_present") val neutralBarPresent: String,
    @ColumnInfo(name = "ground_bar_present") val groundBarPresent: String,
    @ColumnInfo(name = "neutral_and_ground_separated") val neutralAndGroundSeparated: String,
    @ColumnInfo(name = "protection_conductors_present") val protectionConductorsPresent: String,
    @ColumnInfo(name = "improvised_connections") val improvisedConnections: String,
    @ColumnInfo(name = "conductor_color_status") val conductorColorStatus: String,
    @ColumnInfo(name = "mixed_or_incorrect_colors") val mixedOrIncorrectColors: String,
    @ColumnInfo(name = "overheating_signs") val overheatingSigns: String,
    @ColumnInfo(name = "exposed_parts_or_damaged_insulation") val exposedPartsOrDamagedInsulation: String,
    @ColumnInfo(name = "protection_compatibility") val protectionCompatibility: String,
    @ColumnInfo(name = "wiring_risks_notes") val wiringRisksNotes: String?,
    @ColumnInfo(name = "protection_conductor_check_result") val protectionConductorCheckResult: String,
    @ColumnInfo(name = "feeder_distance_meters") val feederDistanceMeters: Double?,
    @ColumnInfo(name = "feeder_conductor_section_mm2") val feederConductorSectionMm2: Double?,
    @ColumnInfo(name = "feeder_conductor_material") val feederConductorMaterial: String?,
    @ColumnInfo(name = "feeder_data_origin") val feederDataOrigin: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
