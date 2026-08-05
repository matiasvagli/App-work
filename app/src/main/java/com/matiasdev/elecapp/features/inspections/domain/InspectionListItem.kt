package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class InspectionListItem(
    val inspection: ElectricalInspection,
    val visitScheduledAt: Instant?,
    val progress: InspectionProgress,
)
