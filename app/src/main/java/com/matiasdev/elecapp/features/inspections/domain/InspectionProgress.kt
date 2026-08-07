package com.matiasdev.elecapp.features.inspections.domain

enum class InspectionSectionStatus {
    NOT_STARTED,
    INCOMPLETE,
    COMPLETE,
}

enum class InspectionSection {
    GENERAL,
    PILLAR,
    MAIN_PANEL,
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
    fun calculate(aggregate: InspectionAggregate): InspectionProgress {
        val inspection = aggregate.inspection
        if (inspection.scope == InspectionScope.VISUAL_INSPECTION) {
            return InspectionProgress(
                sections = listOf(
                    generalProgress(inspection),
                    visualPillarProgress(aggregate.pillar),
                    visualMainPanelProgress(aggregate.mainPanel),
                    findingsProgress(aggregate.findings),
                    visualComplementaryProgress(inspection, aggregate.unverifiedItems),
                    finalReportProgress(inspection),
                ),
            )
        }
        if (inspection.scope == InspectionScope.SECTOR_ASSESSMENT) {
            return InspectionProgress(
                sections = buildList {
                    add(generalProgress(inspection))
                    if (inspection.isPillarRelevantForSector()) add(pillarProgress(aggregate.pillar))
                    if (inspection.isMainPanelRelevantForSector()) add(mainPanelProgress(aggregate.mainPanel))
                    add(findingsProgress(aggregate.findings))
                    add(unverifiedProgress(aggregate.unverifiedItems))
                    add(technicalCommentProgress(inspection))
                    add(finalReportProgress(inspection))
                },
            )
        }
        return InspectionProgress(
            sections = listOf(
                generalProgress(inspection),
                pillarProgress(aggregate.pillar),
                mainPanelProgress(aggregate.mainPanel),
                findingsProgress(aggregate.findings),
                unverifiedProgress(aggregate.unverifiedItems),
                technicalCommentProgress(inspection),
                finalReportProgress(inspection),
            ),
        )
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

    private fun findingsProgress(findings: List<InspectionFinding>): InspectionSectionProgress {
        return InspectionSectionProgress(
            InspectionSection.FINDINGS,
            if (findings.isEmpty()) InspectionSectionStatus.NOT_STARTED else InspectionSectionStatus.COMPLETE,
            if (findings.isEmpty()) "" else "${findings.size} hallazgo(s)",
        )
    }

    private fun unverifiedProgress(items: List<InspectionUnverifiedItem>): InspectionSectionProgress {
        return InspectionSectionProgress(
            InspectionSection.UNVERIFIED,
            if (items.isEmpty()) InspectionSectionStatus.NOT_STARTED else InspectionSectionStatus.COMPLETE,
            if (items.isEmpty()) "" else "${items.size} elemento(s)",
        )
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
