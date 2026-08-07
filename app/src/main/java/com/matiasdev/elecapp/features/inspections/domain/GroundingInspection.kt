package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class GroundingInspection(
    val inspectionId: String,
    val electrodePresent: YesNoUnknown,
    val inspectionChamberAccessible: YesNoUnknown,
    val mainGroundConductorPresent: YesNoUnknown,
    val protectiveConductorContinuity: YesNoUnknown,
    val resistanceOhms: Double?,
    val resistanceOrigin: MeasurementOrigin,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
