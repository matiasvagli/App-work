package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig

enum class InspectionSectionStatus {
    NOT_STARTED,
    INCOMPLETE,
    COMPLETE,
}

enum class InspectionSection {
    GENERAL,
    PILLAR,
    MAIN_PANEL,
    GROUNDING,
    FINDINGS,
    UNVERIFIED,
    VISUAL_COMPLEMENTARY,
    TECHNICAL_COMMENT,
    FINAL_REPORT,
}

data class InspectionSectionProgress(
    val section: InspectionSection,
    val status: InspectionSectionStatus,
    val summary: String,
)

data class InspectionProgress(
    val sections: List<InspectionSectionProgress>,
) {
    val completedCount: Int = sections.count { it.status == InspectionSectionStatus.COMPLETE }
    val totalCount: Int = sections.size
    val percent: Int = if (totalCount == 0) 0 else (completedCount * 100) / totalCount
}

object InspectionProgressCalculator {
    fun calculate(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): InspectionProgress {
        val inspection = aggregate.inspection
        if (inspection.scope == InspectionScope.VISUAL_INSPECTION) {
            return InspectionProgress(
                sections = listOf(
                    visualPillarProgress(aggregate.pillar),
                    visualMainPanelProgress(aggregate.mainPanel),
                    groundingProgress(aggregate.grounding),
                    findingsProgress(aggregate, inspection.findingsReviewedAt, rules),
                    visualComplementaryProgress(inspection, aggregate.unverifiedItems),
                ),
            )
        }
        if (inspection.scope == InspectionScope.SECTOR_ASSESSMENT) {
            return InspectionProgress(
                sections = buildList {
                    if (inspection.isPillarRelevantForSector()) add(pillarProgress(aggregate.pillar))
                    if (inspection.isMainPanelRelevantForSector()) add(mainPanelProgress(aggregate.mainPanel))
                    add(groundingProgress(aggregate.grounding))
                    add(findingsProgress(aggregate, inspection.findingsReviewedAt, rules))
                    add(technicalCommentProgress(inspection))
                },
            )
        }
        return InspectionProgress(
            sections = listOf(
                pillarProgress(aggregate.pillar),
                mainPanelProgress(aggregate.mainPanel),
                groundingProgress(aggregate.grounding),
                findingsProgress(aggregate, inspection.findingsReviewedAt, rules),
                technicalCommentProgress(inspection),
            ),
        )
    }

    private fun groundingProgress(grounding: GroundingInspection?): InspectionSectionProgress = when {
        grounding == null -> InspectionSectionProgress(InspectionSection.GROUNDING, InspectionSectionStatus.NOT_STARTED, "")
        grounding.resistanceOrigin == MeasurementOrigin.NOT_VERIFIED ->
            InspectionSectionProgress(InspectionSection.GROUNDING, InspectionSectionStatus.COMPLETE, "Inspección visual · resistencia no verificada")
        grounding.resistanceOhms != null ->
            InspectionSectionProgress(InspectionSection.GROUNDING, InspectionSectionStatus.COMPLETE, "${grounding.resistanceOhms.formatOhms()} Ω")
        else -> InspectionSectionProgress(InspectionSection.GROUNDING, InspectionSectionStatus.INCOMPLETE, "Falta el valor de resistencia")
    }

    fun ElectricalInspection.isPillarRelevantForSector(): Boolean {
        val text = listOfNotNull(reviewedElement, taskDescription, reviewReason, visitReasonSnapshot)
            .joinToString(" ")
            .lowercase()
        return listOf("pilar", "acometida", "suministro", "medidor", "entrada").any { it in text }
    }

    fun ElectricalInspection.isMainPanelRelevantForSector(): Boolean {
        val text = listOfNotNull(reviewedElement, taskDescription, reviewReason, visitReasonSnapshot)
            .joinToString(" ")
            .lowercase()
        return listOf("tablero", "termica", "térmica", "diferencial", "circuito", "proteccion", "protección").any { it in text }
    }

    private fun generalProgress(inspection: ElectricalInspection): InspectionSectionProgress {
        val complete = if (inspection.scope == InspectionScope.VISUAL_INSPECTION) {
            inspection.clientNameSnapshot.isNotBlank() &&
                listOf(
                    inspection.reviewReason,
                    inspection.reviewedElement,
                    inspection.taskDescription,
                    inspection.visitReasonSnapshot,
                ).any { !it.isNullOrBlank() }
        } else {
            inspection.visitReasonSnapshot.isNotBlank() && inspection.clientNameSnapshot.isNotBlank()
        }
        val summary = listOf(
            inspection.scope.name,
            inspection.inspectionType.name,
            inspection.supplyType.name.takeUnless { inspection.supplyType == SupplyType.UNKNOWN },
            inspection.propertyType.name.takeUnless { inspection.propertyType == PropertyType.UNKNOWN },
        ).filterNotNull().joinToString(" · ")
        return InspectionSectionProgress(
            InspectionSection.GENERAL,
            if (complete) InspectionSectionStatus.COMPLETE else InspectionSectionStatus.INCOMPLETE,
            summary,
        )
    }

