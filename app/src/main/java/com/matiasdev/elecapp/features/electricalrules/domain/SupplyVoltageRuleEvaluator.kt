package com.matiasdev.elecapp.features.electricalrules.domain

class SupplyVoltageRuleEvaluator {
    fun evaluateMinimum(
        input: SupplyVoltageInput,
        config: ElectricalRuleConfig?,
    ): ElectricalRuleEvaluation {
        return evaluate(
            input = input,
            config = config,
            ruleCode = ElectricalRuleCode.MIN_SUPPLY_VOLTAGE,
            fails = { measured, limit -> measured < limit },
        )
    }

    fun evaluateMaximum(
        input: SupplyVoltageInput,
        config: ElectricalRuleConfig?,
    ): ElectricalRuleEvaluation {
        return evaluate(
            input = input,
            config = config,
            ruleCode = ElectricalRuleCode.MAX_SUPPLY_VOLTAGE,
            fails = { measured, limit -> measured > limit },
        )
    }

    private fun evaluate(
        input: SupplyVoltageInput,
        config: ElectricalRuleConfig?,
        ruleCode: ElectricalRuleCode,
        fails: (Double, Double) -> Boolean,
    ): ElectricalRuleEvaluation {
        val invalidInputExplanation = invalidVoltageExplanation(input.voltage)
        if (invalidInputExplanation != null) {
            return notEvaluated(
                ruleCode = ruleCode,
                config = config,
                measuredValue = input.voltage.takeIf { it.isFinite() },
                explanation = invalidInputExplanation,
            )
        }
        if (config == null) {
            return notEvaluated(
                ruleCode = ruleCode,
                config = null,
                measuredValue = input.voltage,
                explanation = "No se encontró configuración para la regla ${ruleCode.name}.",
            )
        }
        if (!config.enabled) {
            return ElectricalRuleEvaluation(
                ruleCode = ruleCode,
                status = ElectricalRuleEvaluationStatus.DISABLED,
                severity = config.severity,
                measuredValue = input.voltage,
                limitValue = config.numericValue,
                unit = config.unit,
                finding = null,
                explanation = "La regla ${config.name} está desactivada.",
            )
        }
        val limit = config.numericValue
        if (limit == null) {
            return notEvaluated(
                ruleCode = ruleCode,
                config = config,
                measuredValue = input.voltage,
                explanation = "La regla ${config.name} no tiene un valor numérico configurado.",
            )
        }

        val failed = fails(input.voltage, limit)
        val finding = if (failed) buildFinding(input, config) else null
        return ElectricalRuleEvaluation(
            ruleCode = ruleCode,
            status = if (failed) ElectricalRuleEvaluationStatus.FAILED else ElectricalRuleEvaluationStatus.PASSED,
            severity = config.severity,
            measuredValue = input.voltage,
            limitValue = limit,
            unit = config.unit,
            finding = finding,
            explanation = if (failed) "La tensión evaluada no cumple el criterio configurable de ${config.name}." else null,
        )
    }

    private fun buildFinding(
        input: SupplyVoltageInput,
        config: ElectricalRuleConfig,
    ): ElectricalRuleFindingDraft {
        val unit = config.unit.orEmpty()
        val limit = config.numericValue
        val locationText = input.locationText()
        val description = config.findingDescriptionTemplate
            .replace("{measured}", ElectricalRuleValueFormatter.decimal(input.voltage))
            .replace("{limit}", limit?.let(ElectricalRuleValueFormatter::decimal).orEmpty())
            .replace("{unit}", unit)
            .replace("{locationText}", locationText)
        return ElectricalRuleFindingDraft(
            ruleCode = config.code,
            severity = config.severity,
            title = config.findingTitle,
            description = description,
            recommendation = config.recommendationTemplate,
            sourceCalculationId = input.sourceCalculationId,
            inspectionId = input.inspectionId,
        )
    }

    private fun notEvaluated(
        ruleCode: ElectricalRuleCode,
        config: ElectricalRuleConfig?,
        measuredValue: Double?,
        explanation: String,
    ): ElectricalRuleEvaluation = ElectricalRuleEvaluation(
        ruleCode = ruleCode,
        status = ElectricalRuleEvaluationStatus.NOT_EVALUATED,
        severity = config?.severity ?: ElectricalRuleSeverity.RECOMMENDED,
        measuredValue = measuredValue,
        limitValue = config?.numericValue,
        unit = config?.unit,
        finding = null,
        explanation = explanation,
    )

    private fun invalidVoltageExplanation(voltage: Double): String? = when {
        voltage.isNaN() -> "La tensión ingresada no es un número válido."
        !voltage.isFinite() -> "La tensión ingresada debe ser un valor finito."
        voltage <= 0.0 -> "La tensión ingresada debe ser mayor que cero."
        else -> null
    }

    private fun SupplyVoltageInput.locationText(): String {
        val trimmed = location?.trim()?.takeIf(String::isNotBlank) ?: return ""
        val lower = trimmed.lowercase()
        val article = listOf("el ", "la ", "los ", "las ").any(lower::startsWith)
        return if (article) " en $trimmed" else " en el $trimmed"
    }
}
