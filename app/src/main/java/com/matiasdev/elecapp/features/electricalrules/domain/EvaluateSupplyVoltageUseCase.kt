package com.matiasdev.elecapp.features.electricalrules.domain

class EvaluateSupplyVoltageUseCase(
    private val repository: ElectricalRuleConfigRepository,
    private val evaluator: SupplyVoltageRuleEvaluator = SupplyVoltageRuleEvaluator(),
) {
    suspend operator fun invoke(input: SupplyVoltageInput): List<ElectricalRuleEvaluation> {
        val minimumConfig = repository.getByCode(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE)
        val maximumConfig = repository.getByCode(ElectricalRuleCode.MAX_SUPPLY_VOLTAGE)
        return listOf(
            evaluator.evaluateMinimum(input, minimumConfig),
            evaluator.evaluateMaximum(input, maximumConfig),
        )
    }
}
