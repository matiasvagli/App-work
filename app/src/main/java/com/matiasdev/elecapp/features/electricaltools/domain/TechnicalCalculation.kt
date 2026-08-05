package com.matiasdev.elecapp.features.electricaltools.domain

import java.time.Instant

data class TechnicalCalculation(
    val id: String,
    val type: TechnicalCalculationType,
    val source: CalculationSource,
    val clientId: String?,
    val visitId: String?,
    val inspectionId: String?,
    val title: String,
    val description: String?,
    val inputDataJson: String,
    val resultDataJson: String,
    val primaryResultValue: Double?,
    val primaryResultUnit: String?,
    val classification: TechnicalClassification,
    val technicianConclusion: TechnicianConclusion,
    val technicianNotes: String?,
    val formulaVersion: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

enum class TechnicalCalculationType {
    POWER_CURRENT_VOLTAGE,
    VOLTAGE_DROP,
    CONDUCTOR_SECTION,
    LIGHTING,
    CAPACITANCE,
    POWER_FACTOR_CORRECTION,
    ENERGY_CONSUMPTION,
    OTHER,
}

enum class CalculationSource {
    MEASURED,
    CALCULATED,
    ESTIMATED,
}

enum class TechnicalClassification {
    INFORMATIONAL,
    ACCEPTABLE,
    REQUIRES_REVIEW,
    CRITICAL_REVIEW,
    NOT_CLASSIFIED,
}

enum class TechnicianConclusion {
    NOT_REVIEWED,
    CONFIRMED_OK,
    CONFIRMED_REQUIRES_ACTION,
    DISCARDED,
}

data class CalculationContext(
    val instrumentName: String? = null,
    val measurementContext: String? = null,
    val assumptions: String? = null,
    val dataProvidedByClient: Boolean = false,
)

enum class ElectricalSystemType {
    DC,
    AC_SINGLE_PHASE,
    AC_THREE_PHASE,
}

enum class ElectricalVariable {
    POWER,
    CURRENT,
    VOLTAGE,
}

enum class PowerUnit {
    W,
    KW,
}

enum class CurrentUnit {
    A,
}

enum class VoltageUnit {
    V,
}

enum class TechnicalConductorMaterial {
    COPPER,
    ALUMINUM,
}

enum class TemperatureMode {
    REFERENCE,
    CUSTOM,
    NOT_CONSIDERED,
}

enum class VoltageDropCurrentMode {
    DIRECT_CURRENT,
    DERIVED_FROM_POWER,
}
