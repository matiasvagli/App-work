package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropResult
import kotlin.math.abs

data class MeasuredVoltageDropResult(
    val sourceVoltageVolts: Double,
    val destinationVoltageVolts: Double,
    val differenceVolts: Double,
    val percent: Double,
    val classification: TechnicalClassification,
)

data class FeederVoltageDropEstimate(
    val lengthMeters: Double,
    val sectionMm2: Double,
    val material: ConductorMaterial,
    val currentAmps: Double,
    val currentOrigin: MeasurementOrigin,
    val result: VoltageDropResult,
    val classification: TechnicalClassification,
)

data class FeederVoltageDropReview(
    val measuredPercent: Double,
    val estimatedPercent: Double,
    val classification: TechnicalClassification,
)

object VoltageDropMeasuredEvaluator {
    fun evaluate(
        sourceVoltageVolts: Double?,
        destinationVoltageVolts: Double?,
        maxAllowedPercent: Double?,
    ): MeasuredVoltageDropResult? {
        val source = sourceVoltageVolts?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val destination = destinationVoltageVolts?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val difference = source - destination
        val percent = abs(difference) / source * 100.0
        return MeasuredVoltageDropResult(
            sourceVoltageVolts = source,
            destinationVoltageVolts = destination,
            differenceVolts = difference,
            percent = percent,
            classification = VoltageDropClassification.classify(percent, maxAllowedPercent),
        )
    }
}

object FeederVoltageDropCalculator {
    fun calculate(
        supplyType: SupplyType,
        sourceVoltageVolts: Double?,
        destinationVoltageVolts: Double?,
        lengthMeters: Double?,
        sectionMm2: Double?,
        material: ConductorMaterial,
        currentAmps: Double?,
        currentOrigin: MeasurementOrigin,
        maxAllowedPercent: Double?,
    ): FeederVoltageDropEstimate? {
        val length = lengthMeters?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val section = sectionMm2?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        val current = currentAmps?.takeIf { it.isFinite() && it > 0.0 } ?: return null
        if (currentOrigin == MeasurementOrigin.NOT_VERIFIED) return null
        val technicalMaterial = material.toTechnicalMaterial() ?: return null
        val voltage = sourceVoltageVolts?.takeIf { it.isFinite() && it > 0.0 }
            ?: destinationVoltageVolts?.takeIf { it.isFinite() && it > 0.0 }
            ?: defaultNominalVoltage(supplyType)
        val result = VoltageDropCalculator.calculate(
            VoltageDropInput(
                systemType = supplyType.toElectricalSystemType(),
                nominalVoltageVolts = voltage,
                currentMode = VoltageDropCurrentMode.DIRECT_CURRENT,
                currentAmps = current,
                activePowerWatts = null,
                powerFactor = null,
                efficiency = null,
                conductorLengthMeters = length,
                conductorSectionMm2 = section,
                conductorMaterial = technicalMaterial,
                temperatureMode = TemperatureMode.NOT_CONSIDERED,
                conductorTemperatureCelsius = null,
                source = CalculationSource.CALCULATED,
            ),
        ).value ?: return null
        return FeederVoltageDropEstimate(
            lengthMeters = length,
            sectionMm2 = section,
            material = material,
            currentAmps = current,
            currentOrigin = currentOrigin,
            result = result,
            classification = VoltageDropClassification.classify(result.voltageDropPercent, maxAllowedPercent),
        )
    }
}

object FeederVoltageDropEvaluator {
    fun compare(
        measured: MeasuredVoltageDropResult?,
        estimated: FeederVoltageDropEstimate?,
    ): FeederVoltageDropReview? {
        if (measured == null || estimated == null) return null
        val estimatedPercent = estimated.result.voltageDropPercent
        val difference = measured.percent - estimatedPercent
        val significantlyHigher = measured.percent >= estimatedPercent * 2.0 && difference >= 2.0
        if (!significantlyHigher) return null
        return FeederVoltageDropReview(
            measuredPercent = measured.percent,
            estimatedPercent = estimatedPercent,
            classification = TechnicalClassification.REQUIRES_REVIEW,
        )
    }
}

private object VoltageDropClassification {
    fun classify(percent: Double, maxAllowedPercent: Double?): TechnicalClassification {
        val limit = maxAllowedPercent?.takeIf { it.isFinite() && it > 0.0 } ?: 3.0
        return if (percent <= limit) TechnicalClassification.ACCEPTABLE else TechnicalClassification.CRITICAL_REVIEW
    }
}

private fun ConductorMaterial.toTechnicalMaterial(): TechnicalConductorMaterial? = when (this) {
    ConductorMaterial.COPPER -> TechnicalConductorMaterial.COPPER
    ConductorMaterial.ALUMINUM -> TechnicalConductorMaterial.ALUMINUM
    ConductorMaterial.OTHER, ConductorMaterial.UNKNOWN -> null
}

private fun SupplyType.toElectricalSystemType(): ElectricalSystemType = when (this) {
    SupplyType.THREE_PHASE -> ElectricalSystemType.AC_THREE_PHASE
    SupplyType.SINGLE_PHASE, SupplyType.UNKNOWN -> ElectricalSystemType.AC_SINGLE_PHASE
}

private fun defaultNominalVoltage(supplyType: SupplyType): Double = when (supplyType) {
    SupplyType.THREE_PHASE -> 380.0
    SupplyType.SINGLE_PHASE, SupplyType.UNKNOWN -> 220.0
}
