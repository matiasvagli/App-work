package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown

fun InspectionStatus.label(): String = when (this) {
    InspectionStatus.DRAFT -> "En borrador"
    InspectionStatus.COMPLETED -> "Finalizado"
}

fun InspectionType.label(): String = when (this) {
    InspectionType.VISUAL -> "Inspección visual"
    InspectionType.VISUAL_AND_MEASUREMENTS -> "Inspección visual y mediciones"
}

fun InspectionScope.label(): String = when (this) {
    InspectionScope.VISUAL_INSPECTION -> "Inspección visual"
    InspectionScope.SECTOR_ASSESSMENT -> "Relevamiento por sector"
    InspectionScope.GENERAL_ASSESSMENT -> "Relevamiento general"
}

fun InspectionScope.description(): String = when (this) {
    InspectionScope.VISUAL_INSPECTION -> "Revisión rápida y puntual, limitada a los elementos visibles y verificaciones registradas."
    InspectionScope.SECTOR_ASSESSMENT -> "Evaluación detallada de una zona, equipo o circuito específico."
    InspectionScope.GENERAL_ASSESSMENT -> "Evaluación general de los componentes accesibles de la instalación."
}

fun GeneralCondition.label(): String = when (this) {
    GeneralCondition.GOOD -> "Bueno"
    GeneralCondition.FAIR -> "Regular"
    GeneralCondition.POOR -> "Malo"
    GeneralCondition.CRITICAL -> "Crítico"
    GeneralCondition.NOT_ASSESSED -> "No evaluado"
}

fun SupplyType.label(): String = when (this) {
    SupplyType.SINGLE_PHASE -> "Monofásico"
    SupplyType.THREE_PHASE -> "Trifásico"
    SupplyType.UNKNOWN -> "No verificado"
}

fun PropertyType.label(): String = when (this) {
    PropertyType.UNKNOWN -> "No verificado"
    PropertyType.HOUSE -> "Casa"
    PropertyType.APARTMENT -> "Departamento"
    PropertyType.COMMERCIAL -> "Comercial"
    PropertyType.OTHER -> "Otro"
}

fun AccessStatus.label(): String = when (this) {
    AccessStatus.UNKNOWN -> "No verificado"
    AccessStatus.YES -> "Accesible"
    AccessStatus.NO -> "No accesible"
    AccessStatus.PARTIAL -> "Acceso parcial"
}

fun YesNoUnknown.label(): String = when (this) {
    YesNoUnknown.YES -> "Sí"
    YesNoUnknown.NO -> "No"
    YesNoUnknown.UNKNOWN -> "No verificado"
}

fun YesNoPartialUnknown.label(): String = when (this) {
    YesNoPartialUnknown.YES -> "Sí"
    YesNoPartialUnknown.NO -> "No"
    YesNoPartialUnknown.PARTIAL -> "Parcial"
    YesNoPartialUnknown.UNKNOWN -> "No verificado"
}

fun ConductorMaterial.label(): String = when (this) {
    ConductorMaterial.COPPER -> "Cobre"
    ConductorMaterial.ALUMINUM -> "Aluminio"
    ConductorMaterial.OTHER -> "Otro"
    ConductorMaterial.UNKNOWN -> "No verificado"
}

fun ConductorCondition.label(): String = when (this) {
    ConductorCondition.GOOD -> "Bueno"
    ConductorCondition.FAIR -> "Regular"
    ConductorCondition.DETERIORATED -> "Deteriorado"
    ConductorCondition.VISIBLE_RISK -> "Riesgo visible"
    ConductorCondition.EXPOSED -> "Expuestos"
    ConductorCondition.OVERHEATED -> "Con recalentamiento"
    ConductorCondition.NOT_ASSESSED -> "No evaluado"
}

fun ProtectionCompatibility.label(): String = when (this) {
    ProtectionCompatibility.COMPATIBLE -> "Compatible"
    ProtectionCompatibility.INCOMPATIBLE -> "Incompatible"
    ProtectionCompatibility.REQUIRES_VERIFICATION -> "Requiere verificación"
    ProtectionCompatibility.NOT_ASSESSED -> "No verificada"
}

fun DifferentialTestResult.label(): String = when (this) {
    DifferentialTestResult.PASSED -> "Correcta"
    DifferentialTestResult.FAILED -> "Fallida"
    DifferentialTestResult.NOT_TESTED -> "No realizada"
    DifferentialTestResult.NOT_APPLICABLE -> "No aplicable"
}

fun FindingCategory.label(): String = when (this) {
    FindingCategory.PILLAR -> "Pilar y acometida"
    FindingCategory.MAIN_PANEL -> "Tablero principal"
    FindingCategory.GENERAL -> "General"
    FindingCategory.OTHER -> "Otro"
}

fun FindingSeverity.label(): String = when (this) {
    FindingSeverity.OK -> "OK"
    FindingSeverity.RECOMMENDED -> "Recomendado"
    FindingSeverity.URGENT -> "Urgente"
}

