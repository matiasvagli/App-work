package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class PillarMeasurement(
    val id: String,
    val inspectionId: String,
    val type: PillarMeasurementType,
    val value: Double?,
    val unit: String,
    val origin: MeasurementOrigin,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
