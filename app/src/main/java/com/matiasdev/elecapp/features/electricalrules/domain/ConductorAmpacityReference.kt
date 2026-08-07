package com.matiasdev.elecapp.features.electricalrules.domain

object ConductorAmpacityReference {
    fun maximumCopperAmps(
        sectionMm2: Double,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): Double? {
        val code = when {
            sectionMm2.matches(1.5) -> ElectricalRuleCode.MAX_CURRENT_COPPER_1_5_MM2
            sectionMm2.matches(2.5) -> ElectricalRuleCode.MAX_CURRENT_COPPER_2_5_MM2
            sectionMm2.matches(4.0) -> ElectricalRuleCode.MAX_CURRENT_COPPER_4_MM2
            sectionMm2.matches(6.0) -> ElectricalRuleCode.MAX_CURRENT_COPPER_6_MM2
            sectionMm2.matches(10.0) -> ElectricalRuleCode.MAX_CURRENT_COPPER_10_MM2
            else -> return null
        }
        return rules.firstOrNull { it.code == code && it.enabled }?.numericValue
    }

    private fun Double.matches(reference: Double): Boolean = kotlin.math.abs(this - reference) < 0.001
}
