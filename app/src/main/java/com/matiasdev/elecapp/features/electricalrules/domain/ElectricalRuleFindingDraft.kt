package com.matiasdev.elecapp.features.electricalrules.domain

data class ElectricalRuleFindingDraft(
    val ruleCode: ElectricalRuleCode,
    val severity: ElectricalRuleSeverity,
    val title: String,
    val description: String,
    val recommendation: String?,
    val sourceCalculationId: String?,
    val inspectionId: String?,
)
