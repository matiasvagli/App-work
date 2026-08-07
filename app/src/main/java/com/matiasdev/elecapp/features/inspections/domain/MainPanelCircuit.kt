package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class MainPanelCircuit(
    val id: String,
    val inspectionId: String,
    val sortOrder: Int,
    val destination: CircuitDestination,
    val destinationOther: String?,
    val breakerAmps: Int?,
    val breakerOtherAmps: Int?,
    val breakerCurve: BreakerCurve,
    val conductorSectionMm2: Double?,
    val conductorOtherSectionMm2: Double?,
    val conductorMaterial: ConductorMaterial,
    val conductorMaterialOther: String?,
    val consumptionAmps: Double?,
    val consumptionOrigin: MeasurementOrigin,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
