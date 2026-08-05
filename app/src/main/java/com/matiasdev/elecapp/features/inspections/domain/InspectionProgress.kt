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

    private fun generalProgress(inspection: ElectricalInspection): InspectionSectionProgress {
        val complete = inspection.visitReasonSnapshot.isNotBlank() && inspection.clientNameSnapshot.isNotBlank()
        val summary = listOf(
            inspection.inspectionType.name,
            inspection.supplyType.name.takeUnless { inspection.supplyType == SupplyType.UNKNOWN },
            inspection.propertyType.name,
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
            pillar.generalCondition != GeneralCondition.NOT_ASSESSED || pillar.exists == false ->
                InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.COMPLETE, pillar.generalCondition.name)
            else -> InspectionSectionProgress(InspectionSection.PILLAR, InspectionSectionStatus.INCOMPLETE, "Sin estado observado")
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
}
