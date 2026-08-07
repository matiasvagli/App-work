package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class MainPanelInspection(
    val inspectionId: String,
    val reviewStatus: InspectionSectionReviewStatus,
    val accessible: AccessStatus,
    val generalCondition: GeneralCondition,
    val differentialPresent: YesNoUnknown,
    val differentialRatedAmps: Int?,
    val differentialOtherRatedAmps: Int?,
    val differentialSensitivityMa: Int?,
    val differentialOtherSensitivityMa: Int?,
    val differentialTestResult: DifferentialTestResult,
    val circuitCount: Int?,
    val circuitsIdentified: YesNoPartialUnknown,
    val neutralBarPresent: YesNoUnknown,
    val groundBarPresent: YesNoUnknown,
    val neutralAndGroundSeparated: YesNoUnknown,
    val protectionConductorsPresent: YesNoPartialUnknown,
    val improvisedConnections: YesNoUnknown,
    val conductorColorStatus: ConductorColorStatus,
    val mixedOrIncorrectColors: YesNoUnknown,
    val overheatingSigns: YesNoUnknown,
    val exposedPartsOrDamagedInsulation: YesNoUnknown,
    val protectionCompatibility: ProtectionCompatibility,
    val wiringRisksNotes: String?,
    val protectionConductorCheckResult: ProtectionConductorCheckResult,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
