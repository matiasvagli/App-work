package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class ElectricalInspection(
    val id: String,
    val visitId: String,
    val status: InspectionStatus,
    val scope: InspectionScope,
    val inspectionType: InspectionType,
    val generalCondition: GeneralCondition,
    val supplyType: SupplyType,
    val propertyType: PropertyType,
    val reviewReason: String?,
    val reviewedElement: String?,
    val taskDescription: String?,
    val visitReasonSnapshot: String,
    val clientNameSnapshot: String,
    val addressSnapshot: String,
    val localitySnapshot: String,
    val technicianName: String?,
    val accessLimitations: String?,
    val originalTechnicalComment: String?,
    val finalClientReport: String?,
    val startedAt: Instant,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