    private fun pillarProgress(pillar: PillarInspection?): InspectionSectionProgress {
        return when {
            pillar == null -> InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.NOT_STARTED, "")
            pillar.reviewStatus != InspectionSectionReviewStatus.REVIEWED ->
                InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.COMPLETE, pillar.reviewStatus.summary())
            pillar.generalCondition != GeneralCondition.NOT_ASSESSED || pillar.exists == false ->
                InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.COMPLETE, pillar.generalCondition.name)
            else -> InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.INCOMPLETE, "Sin estado observado")
        }
    }

    private fun visualPillarProgress(pillar: PillarInspection?): InspectionSectionProgress {
        return when {
            pillar == null -> InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.COMPLETE, "Opcional")
            pillar.reviewStatus != InspectionSectionReviewStatus.REVIEWED ->
                InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.COMPLETE, pillar.reviewStatus.summary())
            else -> InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.COMPLETE, "Revisado")
        }
    }

    private fun mainPanelProgress(panel: MainPanelInspection?): InspectionSectionProgress {
        return when {
            panel == null -> InspectionSectionProgress(InspectionSection.MAIN_PANEL, InspectionSectionStatus.NOT_STARTED, "")
            panel.generalCondition != GeneralCondition.NOT_ASSESSED || panel.accessible == AccessStatus.NO ->
                InspectionSectionProgress(InspectionSection.MAIN_PANEL, InspectionSectionStatus.COMPLETE, panel.generalCondition.name)
            else -> InspectionSectionProgress(InspectionSection.MAIN_PANEL, InspectionSectionStatus.INCOMPLETE, "Sin estado observado")
        }
    }

    private fun visualMainPanelProgress(panel: MainPanelInspection?): InspectionSectionProgress {
        return when {
            panel == null -> InspectionSectionProgress(InspectionSection.MAIN_PANEL, InspectionSectionStatus.COMPLETE, "Opcional")
            panel.reviewStatus != InspectionSectionReviewStatus.REVIEWED ->
                InspectionSectionProgress(InspectionSection.MAIN_PANEL, InspectionSectionStatus.COMPLETE, panel.reviewStatus.summary())
            else -> InspectionSectionProgress(InspectionSection.MAIN_PANEL, InspectionSectionStatus.COMPLETE, "Revisado")
        }
    }

    /**
     * No encontrar hallazgos es un resultado válido de una inspección, distinto de no
     * haber mirado la sección. Por eso la completitud mira si el técnico la revisó, no
     * si la lista tiene elementos.
     *
     * Cuenta los mismos hallazgos que va a mostrar la pantalla de hallazgos y que va a
     * salir en el informe: los automáticos se derivan del aggregate en cada lectura y no
     * están guardados en Room, así que mirar `aggregate.findings` crudo hacía que el
     * overview dijera "Revisado, sin hallazgos" con la pantalla de hallazgos llena.
     */
    private fun findingsProgress(
        aggregate: InspectionAggregate,
        reviewedAt: java.time.Instant?,
        rules: List<ElectricalRuleConfig>,
    ): InspectionSectionProgress {
        val findings = InspectionFindingProposalBuilder.mergeIntoAggregate(aggregate, rules).findings.filter {
            it.includeInReport &&
                it.sourceType != FindingSourceType.NOT_VERIFIED &&
                it.sourceType != FindingSourceType.DATA_REVIEW
        }
        val status = when {
            findings.isNotEmpty() -> InspectionSectionStatus.COMPLETE
            reviewedAt != null -> InspectionSectionStatus.COMPLETE
            else -> InspectionSectionStatus.NOT_STARTED
        }
        val summary = when {
            findings.isNotEmpty() -> "${findings.size} hallazgo(s)"
            reviewedAt != null -> "Revisado, sin hallazgos"
            else -> ""
        }
        return InspectionSectionProgress(InspectionSection.FINDINGS, status, summary)
    }

    private fun visualComplementaryProgress(
        inspection: ElectricalInspection,
        items: List<InspectionUnverifiedItem>,
    ): InspectionSectionProgress {
        val complete = !inspection.originalTechnicalComment.isNullOrBlank() || items.isNotEmpty()
        val summary = buildList {
            inspection.originalTechnicalComment?.takeIf(String::isNotBlank)?.let { add(it.take(60)) }
            if (items.isNotEmpty()) add("${items.size} elemento(s)")
        }.joinToString(" · ")
        return InspectionSectionProgress(
            InspectionSection.VISUAL_COMPLEMENTARY,
            if (complete) InspectionSectionStatus.COMPLETE else InspectionSectionStatus.NOT_STARTED,
            summary,
        )
    }

    private fun technicalCommentProgress(inspection: ElectricalInspection): InspectionSectionProgress {
        return InspectionSectionProgress(
            InspectionSection.TECHNICAL_COMMENT,
            if (inspection.originalTechnicalComment.isNullOrBlank()) InspectionSectionStatus.NOT_STARTED else InspectionSectionStatus.COMPLETE,
            inspection.originalTechnicalComment.orEmpty().take(80),
        )
    }

    private fun finalReportProgress(inspection: ElectricalInspection): InspectionSectionProgress {
        return InspectionSectionProgress(
            InspectionSection.FINAL_REPORT,
            if (inspection.finalClientReport.isNullOrBlank()) InspectionSectionStatus.NOT_STARTED else InspectionSectionStatus.COMPLETE,
            inspection.finalClientReport.orEmpty().take(80),
        )
    }

    private fun InspectionSectionReviewStatus.summary(): String = when (this) {
        InspectionSectionReviewStatus.REVIEWED -> "Revisado"
        InspectionSectionReviewStatus.NOT_APPLICABLE -> "No corresponde"
        InspectionSectionReviewStatus.NOT_VERIFIED -> "No se verificó"
    }
}

private fun Double.formatOhms(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString().replace('.', ',')
}
