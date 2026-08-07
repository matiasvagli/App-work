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
    val sourceType: FindingSourceType = FindingSourceType.MANUAL,
    val sourceSection: InspectionSection? = null,
    val sourceEntityId: String? = null,
    val sourceValue: Double? = null,
    val sourceUnit: String? = null,
    val ruleCode: String? = null,
    val reviewStatus: FindingReviewStatus = FindingReviewStatus.CONFIRMED,
    val includeInReport: Boolean = true,
    val technicianNotes: String? = null,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
