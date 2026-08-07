package com.matiasdev.elecapp.features.inspections.domain

data class InspectionCompletionResult(
    val canComplete: Boolean,
    val missingItems: List<String>,
)

object InspectionCompletionRules {
    fun validate(aggregate: InspectionAggregate, hasCalculations: Boolean = false): InspectionCompletionResult {
        if (aggregate.inspection.scope == InspectionScope.VISUAL_INSPECTION) {
            return validateVisualInspection(aggregate, hasCalculations)
        }
        val missing = buildList {
            val inspection = aggregate.inspection
            if (inspection.clientNameSnapshot.isBlank() || inspection.visitReasonSnapshot.isBlank()) {
                add("Datos generales")
            }
            val pillarIsRequired = inspection.scope != InspectionScope.SECTOR_ASSESSMENT ||
                InspectionProgressCalculator.run { inspection.isPillarRelevantForSector() }
            val pillar = aggregate.pillar
            if (pillarIsRequired && (pillar == null || (pillar.reviewStatus == InspectionSectionReviewStatus.REVIEWED && pillar.generalCondition == GeneralCondition.NOT_ASSESSED))) {
                add("Estado de pilar o marcado como no evaluado")
            }
            val mainPanelIsRequired = inspection.scope != InspectionScope.SECTOR_ASSESSMENT ||
                InspectionProgressCalculator.run { inspection.isMainPanelRelevantForSector() }
            val panel = aggregate.mainPanel
            if (mainPanelIsRequired && (panel == null || (panel.accessible != AccessStatus.NO && panel.generalCondition == GeneralCondition.NOT_ASSESSED))) {
                add("Estado de tablero o marcado como no evaluado")
            }
            if (inspection.originalTechnicalComment.isNullOrBlank() && aggregate.findings.isEmpty()) {
                add("Comentario técnico o al menos un hallazgo")
            }
        }
        return InspectionCompletionResult(canComplete = missing.isEmpty(), missingItems = missing)
    }

    private fun validateVisualInspection(
        aggregate: InspectionAggregate,
        hasCalculations: Boolean,
    ): InspectionCompletionResult {
        val inspection = aggregate.inspection
        val hasMinimumContent = listOf(
            inspection.reviewReason,
            inspection.reviewedElement,
            inspection.taskDescription,
            inspection.visitReasonSnapshot,
            inspection.originalTechnicalComment,
        ).any { !it.isNullOrBlank() } ||
            aggregate.findings.isNotEmpty() ||
            aggregate.unverifiedItems.isNotEmpty() ||
            hasCalculations
        val missing = if (hasMinimumContent) {
            emptyList()
        } else {
            listOf("Motivo, sector, descripción, hallazgo, observación o medición")
        }
        return InspectionCompletionResult(canComplete = missing.isEmpty(), missingItems = missing)
    }
}
