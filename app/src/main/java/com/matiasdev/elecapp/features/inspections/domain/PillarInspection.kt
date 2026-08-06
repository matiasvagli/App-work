package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class PillarInspection(
    val inspectionId: String,
    val reviewStatus: InspectionSectionReviewStatus,
    val exists: Boolean?,
    val accessible: AccessStatus,
    val generalCondition: GeneralCondition,
    val mainBreakerPresent: YesNoUnknown,
    val mainBreakerAmps: Int?,
    val conductorSectionMm2: Double?,
    val conductorMaterial: ConductorMaterial,
    val conductorCondition: ConductorCondition,
    val neutralIdentified: YesNoUnknown,
    val groundingVisible: YesNoUnknown,
    val protectionCompatibility: ProtectionCompatibility,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
