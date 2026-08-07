package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.BreakerCurve
import com.matiasdev.elecapp.features.inspections.domain.CircuitDestination
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgress
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionProgress
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import java.time.Instant

fun ElectricalInspectionEntity.toDomain(): ElectricalInspection = ElectricalInspection(
    id = id,
    visitId = visitId,
    status = enumValue(status, InspectionStatus.DRAFT),
    scope = enumValue(scope, InspectionScope.GENERAL_ASSESSMENT),
    inspectionType = enumValue(inspectionType, InspectionType.VISUAL),
    generalCondition = enumValue(generalCondition, GeneralCondition.NOT_ASSESSED),
    supplyType = enumValue(supplyType, SupplyType.UNKNOWN),
    propertyType = enumValue(propertyType, PropertyType.HOUSE),
    reviewReason = reviewReason,
    reviewedElement = reviewedElement,
    taskDescription = taskDescription,
    visitReasonSnapshot = visitReasonSnapshot,
    clientNameSnapshot = clientNameSnapshot,
    addressSnapshot = addressSnapshot,
    localitySnapshot = localitySnapshot,
    technicianName = technicianName,
    accessLimitations = accessLimitations,
    originalTechnicalComment = originalTechnicalComment,
    finalClientReport = finalClientReport,
    startedAt = Instant.ofEpochMilli(startedAt),
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun ElectricalInspection.toEntity(): ElectricalInspectionEntity = ElectricalInspectionEntity(
    id = id,
    visitId = visitId,
    status = status.name,
    scope = scope.name,
    inspectionType = inspectionType.name,
    generalCondition = generalCondition.name,
    supplyType = supplyType.name,
    propertyType = propertyType.name,
    reviewReason = reviewReason,
    reviewedElement = reviewedElement,
    taskDescription = taskDescription,
    visitReasonSnapshot = visitReasonSnapshot,
    clientNameSnapshot = clientNameSnapshot,
    addressSnapshot = addressSnapshot,
    localitySnapshot = localitySnapshot,
    technicianName = technicianName,
    accessLimitations = accessLimitations,
    originalTechnicalComment = originalTechnicalComment,
    finalClientReport = finalClientReport,
    startedAt = startedAt.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun PillarInspectionEntity.toDomain(): PillarInspection = PillarInspection(
    inspectionId = inspectionId,
    reviewStatus = enumValue(reviewStatus, InspectionSectionReviewStatus.REVIEWED),
    exists = exists,
    propertyType = propertyType?.let { enumValue(it, PropertyType.UNKNOWN) },
    propertyTypeOther = propertyTypeOther,
    supplyType = supplyType?.let { enumValue(it, SupplyType.UNKNOWN) },
    accessible = enumValue(accessible, AccessStatus.NO),
    generalCondition = enumValue(generalCondition, GeneralCondition.NOT_ASSESSED),
    mainBreakerPresent = enumValue(mainBreakerPresent, YesNoUnknown.UNKNOWN),
    mainBreakerAmps = mainBreakerAmps,
    mainBreakerOtherAmps = mainBreakerOtherAmps,
    differentialPresent = enumValue(differentialPresent, YesNoUnknown.UNKNOWN),
    differentialRatedAmps = differentialRatedAmps,
    differentialOtherRatedAmps = differentialOtherRatedAmps,
    differentialSensitivityMa = differentialSensitivityMa,
    differentialOtherSensitivityMa = differentialOtherSensitivityMa,
    differentialTestResult = enumValue(differentialTestResult, DifferentialTestResult.NOT_TESTED),
    conductorSectionMm2 = conductorSectionMm2,
    conductorOtherSectionMm2 = conductorOtherSectionMm2,
    conductorMaterial = enumValue(conductorMaterial, ConductorMaterial.UNKNOWN),
    conductorMaterialOther = conductorMaterialOther,
    conductorCondition = enumValue(conductorCondition, ConductorCondition.NOT_ASSESSED),
    neutralIdentified = enumValue(neutralIdentified, YesNoUnknown.UNKNOWN),
    groundingVisible = enumValue(groundingVisible, YesNoUnknown.UNKNOWN),
    protectionCompatibility = enumValue(protectionCompatibility, ProtectionCompatibility.NOT_ASSESSED),
    protectionCompatibilityNotes = protectionCompatibilityNotes,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun PillarInspection.toEntity(): PillarInspectionEntity = PillarInspectionEntity(
    inspectionId = inspectionId,
    reviewStatus = reviewStatus.name,
    exists = exists,
    propertyType = propertyType?.name,
    propertyTypeOther = propertyTypeOther,
    supplyType = supplyType?.name,
    accessible = accessible.name,
    generalCondition = generalCondition.name,
    mainBreakerPresent = mainBreakerPresent.name,
    mainBreakerAmps = mainBreakerAmps,
    mainBreakerOtherAmps = mainBreakerOtherAmps,
    differentialPresent = differentialPresent.name,
    differentialRatedAmps = differentialRatedAmps,
    differentialOtherRatedAmps = differentialOtherRatedAmps,
    differentialSensitivityMa = differentialSensitivityMa,
    differentialOtherSensitivityMa = differentialOtherSensitivityMa,
    differentialTestResult = differentialTestResult.name,
    conductorSectionMm2 = conductorSectionMm2,
    conductorOtherSectionMm2 = conductorOtherSectionMm2,
    conductorMaterial = conductorMaterial.name,
    conductorMaterialOther = conductorMaterialOther,
    conductorCondition = conductorCondition.name,
    neutralIdentified = neutralIdentified.name,
    groundingVisible = groundingVisible.name,
    protectionCompatibility = protectionCompatibility.name,
    protectionCompatibilityNotes = protectionCompatibilityNotes,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

fun PillarMeasurementEntity.toDomain(): PillarMeasurement = PillarMeasurement(
    id = id,
    inspectionId = inspectionId,
    type = enumValue(type, PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN),
    value = value,
    unit = unit,
    origin = enumValue(origin, MeasurementOrigin.MEASURED),
    sortOrder = sortOrder,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun PillarMeasurement.toEntity(): PillarMeasurementEntity = PillarMeasurementEntity(
    id = id,
    inspectionId = inspectionId,
    type = type.name,
    value = value,
    unit = unit,
    origin = origin.name,
    sortOrder = sortOrder,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun MainPanelInspectionEntity.toDomain(): MainPanelInspection = MainPanelInspection(
    inspectionId = inspectionId,
    reviewStatus = enumValue(reviewStatus, InspectionSectionReviewStatus.REVIEWED),
    accessible = enumValue(accessible, AccessStatus.NO),
    generalCondition = enumValue(generalCondition, GeneralCondition.NOT_ASSESSED),
    differentialPresent = enumValue(differentialPresent, YesNoUnknown.UNKNOWN),
    differentialRatedAmps = differentialRatedAmps,
    differentialOtherRatedAmps = differentialOtherRatedAmps,
    differentialSensitivityMa = differentialSensitivityMa,
    differentialOtherSensitivityMa = differentialOtherSensitivityMa,
    differentialTestResult = enumValue(differentialTestResult, DifferentialTestResult.NOT_TESTED),
    circuitCount = circuitCount,
    circuitsIdentified = enumValue(circuitsIdentified, YesNoPartialUnknown.UNKNOWN),
    neutralBarPresent = enumValue(neutralBarPresent, YesNoUnknown.UNKNOWN),
    groundBarPresent = enumValue(groundBarPresent, YesNoUnknown.UNKNOWN),
    neutralAndGroundSeparated = enumValue(neutralAndGroundSeparated, YesNoUnknown.UNKNOWN),
    protectionConductorsPresent = enumValue(protectionConductorsPresent, YesNoPartialUnknown.UNKNOWN),
    improvisedConnections = enumValue(improvisedConnections, YesNoUnknown.UNKNOWN),
    conductorColorStatus = enumValue(conductorColorStatus, ConductorColorStatus.UNKNOWN),
    mixedOrIncorrectColors = enumValue(mixedOrIncorrectColors, YesNoUnknown.UNKNOWN),
    overheatingSigns = enumValue(overheatingSigns, YesNoUnknown.UNKNOWN),
    exposedPartsOrDamagedInsulation = enumValue(exposedPartsOrDamagedInsulation, YesNoUnknown.UNKNOWN),
    protectionCompatibility = enumValue(protectionCompatibility, ProtectionCompatibility.NOT_ASSESSED),
    wiringRisksNotes = wiringRisksNotes,
    protectionConductorCheckResult = enumValue(protectionConductorCheckResult, ProtectionConductorCheckResult.NOT_VERIFIED),
    feederDistanceMeters = feederDistanceMeters,
    feederConductorSectionMm2 = feederConductorSectionMm2,
    feederConductorMaterial = enumValue(feederConductorMaterial, ConductorMaterial.UNKNOWN),
    feederDataOrigin = enumValue(feederDataOrigin, MeasurementOrigin.NOT_VERIFIED),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun MainPanelInspection.toEntity(): MainPanelInspectionEntity = MainPanelInspectionEntity(
    inspectionId = inspectionId,
    reviewStatus = reviewStatus.name,
    accessible = accessible.name,
    generalCondition = generalCondition.name,
    differentialPresent = differentialPresent.name,
    differentialRatedAmps = differentialRatedAmps,
    differentialOtherRatedAmps = differentialOtherRatedAmps,
    differentialSensitivityMa = differentialSensitivityMa,
    differentialOtherSensitivityMa = differentialOtherSensitivityMa,
    differentialTestResult = differentialTestResult.name,
    circuitCount = circuitCount,
    circuitsIdentified = circuitsIdentified.name,
    neutralBarPresent = neutralBarPresent.name,
    groundBarPresent = groundBarPresent.name,
    neutralAndGroundSeparated = neutralAndGroundSeparated.name,
    protectionConductorsPresent = protectionConductorsPresent.name,
    improvisedConnections = improvisedConnections.name,
    conductorColorStatus = conductorColorStatus.name,
    mixedOrIncorrectColors = mixedOrIncorrectColors.name,
    overheatingSigns = overheatingSigns.name,
    exposedPartsOrDamagedInsulation = exposedPartsOrDamagedInsulation.name,
    protectionCompatibility = protectionCompatibility.name,
    wiringRisksNotes = wiringRisksNotes,
    protectionConductorCheckResult = protectionConductorCheckResult.name,
    feederDistanceMeters = feederDistanceMeters,
    feederConductorSectionMm2 = feederConductorSectionMm2,
    feederConductorMaterial = feederConductorMaterial.name,
    feederDataOrigin = feederDataOrigin.name,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

fun MainPanelMeasurementEntity.toDomain(): MainPanelMeasurement = MainPanelMeasurement(
    id = id,
    inspectionId = inspectionId,
    section = enumValue(section, MainPanelMeasurementSection.INPUT_VOLTAGE),
    type = enumValue(type, MainPanelMeasurementType.INPUT_VOLTAGE_LN),
    value = value,
    unit = unit,
    origin = enumValue(origin, MeasurementOrigin.MEASURED),
    sortOrder = sortOrder,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun MainPanelMeasurement.toEntity(): MainPanelMeasurementEntity = MainPanelMeasurementEntity(
    id = id,
    inspectionId = inspectionId,
    section = section.name,
    type = type.name,
    value = value,
    unit = unit,
    origin = origin.name,
    sortOrder = sortOrder,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun MainPanelCircuitEntity.toDomain(): MainPanelCircuit = MainPanelCircuit(
    id = id,
    inspectionId = inspectionId,
    sortOrder = sortOrder,
    destination = enumValue(destination, CircuitDestination.UNIDENTIFIED),
    destinationOther = destinationOther,
    breakerAmps = breakerAmps,
    breakerOtherAmps = breakerOtherAmps,
    breakerCurve = enumValue(breakerCurve, BreakerCurve.UNKNOWN),
    conductorSectionMm2 = conductorSectionMm2,
    conductorOtherSectionMm2 = conductorOtherSectionMm2,
    conductorMaterial = enumValue(conductorMaterial, ConductorMaterial.UNKNOWN),
    conductorMaterialOther = conductorMaterialOther,
    consumptionAmps = consumptionAmps,
    consumptionOrigin = enumValue(consumptionOrigin, MeasurementOrigin.NOT_VERIFIED),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun MainPanelCircuit.toEntity(): MainPanelCircuitEntity = MainPanelCircuitEntity(
    id = id,
    inspectionId = inspectionId,
    sortOrder = sortOrder,
    destination = destination.name,
    destinationOther = destinationOther,
    breakerAmps = breakerAmps,
    breakerOtherAmps = breakerOtherAmps,
    breakerCurve = breakerCurve.name,
    conductorSectionMm2 = conductorSectionMm2,
    conductorOtherSectionMm2 = conductorOtherSectionMm2,
    conductorMaterial = conductorMaterial.name,
    conductorMaterialOther = conductorMaterialOther,
    consumptionAmps = consumptionAmps,
    consumptionOrigin = consumptionOrigin.name,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun InspectionFindingEntity.toDomain(): InspectionFinding = InspectionFinding(
    id = id,
    inspectionId = inspectionId,
    category = enumValue(category, FindingCategory.OTHER),
    severity = enumValue(severity, FindingSeverity.RECOMMENDED),
    title = title,
    description = description,
    recommendation = recommendation,
    sourceType = enumValue(sourceType, FindingSourceType.MANUAL),
    sourceSection = sourceSection?.let { enumValue(it, InspectionSection.FINDINGS) },
    sourceEntityId = sourceEntityId,
    sourceValue = sourceValue,
    sourceUnit = sourceUnit,
    ruleCode = ruleCode,
    reviewStatus = enumValue(reviewStatus, FindingReviewStatus.CONFIRMED),
    includeInReport = includeInReport,
    technicianNotes = technicianNotes,
    sortOrder = sortOrder,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun InspectionFinding.toEntity(): InspectionFindingEntity = InspectionFindingEntity(
    id = id,
    inspectionId = inspectionId,
    category = category.name,
    severity = severity.name,
    title = title,
    description = description,
    recommendation = recommendation,
    sourceType = sourceType.name,
    sourceSection = sourceSection?.name,
    sourceEntityId = sourceEntityId,
    sourceValue = sourceValue,
    sourceUnit = sourceUnit,
    ruleCode = ruleCode,
    reviewStatus = reviewStatus.name,
    includeInReport = includeInReport,
    technicianNotes = technicianNotes,
    sortOrder = sortOrder,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun InspectionUnverifiedItemEntity.toDomain(): InspectionUnverifiedItem = InspectionUnverifiedItem(
    id = id,
    inspectionId = inspectionId,
    type = enumValue(type, UnverifiedItemType.OTHER),
    description = description,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun InspectionUnverifiedItem.toEntity(): InspectionUnverifiedItemEntity = InspectionUnverifiedItemEntity(
    id = id,
    inspectionId = inspectionId,
    type = type.name,
    description = description,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun InspectionListItemEntity.toDomain(): InspectionListItem {
    val inspection = inspection.toDomain()
    return InspectionListItem(
        inspection = inspection,
        visitScheduledAt = visitScheduledAt?.let(Instant::ofEpochMilli),
        progress = InspectionProgress(
            sections = listOf(
                InspectionSectionProgress(
                    InspectionSection.GENERAL,
                    InspectionSectionStatus.COMPLETE,
                    inspection.clientNameSnapshot,
                ),
                InspectionSectionProgress(
                    InspectionSection.PILLAR,
                    sectionStatus(
                        valuePresent = pillarCondition != null,
                        complete = pillarExists == false ||
                            enumValue(pillarCondition.orEmpty(), GeneralCondition.NOT_ASSESSED) != GeneralCondition.NOT_ASSESSED,
                    ),
                    pillarCondition.orEmpty(),
                ),
                InspectionSectionProgress(
                    InspectionSection.MAIN_PANEL,
                    sectionStatus(
                        valuePresent = mainPanelCondition != null,
                        complete = mainPanelAccessible == AccessStatus.NO.name ||
                            enumValue(mainPanelCondition.orEmpty(), GeneralCondition.NOT_ASSESSED) != GeneralCondition.NOT_ASSESSED,
                    ),
                    mainPanelCondition.orEmpty(),
                ),
                InspectionSectionProgress(
                    InspectionSection.FINDINGS,
                    if (findingCount > 0) InspectionSectionStatus.COMPLETE else InspectionSectionStatus.NOT_STARTED,
                    if (findingCount > 0) "$findingCount hallazgo(s)" else "",
                ),
                InspectionSectionProgress(
                    InspectionSection.TECHNICAL_COMMENT,
                    if (inspection.originalTechnicalComment.isNullOrBlank()) {
                        InspectionSectionStatus.NOT_STARTED
                    } else {
                        InspectionSectionStatus.COMPLETE
                    },
                    "",
                ),
                InspectionSectionProgress(
                    InspectionSection.FINAL_REPORT,
                    if (inspection.finalClientReport.isNullOrBlank()) {
                        InspectionSectionStatus.NOT_STARTED
                    } else {
                        InspectionSectionStatus.COMPLETE
                    },
                    "",
                ),
            ),
        ),
    )
}

private fun sectionStatus(valuePresent: Boolean, complete: Boolean): InspectionSectionStatus {
    return when {
        !valuePresent -> InspectionSectionStatus.NOT_STARTED
        complete -> InspectionSectionStatus.COMPLETE
        else -> InspectionSectionStatus.INCOMPLETE
    }
}

private inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T {
    if (value == null) return fallback
    return runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
