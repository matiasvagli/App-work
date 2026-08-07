package com.matiasdev.elecapp.features.electricalrules.domain

data class ElectricalRuleConfig(
    val code: ElectricalRuleCode,
    val name: String,
    val enabled: Boolean,
    val severity: ElectricalRuleSeverity,
    val numericValue: Double?,
    val secondaryNumericValue: Double?,
    val unit: String?,
    val findingTitle: String,
    val findingDescriptionTemplate: String,
    val recommendationTemplate: String?,
    val configVersion: Int,
)

object DefaultElectricalRuleConfigs {
    val all: List<ElectricalRuleConfig> = listOf(
        ElectricalRuleConfig(
            code = ElectricalRuleCode.MIN_SUPPLY_VOLTAGE,
            name = "Tensión mínima de suministro",
            enabled = true,
            severity = ElectricalRuleSeverity.IMPORTANT,
            numericValue = 190.0,
            secondaryNumericValue = null,
            unit = "V",
            findingTitle = "Baja tensión en el suministro",
            findingDescriptionTemplate = "Se registró una tensión de {measured} {unit}{locationText}, inferior al límite configurado de {limit} {unit}.",
            recommendationTemplate = "Se recomienda verificar el suministro, repetir la medición bajo condiciones de carga conocidas y evaluar el estado de la acometida.",
            configVersion = CONFIG_VERSION,
        ),
        ElectricalRuleConfig(
            code = ElectricalRuleCode.MAX_SUPPLY_VOLTAGE,
            name = "Tensión máxima de suministro",
            enabled = true,
            severity = ElectricalRuleSeverity.IMPORTANT,
            numericValue = 250.0,
            secondaryNumericValue = null,
            unit = "V",
            findingTitle = "Tensión elevada en el suministro",
            findingDescriptionTemplate = "Se registró una tensión de {measured} {unit}{locationText}, superior al límite configurado de {limit} {unit}.",
            recommendationTemplate = "Se recomienda repetir la medición y verificar las condiciones del suministro antes de intervenir la instalación.",
            configVersion = CONFIG_VERSION,
        ),
        ElectricalRuleConfig(
            code = ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT,
            name = "Caída máxima entre pilar y tablero principal",
            enabled = true,
            severity = ElectricalRuleSeverity.IMPORTANT,
            numericValue = 3.0,
            secondaryNumericValue = null,
            unit = "%",
            findingTitle = "Caída de tensión entre pilar y tablero a revisar",
            findingDescriptionTemplate = "La caída de tensión supera el límite configurado de {limit} {unit}.",
            recommendationTemplate = "Verificar tensión en origen y destino, longitud, sección, corriente y estado de conexiones.",
            configVersion = CONFIG_VERSION,
        ),
        ElectricalRuleConfig(
            code = ElectricalRuleCode.MAX_GROUND_RESISTANCE_OHMS,
            name = "Resistencia máxima de puesta a tierra",
            enabled = true,
            severity = ElectricalRuleSeverity.IMPORTANT,
            numericValue = 40.0,
            secondaryNumericValue = null,
            unit = "Ω",
            findingTitle = "Resistencia de puesta a tierra a revisar",
            findingDescriptionTemplate = "La resistencia medida supera el límite configurado de {limit} {unit}.",
            recommendationTemplate = "Verificar la medición con telurómetro, el electrodo, sus conexiones y la continuidad del sistema de protección.",
            configVersion = CONFIG_VERSION,
        ),
        maxCurrentRule(
            code = ElectricalRuleCode.MAX_CURRENT_COPPER_1_5_MM2,
            sectionLabel = "1,5 mm²",
            maxCurrent = 10.0,
        ),
        maxCurrentRule(
            code = ElectricalRuleCode.MAX_CURRENT_COPPER_2_5_MM2,
            sectionLabel = "2,5 mm²",
            maxCurrent = 16.0,
        ),
        maxCurrentRule(
            code = ElectricalRuleCode.MAX_CURRENT_COPPER_4_MM2,
            sectionLabel = "4 mm²",
            maxCurrent = 25.0,
        ),
        maxCurrentRule(
            code = ElectricalRuleCode.MAX_CURRENT_COPPER_6_MM2,
            sectionLabel = "6 mm²",
            maxCurrent = 32.0,
        ),
        maxCurrentRule(
            code = ElectricalRuleCode.MAX_CURRENT_COPPER_10_MM2,
            sectionLabel = "10 mm²",
            maxCurrent = 40.0,
        ),
    )

    private fun maxCurrentRule(
        code: ElectricalRuleCode,
        sectionLabel: String,
        maxCurrent: Double,
    ): ElectricalRuleConfig = ElectricalRuleConfig(
        code = code,
        name = "Corriente máxima cobre $sectionLabel",
        enabled = true,
        severity = ElectricalRuleSeverity.IMPORTANT,
        numericValue = maxCurrent,
        secondaryNumericValue = null,
        unit = "A",
        findingTitle = "Protección posiblemente incompatible con conductor",
        findingDescriptionTemplate = "La corriente de protección supera el límite configurado de {limit} {unit} para conductor de cobre $sectionLabel.",
        recommendationTemplate = "Verificar sección real, material, condiciones de instalación y protección antes de definir una corrección.",
        configVersion = CONFIG_VERSION,
    )

    private const val CONFIG_VERSION = 2
}
