package com.matiasdev.elecapp.features.electricalrules.domain

data class ElectricalRuleEvaluation(
    val ruleCode: ElectricalRuleCode,
    val status: ElectricalRuleEvaluationStatus,
    val severity: ElectricalRuleSeverity,
    val measuredValue: Double?,
    val limitValue: Double?,
    val unit: String?,
    val finding: ElectricalRuleFindingDraft?,
    val explanation: String?,
)
