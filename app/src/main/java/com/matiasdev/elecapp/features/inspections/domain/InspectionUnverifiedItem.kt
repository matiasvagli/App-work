package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class InspectionUnverifiedItem(
    val id: String,
    val inspectionId: String,
    val type: UnverifiedItemType,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
