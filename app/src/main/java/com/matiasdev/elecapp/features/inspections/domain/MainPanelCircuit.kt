package com.matiasdev.elecapp.features.inspections.domain

import java.time.Instant

data class MainPanelCircuit(
    val id: String,
    val inspectionId: String,
    val sortOrder: Int,
    val destination: CircuitDestination,
    val destinationOther: String?,
    val breakerAmps: Int?,
    val breakerOtherAmps: Int?,
    val breakerCurve: BreakerCurve,
    val conductorSectionMm2: Double?,
    val conductorOtherSectionMm2: Double?,
    val conductorMaterial: ConductorMaterial,
    val conductorMaterialOther: String?,
    val consumptionAmps: Double?,
    val consumptionOrigin: MeasurementOrigin,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)

fun List<MainPanelCircuit>.activeCircuitsInReportOrder(): List<MainPanelCircuit> {
    return filterNot(MainPanelCircuit::isDeleted)
        .sortedWith(compareBy({ it.sortOrder }, { it.createdAt }))
}

fun List<MainPanelCircuit>.reportableCircuitsInReportOrder(): List<MainPanelCircuit> {
    return activeCircuitsInReportOrder().filter(MainPanelCircuit::hasReportContent)
}

fun MainPanelCircuit.hasReportContent(): Boolean {
    return destination != CircuitDestination.UNIDENTIFIED ||
        !destinationOther.isNullOrBlank() ||
        breakerAmps != null ||
        breakerOtherAmps?.let { it > 0 } == true ||
        breakerCurve != BreakerCurve.UNKNOWN ||
        conductorSectionMm2 != null ||
        conductorOtherSectionMm2?.let { it > 0.0 } == true ||
        conductorMaterial != ConductorMaterial.UNKNOWN ||
        consumptionAmps != null ||
        consumptionOrigin != MeasurementOrigin.NOT_VERIFIED ||
        !notes.isNullOrBlank()
}

fun MainPanelCircuit.reportCircuitName(index: Int): String {
    return when {
        destination in circuitDestinationsWithFreeText && !destinationOther.isNullOrBlank() ->
            "Circuito ${index + 1} ($destinationOther)"
        destination == CircuitDestination.UNIDENTIFIED -> "Circuito ${index + 1} sin identificar"
        else -> "Circuito ${index + 1} (${destination.reportLabel().lowercase()})"
    }
}

/** Destinos que se describen con texto libre en `destinationOther`. */
val circuitDestinationsWithFreeText = setOf(CircuitDestination.OTHER, CircuitDestination.PARTIAL)

private fun CircuitDestination.reportLabel(): String = when (this) {
    CircuitDestination.GENERAL -> "General, toda la casa"
    CircuitDestination.PARTIAL -> "Parcial"
    CircuitDestination.LIGHTING -> "Iluminación"
    CircuitDestination.OUTLETS -> "Tomacorrientes"
    CircuitDestination.AIR_CONDITIONING -> "Aire acondicionado"
    CircuitDestination.KITCHEN -> "Cocina"
    CircuitDestination.OVEN -> "Horno"
    CircuitDestination.PUMP -> "Bomba"
    CircuitDestination.EXTERIOR -> "Exterior"
    CircuitDestination.GATE -> "Portón"
    CircuitDestination.WATER_HEATER -> "Termotanque"
    CircuitDestination.RESERVE -> "Reserva"
    CircuitDestination.UNIDENTIFIED -> "Sin identificar"
    CircuitDestination.OTHER -> "Otro"
}
