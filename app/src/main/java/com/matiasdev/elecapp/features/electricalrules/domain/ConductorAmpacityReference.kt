package com.matiasdev.elecapp.features.electricalrules.domain

object ConductorAmpacityReference {
    data class Reference(
        val sectionMm2: Double,
        val maxCurrentAmps: Double,
    )

    fun maximumCopperAmps(
        sectionMm2: Double,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): Double? {
        val code = copperReferenceCodes.firstOrNull { (section, _) -> sectionMm2.matches(section) }?.second ?: return null
        return rules.firstOrNull { it.code == code && it.enabled }?.numericValue
    }

    fun copperReferences(
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): List<Reference> {
        return copperReferenceCodes.mapNotNull { (section, code) ->
            val maxCurrent = rules.firstOrNull { it.code == code && it.enabled }?.numericValue ?: return@mapNotNull null
            Reference(sectionMm2 = section, maxCurrentAmps = maxCurrent)
        }.sortedBy { it.sectionMm2 }
    }

    fun minimumCopperSectionForAmps(
        breakerAmps: Double,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): Reference? {
        return copperReferences(rules).firstOrNull { it.maxCurrentAmps >= breakerAmps }
    }

    private val copperReferenceCodes = listOf(
        1.5 to ElectricalRuleCode.MAX_CURRENT_COPPER_1_5_MM2,
        2.5 to ElectricalRuleCode.MAX_CURRENT_COPPER_2_5_MM2,
        4.0 to ElectricalRuleCode.MAX_CURRENT_COPPER_4_MM2,
        6.0 to ElectricalRuleCode.MAX_CURRENT_COPPER_6_MM2,
        10.0 to ElectricalRuleCode.MAX_CURRENT_COPPER_10_MM2,
    )

    private fun Double.matches(reference: Double): Boolean = kotlin.math.abs(this - reference) < 0.001
}
