package com.matiasdev.elecapp.features.electricalrules.data

import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleSeverity

fun ElectricalRuleConfigEntity.toDomain(): ElectricalRuleConfig = ElectricalRuleConfig(
    code = enumValue(code, ElectricalRuleCode.MIN_SUPPLY_VOLTAGE),
    name = name,
    enabled = enabled,
    severity = enumValue(severity, ElectricalRuleSeverity.RECOMMENDED),
    numericValue = numericValue,
    secondaryNumericValue = secondaryNumericValue,
    unit = unit,
    findingTitle = findingTitle,
    findingDescriptionTemplate = findingDescriptionTemplate,
    recommendationTemplate = recommendationTemplate,
    configVersion = configVersion,
)

fun ElectricalRuleConfig.toEntity(): ElectricalRuleConfigEntity = ElectricalRuleConfigEntity(
    code = code.name,
    name = name,
    enabled = enabled,
    severity = severity.name,
    numericValue = numericValue,
    secondaryNumericValue = secondaryNumericValue,
    unit = unit,
    findingTitle = findingTitle,
    findingDescriptionTemplate = findingDescriptionTemplate,
    recommendationTemplate = recommendationTemplate,
    configVersion = configVersion,
)

private inline fun <reified T : Enum<T>> enumValue(value: String, fallback: T): T {
    return runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}
