package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation

data class InspectionAggregate(
    val inspection: ElectricalInspection,
    val pillar: PillarInspection?,
    val mainPanel: MainPanelInspection?,
    val findings: List<InspectionFinding>,
    val unverifiedItems: List<InspectionUnverifiedItem>,
    val calculations: List<TechnicalCalculation> = emptyList(),
)
