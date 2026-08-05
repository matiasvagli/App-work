package com.matiasdev.elecapp.features.inspections.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class InspectionListItemEntity(
    @Embedded val inspection: ElectricalInspectionEntity,
    @ColumnInfo(name = "visit_scheduled_at") val visitScheduledAt: Long?,
    @ColumnInfo(name = "pillar_exists") val pillarExists: Boolean?,
    @ColumnInfo(name = "pillar_condition") val pillarCondition: String?,
    @ColumnInfo(name = "main_panel_accessible") val mainPanelAccessible: String?,
    @ColumnInfo(name = "main_panel_condition") val mainPanelCondition: String?,
    @ColumnInfo(name = "finding_count") val findingCount: Int,
    @ColumnInfo(name = "unverified_count") val unverifiedCount: Int,
)
