package com.matiasdev.elecapp.features.electricaltools.domain

enum class DiagramType {
    GENERAL_DISTRIBUTION,
    PHOTOCELL,
    MOTION_SENSOR,
    THREE_WAY_SWITCH,
    DOORBELL_AND_ELECTRIC_LOCK,
    CIRCUIT_BREAKERS_AND_RCD,
    WATER_PUMP_TANK_CISTERN,
}

data class WiringConnectionStep(
    val stepNumber: Int,
    val title: String,
    val wireName: String,
    val wireColorHex: Long,
    val fromTerminal: String,
    val toTerminal: String,
    val description: String,
)

data class ComponentTerminal(
    val code: String,
    val name: String,
    val functionDescription: String,
)

data class ElectricalDiagram(
    val id: String,
    val type: DiagramType,
    val title: String,
    val subtitle: String,
    val category: String,
    val badgeText: String,
    val requiredVoltage: String,
    val safetyVoltageNote: String?,
    val requiredComponents: List<String>,
    val terminalLegend: List<ComponentTerminal>,
    val stepByStepGuide: List<WiringConnectionStep>,
    val practicalTips: List<String>,
    val securityWarning: String,
    val aeaReference: String,
)
