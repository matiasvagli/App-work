package com.matiasdev.elecapp.features.inspections.domain

data class InspectionAggregate(
    val inspection: ElectricalInspection,
    val pillar: PillarInspection?,
    val mainPanel: MainPanelInspection?,
    val findings: List<InspectionFinding>,
    val unverifiedItems: List<InspectionUnverifiedItem>,
)
