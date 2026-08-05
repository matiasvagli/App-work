package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgress
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionProgress
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import java.time.Instant

fun ElectricalInspectionEntity.toDomain(): ElectricalInspection = ElectricalInspection(
    id = id,
    visitId = visitId,
    status = enumValue(status, InspectionStatus.DRAFT),
    inspectionType = enumValue(inspectionType, InspectionType.VISUAL),
    generalCondition = enumValue(generalCondition, GeneralCondition.NOT_ASSESSED),
    supplyType = enumValue(supplyType, SupplyType.UNKNOWN),
    propertyType = enumValue(propertyType, PropertyType.HOUSE),
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
    inspectionType = inspectionType.name,
    generalCondition = generalCondition.name,
    supplyType = supplyType.name,
    propertyType = propertyType.name,
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
    exists = exists,
    accessible = enumValue(accessible, AccessStatus.NO),
    generalCondition = enumValue(generalCondition, GeneralCondition.NOT_ASSESSED),
    mainBreakerPresent = enumValue(mainBreakerPresent, YesNoUnknown.UNKNOWN),
    mainBreakerAmps = mainBreakerAmps,
    conductorSectionMm2 = conductorSectionMm2,
    conductorMaterial = enumValue(conductorMaterial, ConductorMaterial.UNKNOWN),
    conductorCondition = enumValue(conductorCondition, ConductorCondition.NOT_ASSESSED),
    neutralIdentified = enumValue(neutralIdentified, YesNoUnknown.UNKNOWN),
    groundingVisible = enumValue(groundingVisible, YesNoUnknown.UNKNOWN),
    protectionCompatibility = enumValue(protectionCompatibility, ProtectionCompatibility.NOT_ASSESSED),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun PillarInspection.toEntity(): PillarInspectionEntity = PillarInspectionEntity(
    inspectionId = inspectionId,
    exists = exists,
    accessible = accessible.name,
    generalCondition = generalCondition.name,
    mainBreakerPresent = mainBreakerPresent.name,
    mainBreakerAmps = mainBreakerAmps,
    conductorSectionMm2 = conductorSectionMm2,
    conductorMaterial = conductorMaterial.name,
    conductorCondition = conductorCondition.name,
    neutralIdentified = neutralIdentified.name,
    groundingVisible = groundingVisible.name,
    protectionCompatibility = protectionCompatibility.name,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

fun MainPanelInspectionEntity.toDomain(): MainPanelInspection = MainPanelInspection(
    inspectionId = inspectionId,
    accessible = enumValue(accessible, AccessStatus.NO),
    generalCondition = enumValue(generalCondition, GeneralCondition.NOT_ASSESSED),
    differentialPresent = enumValue(differentialPresent, YesNoUnknown.UNKNOWN),
    differentialRatedAmps = differentialRatedAmps,
    differentialSensitivityMa = differentialSensitivityMa,
    differentialTestResult = enumValue(differentialTestResult, DifferentialTestResult.NOT_TESTED),
    circuitCount = circuitCount,
    circuitsIdentified = enumValue(circuitsIdentified, YesNoPartialUnknown.UNKNOWN),
    neutralBarPresent = enumValue(neutralBarPresent, YesNoUnknown.UNKNOWN),
    groundBarPresent = enumValue(groundBarPresent, YesNoUnknown.UNKNOWN),
    neutralAndGroundSeparated = enumValue(neutralAndGroundSeparated, YesNoUnknown.UNKNOWN),
    improvisedConnections = enumValue(improvisedConnections, YesNoUnknown.UNKNOWN),
    mixedOrIncorrectColors = enumValue(mixedOrIncorrectColors, YesNoUnknown.UNKNOWN),
    overheatingSigns = enumValue(overheatingSigns, YesNoUnknown.UNKNOWN),
    protectionCompatibility = enumValue(protectionCompatibility, ProtectionCompatibility.NOT_ASSESSED),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun MainPanelInspection.toEntity(): MainPanelInspectionEntity = MainPanelInspectionEntity(
    inspectionId = inspectionId,
    accessible = accessible.name,
    generalCondition = generalCondition.name,
    differentialPresent = differentialPresent.name,
    differentialRatedAmps = differentialRatedAmps,
    differentialSensitivityMa = differentialSensitivityMa,
    differentialTestResult = differentialTestResult.name,
    circuitCount = circuitCount,
    circuitsIdentified = circuitsIdentified.name,
    neutralBarPresent = neutralBarPresent.name,
    groundBarPresent = groundBarPresent.name,
    neutralAndGroundSeparated = neutralAndGroundSeparated.name,
    improvisedConnections = improvisedConnections.name,
    mixedOrIncorrectColors = mixedOrIncorrectColors.name,
    overheatingSigns = overheatingSigns.name,
    protectionCompatibility = protectionCompatibility.name,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

fun InspectionFindingEntity.toDomain(): InspectionFinding = InspectionFinding(
    id = id,
    inspectionId = inspectionId,
    category = enumValue(category, FindingCategory.OTHER),
    severity = enumValue(severity, FindingSeverity.RECOMMENDED),
    title = title,
    description = description,
    recommendation = recommendation,
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
                    InspectionSection.UNVERIFIED,
                    if (unverifiedCount > 0) InspectionSectionStatus.COMPLETE else InspectionSectionStatus.NOT_STARTED,
                    if (unverifiedCount > 0) "$unverifiedCount elemento(s)" else "",
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

private inline fun <reified T : Enum<T>> enumValue(value: String, fallback: T): T {
    return runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
