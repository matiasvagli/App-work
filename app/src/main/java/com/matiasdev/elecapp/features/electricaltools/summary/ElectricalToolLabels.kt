package com.matiasdev.elecapp.features.electricaltools.summary

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode

fun TechnicalCalculationType.label(): String = when (this) {
    TechnicalCalculationType.POWER_CURRENT_VOLTAGE -> "Potencia, corriente y tensión"
    TechnicalCalculationType.VOLTAGE_DROP -> "Caída de tensión"
    TechnicalCalculationType.CONDUCTOR_SECTION -> "Sección orientativa de conductor"
    TechnicalCalculationType.LIGHTING -> "Luminotecnia"
    TechnicalCalculationType.CAPACITANCE -> "Capacitancia"
    TechnicalCalculationType.POWER_FACTOR_CORRECTION -> "Corrección de factor de potencia"
    TechnicalCalculationType.ENERGY_CONSUMPTION -> "Consumo energético"
    TechnicalCalculationType.OTHER -> "Otro cálculo"
}

fun CalculationSource.label(): String = when (this) {
    CalculationSource.MEASURED -> "Medido"
    CalculationSource.CALCULATED -> "Calculado"
    CalculationSource.ESTIMATED -> "Estimado"
}

fun TechnicalClassification.label(): String = when (this) {
    TechnicalClassification.INFORMATIONAL -> "Informativo"
    TechnicalClassification.ACCEPTABLE -> "Aceptable"
    TechnicalClassification.REQUIRES_REVIEW -> "Requiere revisión técnica"
    TechnicalClassification.CRITICAL_REVIEW -> "Revisión crítica"
    TechnicalClassification.NOT_CLASSIFIED -> "Sin clasificar"
}

fun TechnicianConclusion.label(): String = when (this) {
    TechnicianConclusion.NOT_REVIEWED -> "No revisado"
    TechnicianConclusion.CONFIRMED_OK -> "Confirmado como correcto"
    TechnicianConclusion.CONFIRMED_REQUIRES_ACTION -> "Requiere acción"
    TechnicianConclusion.DISCARDED -> "Descartado"
}

fun ElectricalSystemType.label(): String = when (this) {
    ElectricalSystemType.DC -> "DC"
    ElectricalSystemType.AC_SINGLE_PHASE -> "Monofásico"
    ElectricalSystemType.AC_THREE_PHASE -> "Trifásico"
}

fun ElectricalVariable.label(): String = when (this) {
    ElectricalVariable.POWER -> "Potencia"
    ElectricalVariable.CURRENT -> "Corriente"
    ElectricalVariable.VOLTAGE -> "Tensión"
}

fun TechnicalConductorMaterial.label(): String = when (this) {
    TechnicalConductorMaterial.COPPER -> "Cobre"
    TechnicalConductorMaterial.ALUMINUM -> "Aluminio"
}

fun TemperatureMode.label(): String = when (this) {
    TemperatureMode.REFERENCE -> "Referencia 20 °C"
    TemperatureMode.CUSTOM -> "Temperatura personalizada"
    TemperatureMode.NOT_CONSIDERED -> "No considerada"
}
