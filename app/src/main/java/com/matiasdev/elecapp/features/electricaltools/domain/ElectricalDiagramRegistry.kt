package com.matiasdev.elecapp.features.electricaltools.domain

import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.doorbellAndLockDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.generalDistributionDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.motionSensorDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.photocellDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.protectionsDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.threeWaySwitchDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.diagrams.waterPumpDiagram

object ElectricalDiagramRegistry {

    val allDiagrams: List<ElectricalDiagram> = listOf(
        generalDistributionDiagram,
        protectionsDiagram,
        threeWaySwitchDiagram,
        photocellDiagram,
        motionSensorDiagram,
        doorbellAndLockDiagram,
        waterPumpDiagram,
    )

    fun getById(id: String): ElectricalDiagram? =
        allDiagrams.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun getByType(type: DiagramType): ElectricalDiagram =
        allDiagrams.first { it.type == type }
}
