package com.matiasdev.elecapp.features.inspections.domain

data class InspectionCompletionResult(
    val canComplete: Boolean,
    val missingItems: List<String>,
)

object InspectionCompletionRules {
    fun validate(aggregate: InspectionAggregate): InspectionCompletionResult {
        val missing = buildList {
            val inspection = aggregate.inspection
            if (inspection.clientNameSnapshot.isBlank() || inspection.visitReasonSnapshot.isBlank()) {
                add("Datos generales")
            }
            val pillar = aggregate.pillar
            if (pillar == null || (pillar.exists != false && pillar.generalCondition == GeneralCondition.NOT_ASSESSED)) {
                add("Estado de pilar o marcado como no evaluado")
            }
            val panel = aggregate.mainPanel
            if (panel == null || (panel.accessible != AccessStatus.NO && panel.generalCondition == GeneralCondition.NOT_ASSESSED)) {
                add("Estado de tablero o marcado como no evaluado")
            }
            if (inspection.originalTechnicalComment.isNullOrBlank() && aggregate.findings.isEmpty()) {
                add("Comentario técnico o al menos un hallazgo")
            }
        }
        return InspectionCompletionResult(canComplete = missing.isEmpty(), missingItems = missing)
    }
}
