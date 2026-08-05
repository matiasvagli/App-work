package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class MainPanelInspection(
    val inspectionId: String,
    val accessible: AccessStatus,
    val generalCondition: GeneralCondition,
    val differentialPresent: YesNoUnknown,
    val differentialRatedAmps: Int?,
    val differentialSensitivityMa: Int?,
    val differentialTestResult: DifferentialTestResult,
    val circuitCount: Int?,
    val circuitsIdentified: YesNoPartialUnknown,
    val neutralBarPresent: YesNoUnknown,
    val groundBarPresent: YesNoUnknown,
    val neutralAndGroundSeparated: YesNoUnknown,
    val improvisedConnections: YesNoUnknown,
    val mixedOrIncorrectColors: YesNoUnknown,
    val overheatingSigns: YesNoUnknown,
    val protectionCompatibility: ProtectionCompatibility,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
