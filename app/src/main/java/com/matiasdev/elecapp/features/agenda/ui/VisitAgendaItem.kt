package com.matiasdev.elecapp.features.agenda.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.visits.domain.Visit

data class VisitAgendaItem(
    val visit: Visit,
    val client: Client?,
    val inspectionStatus: InspectionStatus? = null,
) {
    val clientName: String = client?.fullName ?: "Cliente no encontrado"
    val location: String = listOfNotNull(client?.address, client?.locality)
        .filter(String::isNotBlank)
        .joinToString(", ")
}
