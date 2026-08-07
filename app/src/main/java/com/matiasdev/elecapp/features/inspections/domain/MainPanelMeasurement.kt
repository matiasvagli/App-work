package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class MainPanelMeasurement(
    val id: String,
    val inspectionId: String,
    val section: MainPanelMeasurementSection,
    val type: MainPanelMeasurementType,
    val value: Double?,
    val unit: String,
    val origin: MeasurementOrigin,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
