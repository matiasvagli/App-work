package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.GroundingInspection
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.visits.domain.Visit
import kotlinx.coroutines.flow.Flow

interface InspectionRepository {
    fun observeInspectionList(status: InspectionStatus?, query: String): Flow<List<InspectionListItem>>

    fun observeDraftInspectionCount(): Flow<Int>

    fun observeActiveInspectionForVisit(visitId: String): Flow<ElectricalInspection?>

    fun observeAggregate(inspectionId: String): Flow<InspectionAggregate?>

    suspend fun findAggregate(inspectionId: String): InspectionAggregate?

    suspend fun findActiveInspectionForVisit(visitId: String): ElectricalInspection?

    suspend fun startOrGetInspection(visit: Visit, client: Client): ElectricalInspection {
        return startOrGetInspection(visit, client, InspectionScope.GENERAL_ASSESSMENT)
    }

    suspend fun startOrGetInspection(
        visit: Visit,
        client: Client,
        scope: InspectionScope,
    ): ElectricalInspection

    suspend fun saveInspection(inspection: ElectricalInspection)

    suspend fun savePillar(pillar: PillarInspection)

    suspend fun savePillarMeasurement(measurement: PillarMeasurement)

    suspend fun softDeletePillarMeasurement(id: String)

    suspend fun saveMainPanel(mainPanel: MainPanelInspection)

    suspend fun saveGrounding(grounding: GroundingInspection)

    suspend fun saveMainPanelMeasurement(measurement: MainPanelMeasurement)

    suspend fun softDeleteMainPanelMeasurement(id: String)

    suspend fun saveMainPanelCircuit(circuit: MainPanelCircuit)

    suspend fun softDeleteMainPanelCircuit(id: String)

    suspend fun saveFinding(finding: InspectionFinding)

    suspend fun softDeleteFinding(id: String)

    suspend fun saveUnverifiedItems(inspectionId: String, items: List<InspectionUnverifiedItem>)
}
