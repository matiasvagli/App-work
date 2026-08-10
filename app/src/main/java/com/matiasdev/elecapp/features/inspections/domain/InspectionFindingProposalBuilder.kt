package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter
import java.time.Instant

data class InspectionFindingGroups(
    val confirmed: List<InspectionFinding> = emptyList(),
    val suggested: List<InspectionFinding> = emptyList(),
    val dataReview: List<InspectionFinding> = emptyList(),
    val notVerified: List<InspectionFinding> = emptyList(),
    val manual: List<InspectionFinding> = emptyList(),
)

object InspectionFindingProposalBuilder {
    fun buildGroups(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): InspectionFindingGroups {
        val existingById = aggregate.findings.associateBy { it.id }
        val automatic = buildList {
            addAll(aggregate.confirmedObservationFindings())
            addAll(aggregate.ruleSuggestionFindings(rules))
            addAll(aggregate.dataReviewFindings())
            addAll(aggregate.notVerifiedFindings())
        }.map { proposal ->
            existingById[proposal.id]?.mergeFrom(proposal) ?: proposal
        }
        val automaticIds = automatic.map { it.id }.toSet()
        val retained = aggregate.findings.filter {
            it.sourceType != FindingSourceType.MANUAL && it.id !in automaticIds
        }
        val groupedAutomatic = automatic + retained
        val manual = aggregate.findings.filter { it.sourceType == FindingSourceType.MANUAL }
        return InspectionFindingGroups(
            confirmed = groupedAutomatic.filter { it.sourceType == FindingSourceType.OBSERVATION_CONFIRMED },
            suggested = groupedAutomatic.filter { it.sourceType == FindingSourceType.RULE_SUGGESTION },
            dataReview = groupedAutomatic.filter { it.sourceType == FindingSourceType.DATA_REVIEW },
            notVerified = groupedAutomatic.filter { it.sourceType == FindingSourceType.NOT_VERIFIED },
            manual = manual,
        )
    }

    fun mergeIntoAggregate(
        aggregate: InspectionAggregate,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): InspectionAggregate {
        val groups = buildGroups(aggregate, rules)
        return aggregate.copy(
            findings = groups.confirmed + groups.suggested + groups.dataReview + groups.notVerified + groups.manual,
        )
    }
}