fun UnverifiedItemType.label(): String = when (this) {
    UnverifiedItemType.GROUNDING_SYSTEM -> "Resistencia de puesta a tierra"
    UnverifiedItemType.INSULATION_RESISTANCE -> "Resistencia de aislación"
    UnverifiedItemType.HIDDEN_WIRING -> "Conductores ocultos"
    UnverifiedItemType.INACCESSIBLE_AREA -> "Sector inaccesible"
    UnverifiedItemType.PANEL_NOT_OPENED -> "Tablero no abierto"
    UnverifiedItemType.PILLAR_NOT_ACCESSIBLE -> "Pilar no accesible"
    UnverifiedItemType.CIRCUIT_OUT_OF_SERVICE -> "Circuito fuera de servicio"
    UnverifiedItemType.NO_MEASUREMENTS -> "No se realizaron mediciones"
    UnverifiedItemType.INDIVIDUAL_CIRCUITS -> "Circuitos individuales"
    UnverifiedItemType.OTHER -> "Otro"
}

fun InspectionSectionReviewStatus.label(): String = when (this) {
    InspectionSectionReviewStatus.REVIEWED -> "Corresponde"
    InspectionSectionReviewStatus.NOT_APPLICABLE -> "No corresponde"
    InspectionSectionReviewStatus.NOT_VERIFIED -> "No se verificó"
}

fun com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN -> "Tensión fase-neutro"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.SINGLE_PHASE_CURRENT -> "Consumo"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.VOLTAGE_L1_N -> "L1-N"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.VOLTAGE_L2_N -> "L2-N"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.VOLTAGE_L3_N -> "L3-N"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.VOLTAGE_L1_L2 -> "L1-L2"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.VOLTAGE_L2_L3 -> "L2-L3"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.VOLTAGE_L3_L1 -> "L3-L1"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.CURRENT_L1 -> "Corriente L1"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.CURRENT_L2 -> "Corriente L2"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.CURRENT_L3 -> "Corriente L3"
    com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType.CURRENT_NEUTRAL -> "Corriente neutro"
}

fun com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin.MEASURED -> "Medido"
    com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin.ESTIMATED -> "Estimado"
    com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin.CALCULATED -> "Calculado"
    com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin.DECLARED_BY_CLIENT -> "Declarado por el cliente"
    com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin.NOT_VERIFIED -> "No verificado"
}

fun com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_LN -> "Tensión fase-neutro"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_L1_N -> "L1-N"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_L2_N -> "L2-N"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_L3_N -> "L3-N"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_L1_L2 -> "L1-L2"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_L2_L3 -> "L2-L3"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.INPUT_VOLTAGE_L3_L1 -> "L3-L1"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.PROTECTION_VOLTAGE_PHASE_GROUND -> "Fase-tierra"
    com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND -> "Neutro-tierra"
}

fun com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.LIGHTING -> "Iluminación"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.OUTLETS -> "Tomacorrientes"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.AIR_CONDITIONING -> "Aire acondicionado"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.KITCHEN -> "Cocina"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.OVEN -> "Horno"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.PUMP -> "Bomba"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.EXTERIOR -> "Exterior"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.GATE -> "Portón"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.WATER_HEATER -> "Termotanque"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.RESERVE -> "Reserva"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.UNIDENTIFIED -> "Sin identificar"
    com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.OTHER -> "Otro"
}

fun com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.B -> "B"
    com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.C -> "C"
    com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.D -> "D"
    com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN -> "No verificada"
}

fun com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.COMPLIANT -> "Acordes"
    com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.PARTIALLY_COMPLIANT -> "Parcialmente acordes"
    com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.INCORRECT_OR_MIXED -> "Incorrectos o mezclados"
    com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.UNKNOWN -> "No verificados"
}

fun com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.label(): String = when (this) {
    com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.APPARENT_PRESENCE -> "Presencia aparente"
    com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.REQUIRES_REVIEW -> "Requiere revisión"
    com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.NOT_VERIFIED -> "No verificado"
}

fun InspectionSection.label(): String = when (this) {
    InspectionSection.GENERAL -> "Datos generales"
    InspectionSection.PILLAR -> "Pilar y acometida"
    InspectionSection.MAIN_PANEL -> "Tablero principal"
    InspectionSection.FINDINGS -> "Hallazgos"
    InspectionSection.UNVERIFIED -> "Sectores no verificados"
    InspectionSection.VISUAL_COMPLEMENTARY -> "Observaciones y no verificados"
    InspectionSection.TECHNICAL_COMMENT -> "Observación técnica"
    InspectionSection.FINAL_REPORT -> "Informe final del cliente"
}

fun InspectionSectionStatus.label(): String = when (this) {
    InspectionSectionStatus.NOT_STARTED -> "No iniciada"
    InspectionSectionStatus.INCOMPLETE -> "Incompleta"
    InspectionSectionStatus.COMPLETE -> "Completa"
}
