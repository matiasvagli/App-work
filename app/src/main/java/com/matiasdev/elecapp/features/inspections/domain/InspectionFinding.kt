package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class InspectionFinding(
    val id: String,
    val inspectionId: String,
    val category: FindingCategory,
    val severity: FindingSeverity,
    val title: String,
    val description: String,
    val recommendation: String?,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
