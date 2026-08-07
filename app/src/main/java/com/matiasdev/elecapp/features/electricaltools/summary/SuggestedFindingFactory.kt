package com.matiasdev.elecapp.features.electricaltools.summary

import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.FindingSourceType
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import java.time.Instant
import java.util.UUID

object SuggestedFindingFactory {
    fun fromCalculation(calculation: TechnicalCalculation): InspectionFinding? {
        if (calculation.inspectionId.isNullOrBlank()) return null
        if (calculation.classification !in reviewClassifications) return null
        return when (calculation.type) {
            TechnicalCalculationType.VOLTAGE_DROP -> voltageDropFinding(calculation)
            else -> null
        }
    }

    private fun voltageDropFinding(calculation: TechnicalCalculation): InspectionFinding? {
        val input = CalculationJson.decodeVoltageDropInput(calculation.inputDataJson) ?: return null
        val result = CalculationJson.decodeVoltageDropResult(calculation.resultDataJson) ?: return null
        val now = Instant.now()
        return InspectionFinding(
            id = UUID.randomUUID().toString(),
            inspectionId = calculation.inspectionId!!,
            category = FindingCategory.GENERAL,
            severity = if (calculation.classification == TechnicalClassification.CRITICAL_REVIEW) {
                FindingSeverity.URGENT
            } else {
                FindingSeverity.RECOMMENDED
            },
            title = "Caída de tensión estimada a revisar",
            description = "Ingresando una longitud de ${TechnicalValueFormatter.withUnit(input.conductorLengthMeters, "m")}, " +
                "una corriente de ${TechnicalValueFormatter.withUnit(result.currentUsedAmps, "A")} y una sección de " +
                "${TechnicalValueFormatter.withUnit(input.conductorSectionMm2, "mm²")}, se obtuvo una caída aproximada de " +
                "${TechnicalValueFormatter.withUnit(result.voltageDropVolts, "V")} (${TechnicalValueFormatter.withUnit(result.voltageDropPercent, "%")}).",
            recommendation = "Verificar la sección y longitud del tendido, el estado de conexiones, la carga real del circuito y las condiciones de instalación antes de definir una corrección.",
            sourceType = FindingSourceType.RULE_SUGGESTION,
            sourceSection = InspectionSection.FINDINGS,
            sourceEntityId = calculation.id,
            sourceValue = result.voltageDropPercent,
            sourceUnit = "%",
            ruleCode = "VOLTAGE_DROP",
            reviewStatus = FindingReviewStatus.PENDING,
            includeInReport = false,
            technicianNotes = null,
            sortOrder = Int.MAX_VALUE,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }

    private val reviewClassifications = setOf(
        TechnicalClassification.REQUIRES_REVIEW,
        TechnicalClassification.CRITICAL_REVIEW,
    )
}