private fun InspectionFinding.mergeFrom(proposal: InspectionFinding): InspectionFinding {
    return proposal.copy(
        sourceType = sourceType,
        severity = severity,
        reviewStatus = reviewStatus,
        includeInReport = includeInReport,
        technicianNotes = technicianNotes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun InspectionAggregate.confirmedObservationFindings(): List<InspectionFinding> = buildList {
    val now = Instant.now()
    pillar?.let { pillar ->
        if (pillar.conductorCondition in listOf(ConductorCondition.DETERIORATED, ConductorCondition.VISIBLE_RISK)) {
            add(proposal("auto:obs:pillar:conductors", FindingCategory.PILLAR, FindingSeverity.PRIORITY, "Conductores observados con deterioro o riesgo visible.", "Pilar y acometida", now, recommendation = InspectionFindingRecommendations.PILLAR_CONDUCTORS))
        }
        if (pillar.protectionCompatibility == ProtectionCompatibility.INCOMPATIBLE) {
            add(proposal("auto:obs:pillar:compatibility", FindingCategory.PROTECTIONS, FindingSeverity.PRIORITY, "Compatibilidad protección/conductor marcada como incompatible.", "Pilar y acometida", now, recommendation = InspectionFindingRecommendations.PROTECTION_COMPATIBILITY))
        }
    }
    mainPanel?.let { panel ->
        if (panel.protectionCompatibility == ProtectionCompatibility.INCOMPATIBLE) {
            add(proposal("auto:obs:panel:compatibility", FindingCategory.PROTECTIONS, FindingSeverity.PRIORITY, "Compatibilidad protección/conductor marcada como incompatible en el tablero principal.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PROTECTION_COMPATIBILITY))
        }
        if (panel.differentialPresent == YesNoUnknown.NO) {
            add(proposal("auto:obs:panel:differential_missing", FindingCategory.PROTECTIONS, FindingSeverity.URGENT, "No se observó interruptor diferencial en el tablero principal.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_DIFFERENTIAL_MISSING))
        }
        if (panel.differentialTestResult == DifferentialTestResult.FAILED) {
            add(proposal("auto:obs:panel:differential_failed", FindingCategory.PROTECTIONS, FindingSeverity.URGENT, "La prueba manual del interruptor diferencial fue fallida.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_DIFFERENTIAL_FAILED))
        }
        if (panel.improvisedConnections == YesNoUnknown.YES) add(proposal("auto:obs:panel:improvised", FindingCategory.MAIN_PANEL, FindingSeverity.PRIORITY, "Se observaron empalmes o conexiones improvisadas.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_IMPROVISED_CONNECTIONS))
        if (panel.overheatingSigns == YesNoUnknown.YES) add(proposal("auto:obs:panel:overheating", FindingCategory.VISIBLE_RISK, FindingSeverity.URGENT, "Se observaron signos de calentamiento o recalentamiento.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_OVERHEATING))
        if (panel.exposedPartsOrDamagedInsulation == YesNoUnknown.YES) add(proposal("auto:obs:panel:exposed", FindingCategory.VISIBLE_RISK, FindingSeverity.URGENT, "Se observaron partes expuestas, aislación dañada o riesgo de contacto.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_EXPOSED_PARTS))
        if (panel.conductorColorStatus == ConductorColorStatus.INCORRECT_OR_MIXED) add(proposal("auto:obs:panel:colors", FindingCategory.CONDUCTORS, FindingSeverity.RECOMMENDED, "Colores de conductores incorrectos o mezclados.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_CONDUCTOR_COLORS))
        if (panel.groundBarPresent == YesNoUnknown.NO) add(proposal("auto:obs:panel:ground_bar_missing", FindingCategory.GROUNDING, FindingSeverity.PRIORITY, "No se observó bornera de tierra en el tablero principal.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_GROUND_BAR_MISSING))
        if (panel.neutralAndGroundSeparated == YesNoUnknown.NO) add(proposal("auto:obs:panel:neutral_ground", FindingCategory.MAIN_PANEL, FindingSeverity.PRIORITY, "Neutro y tierra no se encuentran separados.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_NEUTRAL_GROUND_NOT_SEPARATED))
        if (panel.protectionConductorsPresent == YesNoPartialUnknown.NO) add(proposal("auto:obs:panel:protection_conductors", FindingCategory.GROUNDING, FindingSeverity.PRIORITY, "No se observaron conductores de protección.", "Tablero principal", now, recommendation = InspectionFindingRecommendations.PANEL_PROTECTION_CONDUCTORS_MISSING))
    }
}

private fun InspectionAggregate.ruleSuggestionFindings(
    rules: List<ElectricalRuleConfig>,
): List<InspectionFinding> = buildList {
    val now = Instant.now()
    val min = rules.firstOrNull { it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }?.numericValue
    val max = rules.firstOrNull { it.code == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }?.numericValue
    val voltageMeasurements = pillarMeasurements
        .filter { it.type.isVoltageMeasurement() }
        .map { it.id to Triple(it.value, it.unit, "Pilar y acometida") } +
        mainPanelMeasurements
            .filter { it.section == MainPanelMeasurementSection.INPUT_VOLTAGE }
            .filter { it.type.isVoltageMeasurement() }
            .map { it.id to Triple(it.value, it.unit, "Tablero principal") }
    voltageMeasurements.forEach { (id, data) ->
        val value = data.first ?: return@forEach
        val unit = data.second
        val section = data.third
        if ((min != null && value < min) || (max != null && value > max)) {
            add(
                proposal(
                    id = "auto:rule:voltage:$id",
                    category = FindingCategory.PROTECTIONS,
                    severity = FindingSeverity.RECOMMENDED,
                    description = "Tensión fuera del rango configurado. Valor registrado: ${value.formatNumber()} $unit. Criterio: ${min?.formatNumber() ?: "sin mínimo"} - ${max?.formatNumber() ?: "sin máximo"} $unit. Requiere validación del técnico.",
                    sectionName = section,
                    now = now,
                    sourceType = FindingSourceType.RULE_SUGGESTION,
                    sourceEntityId = id,
                    sourceValue = value,
                    sourceUnit = unit,
                    ruleCode = "SUPPLY_VOLTAGE_RANGE",
                    recommendation = InspectionFindingRecommendations.SUPPLY_VOLTAGE_OUT_OF_RANGE,
                    includeInReport = true,
                    reviewStatus = FindingReviewStatus.PENDING,
                ),
            )
        }
    }
    mainPanelMeasurements
        .filter { it.section == MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK }
        .filter { it.value != null && it.origin != MeasurementOrigin.NOT_VERIFIED }
        .forEach {
            if (mainPanel?.protectionConductorCheckResult == ProtectionConductorCheckResult.REQUIRES_REVIEW) {
                add(
                    proposal(
                        id = "auto:rule:protection_check:${it.id}",
                        category = FindingCategory.GROUNDING,
                        severity = FindingSeverity.RECOMMENDED,
                        description = "Verificación rápida del conductor de protección con valor ${it.value!!.formatNumber()} ${it.unit}. Resultado orientativo: requiere revisión específica.",
                        sectionName = "Tablero principal",
                        now = now,
                        sourceType = FindingSourceType.RULE_SUGGESTION,
                        sourceEntityId = it.id,
                        sourceValue = it.value,
                        sourceUnit = it.unit,
                        ruleCode = "PROTECTION_CONDUCTOR_QUICK_CHECK",
                        recommendation = InspectionFindingRecommendations.PROTECTION_CONDUCTOR_QUICK_CHECK,
                        includeInReport = true,
                        reviewStatus = FindingReviewStatus.PENDING,
                    ),
                )
            }
        }
    addAll(feederVoltageDropFindings(rules, now))
    grounding?.let { grounding ->
        val resistance = grounding.resistanceOhms
        val limit = rules.firstOrNull {
            it.code == ElectricalRuleCode.MAX_GROUND_RESISTANCE_OHMS && it.enabled
        }?.numericValue
        if (
            resistance != null && limit != null &&
            grounding.resistanceOrigin != MeasurementOrigin.NOT_VERIFIED &&
            resistance > limit
        ) {
            add(
                proposal(
                    id = "auto:rule:grounding:resistance",
                    category = FindingCategory.GROUNDING,
                    severity = FindingSeverity.PRIORITY,
                    description = "La resistencia de puesta a tierra registrada (${resistance.formatNumber()} Ω) supera el máximo configurado (${limit.formatNumber()} Ω). Requiere verificación técnica.",
                    sectionName = "Puesta a tierra",
                    now = now,
                    sourceType = FindingSourceType.RULE_SUGGESTION,
                    sourceEntityId = grounding.inspectionId,
                    sourceValue = resistance,
                    sourceUnit = "Ω",
                    ruleCode = ElectricalRuleCode.MAX_GROUND_RESISTANCE_OHMS.name,
                    recommendation = InspectionFindingRecommendations.GROUND_RESISTANCE_ABOVE_LIMIT,
                    includeInReport = true,
                    reviewStatus = FindingReviewStatus.PENDING,
                ),
            )
        }
    }
}

/**
 * Hallazgos de la caída de tensión del alimentador pilar-tablero.
 *
 * Antes esto vivía solo como cálculo [AUTO] dentro de "MEDICIONES Y CÁLCULOS": el informe
 * decía "8,1 % · crítico" pero nadie lo levantaba como hallazgo, así que no llegaba a la
 * lista de acciones ni lo contaba el overview. Una caída por encima del umbral configurado
 * es un hallazgo como cualquier otro; lo que cambia es que sale de una regla, no de una
 * observación del técnico, y por eso entra como sugerencia pendiente de validación.
 *
 * Son tres situaciones distintas y excluyentes en su origen:
 * - La medida supera el umbral: es el caso principal, hay dos tensiones medidas.
 * - Sin mediciones para comparar, la estimada supera el umbral: la acción es medir, no
 *   corregir, porque todavía no hay una caída real registrada.
 * - La medida es muy superior a la estimada: la sección del cable no explica la caída, así
 *   que apunta al recorrido (empalmes, bornes, continuidad) y convive con la primera.
 */
private fun InspectionAggregate.feederVoltageDropFindings(
    rules: List<ElectricalRuleConfig>,
    now: Instant,
): List<InspectionFinding> = buildList {
    val measured = InspectionFeederVoltageDrop.measured(this@feederVoltageDropFindings, rules)
    val estimated = InspectionFeederVoltageDrop.estimated(this@feederVoltageDropFindings, rules)
    if (measured != null && measured.classification == TechnicalClassification.CRITICAL_REVIEW) {
        add(
            proposal(
                id = "auto:rule:feeder_voltage_drop:measured",
                category = FindingCategory.CONDUCTORS,
                severity = FindingSeverity.PRIORITY,
                description = "La caída de tensión entre el pilar y el tablero principal es de " +
                    "${TechnicalValueFormatter.format(measured.percent)} %, por encima del máximo configurado " +
                    "(${TechnicalValueFormatter.format(measured.limitPercent)} %). Pilar ${TechnicalValueFormatter.format(measured.sourceVoltageVolts)} V, " +
                    "tablero ${TechnicalValueFormatter.format(measured.destinationVoltageVolts)} V, " +
                    "diferencia ${TechnicalValueFormatter.format(measured.differenceVolts)} V.",
                sectionName = "Pilar y acometida",
                now = now,
                sourceType = FindingSourceType.RULE_SUGGESTION,
                sourceEntityId = inspection.id,
                sourceValue = measured.percent,
                sourceUnit = "%",
                ruleCode = ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT.name,
                recommendation = InspectionFindingRecommendations.FEEDER_VOLTAGE_DROP_ABOVE_LIMIT,
                includeInReport = true,
                reviewStatus = FindingReviewStatus.PENDING,
            ),
        )
    }
    if (measured == null && estimated != null && estimated.classification == TechnicalClassification.CRITICAL_REVIEW) {
        add(
            proposal(
                id = "auto:rule:feeder_voltage_drop:estimated",
                category = FindingCategory.CONDUCTORS,
                severity = FindingSeverity.RECOMMENDED,
                description = "La caída de tensión estimada del alimentador es de " +
                    "${TechnicalValueFormatter.format(estimated.result.voltageDropPercent)} %, por encima del máximo " +
                    "configurado (${TechnicalValueFormatter.format(estimated.limitPercent)} %). Estimada con " +
                    "${TechnicalValueFormatter.format(estimated.lengthMeters)} m, ${TechnicalValueFormatter.format(estimated.sectionMm2)} mm² y " +
                    "${TechnicalValueFormatter.format(estimated.currentAmps)} A. No se registraron mediciones de tensión en " +
                    "el pilar y el tablero para confirmarla.",
                sectionName = "Pilar y acometida",
                now = now,
                sourceType = FindingSourceType.RULE_SUGGESTION,
                sourceEntityId = inspection.id,
                sourceValue = estimated.result.voltageDropPercent,
                sourceUnit = "%",
                ruleCode = ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT.name,
                recommendation = InspectionFindingRecommendations.FEEDER_VOLTAGE_DROP_ESTIMATED_ABOVE_LIMIT,
                includeInReport = true,
                reviewStatus = FindingReviewStatus.PENDING,
            ),
        )
    }
    FeederVoltageDropEvaluator.compare(measured, estimated)?.let { review ->
        add(
            proposal(
                id = "auto:rule:feeder_voltage_drop:review",
                category = FindingCategory.CONDUCTORS,
                severity = FindingSeverity.PRIORITY,
                description = "La caída de tensión medida entre el pilar y el tablero " +
                    "(${TechnicalValueFormatter.format(review.measuredPercent)} %) es muy superior a la estimada para los " +
                    "datos relevados (${TechnicalValueFormatter.format(review.estimatedPercent)} %).",
                sectionName = "Pilar y acometida",
                now = now,
                sourceType = FindingSourceType.RULE_SUGGESTION,
                sourceEntityId = inspection.id,
                sourceValue = review.measuredPercent,
                sourceUnit = "%",
                ruleCode = "FEEDER_VOLTAGE_DROP_VS_ESTIMATE",
                recommendation = InspectionFindingRecommendations.FEEDER_VOLTAGE_DROP_HIGHER_THAN_ESTIMATED,
                includeInReport = true,
                reviewStatus = FindingReviewStatus.PENDING,
            ),
        )
    }
}

private fun PillarMeasurementType.isVoltageMeasurement(): Boolean = this in setOf(
    PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
    PillarMeasurementType.VOLTAGE_L1_N,
    PillarMeasurementType.VOLTAGE_L2_N,
    PillarMeasurementType.VOLTAGE_L3_N,
    PillarMeasurementType.VOLTAGE_L1_L2,
    PillarMeasurementType.VOLTAGE_L2_L3,
    PillarMeasurementType.VOLTAGE_L3_L1,
)

private fun MainPanelMeasurementType.isVoltageMeasurement(): Boolean = this in setOf(
    MainPanelMeasurementType.INPUT_VOLTAGE_LN,
    MainPanelMeasurementType.INPUT_VOLTAGE_L1_N,
    MainPanelMeasurementType.INPUT_VOLTAGE_L2_N,
    MainPanelMeasurementType.INPUT_VOLTAGE_L3_N,
    MainPanelMeasurementType.INPUT_VOLTAGE_L1_L2,
    MainPanelMeasurementType.INPUT_VOLTAGE_L2_L3,
    MainPanelMeasurementType.INPUT_VOLTAGE_L3_L1,
)

private fun InspectionAggregate.dataReviewFindings(): List<InspectionFinding> = buildList {
    val now = Instant.now()
    mainPanelCircuits.reportableCircuitsInReportOrder().forEachIndexed { index, circuit ->
        val circuitName = circuit.reportCircuitName(index)
        val breaker = circuit.breakerAmps ?: circuit.breakerOtherAmps
        val section = circuit.conductorSectionMm2 ?: circuit.conductorOtherSectionMm2
        val consumption = circuit.consumptionAmps
        if (
            breaker != null && consumption != null &&
            circuit.consumptionOrigin != MeasurementOrigin.NOT_VERIFIED &&
            consumption > breaker
        ) {
            add(
                proposal(
                    id = "auto:rule:circuit:${circuit.id}:consumption-breaker",
                    category = FindingCategory.PROTECTIONS,
                    severity = FindingSeverity.PRIORITY,
                    description = "$circuitName: el consumo registrado (${consumption.formatNumber()} A) supera la corriente nominal de la térmica (${breaker} A). Verificar medición, distribución de cargas y actuación de la protección.",
                    sectionName = "Tablero principal",
                    now = now,
                    sourceType = FindingSourceType.RULE_SUGGESTION,
                    sourceEntityId = circuit.id,
                    sourceValue = consumption,
                    sourceUnit = "A",
                    ruleCode = "CIRCUIT_CONSUMPTION_BREAKER",
                    recommendation = InspectionFindingRecommendations.CIRCUIT_CONSUMPTION_ABOVE_BREAKER,
                    includeInReport = true,
                    reviewStatus = FindingReviewStatus.PENDING,
                ),
            )
        }
        if (section != null && section <= 2.5 && consumption != null && consumption >= 80.0) {
            add(
                proposal(
                    id = "auto:data:circuit:${circuit.id}:consumption",
                    category = FindingCategory.CIRCUITS,
                    severity = FindingSeverity.OK,
                    description = "$circuitName: revisar el valor ingresado antes de incluirlo en el informe. Consumo registrado: ${consumption.formatNumber()} A para conductor de ${section.formatNumber()} mm².",
                    sectionName = "Tablero principal",
                    now = now,
                    sourceType = FindingSourceType.DATA_REVIEW,
                    sourceEntityId = circuit.id,
                    sourceValue = consumption,
                    sourceUnit = "A",
                    includeInReport = true,
                    reviewStatus = FindingReviewStatus.PENDING,
                ),
            )
        }
    }
}

private fun InspectionAggregate.notVerifiedFindings(): List<InspectionFinding> = buildList {
    val now = Instant.now()
    unverifiedItems.forEach {
        add(
            proposal(
                id = "auto:not_verified:${it.id}",
                category = FindingCategory.OTHER,
                severity = FindingSeverity.OK,
                description = it.description ?: it.type.name,
                sectionName = "No verificado",
                now = now,
                sourceType = FindingSourceType.NOT_VERIFIED,
                sourceEntityId = it.id,
                includeInReport = true,
                reviewStatus = FindingReviewStatus.PENDING,
            ),
        )
    }
    mainPanel?.let { panel ->
        if (panel.groundBarPresent == YesNoUnknown.UNKNOWN) add(proposal("auto:not_verified:panel:ground_bar", FindingCategory.GROUNDING, FindingSeverity.OK, "No se verificó la existencia de bornera de tierra.", "Tablero principal", now, FindingSourceType.NOT_VERIFIED, includeInReport = true))
        if (panel.neutralBarPresent == YesNoUnknown.UNKNOWN) add(proposal("auto:not_verified:panel:neutral_bar", FindingCategory.MAIN_PANEL, FindingSeverity.OK, "No se verificó la existencia de bornera de neutro.", "Tablero principal", now, FindingSourceType.NOT_VERIFIED, includeInReport = true))
        if (panel.differentialPresent == YesNoUnknown.UNKNOWN) add(proposal("auto:not_verified:panel:differential", FindingCategory.PROTECTIONS, FindingSeverity.OK, "No se verificó interruptor diferencial.", "Tablero principal", now, FindingSourceType.NOT_VERIFIED, includeInReport = true))
    }
}

private fun InspectionAggregate.proposal(
    id: String,
    category: FindingCategory,
    severity: FindingSeverity,
    description: String,
    sectionName: String,
    now: Instant,
    sourceType: FindingSourceType = FindingSourceType.OBSERVATION_CONFIRMED,
    sourceEntityId: String? = id,
    sourceValue: Double? = null,
    sourceUnit: String? = null,
    ruleCode: String? = null,
    recommendation: String? = null,
    includeInReport: Boolean = sourceType == FindingSourceType.OBSERVATION_CONFIRMED || sourceType == FindingSourceType.NOT_VERIFIED,
    reviewStatus: FindingReviewStatus = if (sourceType == FindingSourceType.OBSERVATION_CONFIRMED) FindingReviewStatus.CONFIRMED else FindingReviewStatus.PENDING,
): InspectionFinding {
    return InspectionFinding(
        id = id,
        inspectionId = inspection.id,
        category = category,
        severity = severity,
        title = sectionName,
        description = description,
        recommendation = recommendation,
        sourceType = sourceType,
        sourceSection = sectionName.toInspectionSection(),
        sourceEntityId = sourceEntityId,
        sourceValue = sourceValue,
        sourceUnit = sourceUnit,
        ruleCode = ruleCode,
        reviewStatus = reviewStatus,
        includeInReport = includeInReport,
        technicianNotes = null,
        sortOrder = 0,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )
}

private fun String.toInspectionSection(): InspectionSection? = when (this) {
    "Pilar y acometida" -> InspectionSection.PILLAR
    "Tablero principal" -> InspectionSection.MAIN_PANEL
    "Puesta a tierra" -> InspectionSection.GROUNDING
    "No verificado" -> InspectionSection.UNVERIFIED
    else -> null
}

private fun Double.formatNumber(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString().replace(".", ",")
}
