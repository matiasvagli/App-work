package com.matiasdev.elecapp.features.electricaltools.ui

import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationContext
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput
import com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageResult
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult
import java.time.Instant
import java.util.UUID

data class CalculationAssociationDraft(
    val clientId: String? = null,
    val visitId: String? = null,
    val inspectionId: String? = null,
    val label: String = "Sin asociación",
) {
    val hasAssociation: Boolean get() = clientId != null || visitId != null || inspectionId != null
}

fun buildPowerCalculation(
    existingId: String?,
    input: PowerCurrentVoltageInput,
    result: PowerCurrentVoltageResult,
    association: CalculationAssociationDraft,
): TechnicalCalculation {
    val now = Instant.now()
    val primary = when (result.calculatedVariable) {
        com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable.POWER -> result.powerWatts to "W"
        com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable.CURRENT -> result.currentAmps to "A"
        com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable.VOLTAGE -> result.voltageVolts to "V"
    }
    return TechnicalCalculation(
        id = existingId ?: UUID.randomUUID().toString(),
        type = TechnicalCalculationType.POWER_CURRENT_VOLTAGE,
        source = input.source,
        clientId = association.clientId,
        visitId = association.visitId,
        inspectionId = association.inspectionId,
        title = "Cálculo de ${result.calculatedVariable.name.lowercase()}",
        description = null,
        inputDataJson = CalculationJson.encode(input),
        resultDataJson = CalculationJson.encode(result),
        primaryResultValue = primary.first,
        primaryResultUnit = primary.second,
        classification = TechnicalClassification.INFORMATIONAL,
        technicianConclusion = TechnicianConclusion.NOT_REVIEWED,
        technicianNotes = null,
        formulaVersion = result.formulaVersion,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )
}

fun buildVoltageDropCalculation(
    existingId: String?,
    input: VoltageDropInput,
    result: VoltageDropResult,
    association: CalculationAssociationDraft,
): TechnicalCalculation {
    val now = Instant.now()
    return TechnicalCalculation(
        id = existingId ?: UUID.randomUUID().toString(),
        type = TechnicalCalculationType.VOLTAGE_DROP,
        source = input.source,
        clientId = association.clientId,
        visitId = association.visitId,
        inspectionId = association.inspectionId,
        title = "Caída de tensión",
        description = "Resultado orientativo basado en resistencia del conductor.",
        inputDataJson = CalculationJson.encode(input),
        resultDataJson = CalculationJson.encode(result),
        primaryResultValue = result.voltageDropPercent,
        primaryResultUnit = "%",
        classification = result.classification,
        technicianConclusion = TechnicianConclusion.NOT_REVIEWED,
        technicianNotes = null,
        formulaVersion = result.formulaVersion,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )
}

fun contextFromFields(
    source: CalculationSource,
    instrumentName: String,
    measurementContext: String,
    assumptions: String,
    dataProvidedByClient: Boolean,
): CalculationContext = CalculationContext(
    instrumentName = instrumentName.takeIf { source == CalculationSource.MEASURED }?.trim()?.takeIf(String::isNotBlank),
    measurementContext = measurementContext.trim().takeIf(String::isNotBlank),
    assumptions = assumptions.trim().takeIf(String::isNotBlank),
    dataProvidedByClient = dataProvidedByClient,
)
