package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgressCalculator
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurement
import com.matiasdev.elecapp.features.inspections.domain.PillarInspection
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeInspectionRepository : InspectionRepository {
    private val inspections = MutableStateFlow<List<ElectricalInspection>>(emptyList())
    private val pillars = MutableStateFlow<List<PillarInspection>>(emptyList())
    private val pillarMeasurements = MutableStateFlow<List<PillarMeasurement>>(emptyList())
    private val panels = MutableStateFlow<List<MainPanelInspection>>(emptyList())
    private val mainPanelMeasurements = MutableStateFlow<List<MainPanelMeasurement>>(emptyList())
    private val mainPanelCircuits = MutableStateFlow<List<MainPanelCircuit>>(emptyList())
    private val findings = MutableStateFlow<List<InspectionFinding>>(emptyList())
    private val unverifiedItems = MutableStateFlow<List<InspectionUnverifiedItem>>(emptyList())
    private val aggregateVersion = MutableStateFlow(0)

    override fun observeInspectionList(status: InspectionStatus?, query: String): Flow<List<InspectionListItem>> {
        return inspections.map { values ->
            values
                .filter { !it.isDeleted && (status == null || it.status == status) }
                .filter { inspection ->
                    val needle = query.trim()
                    needle.isBlank() ||
                        listOf(
                            inspection.clientNameSnapshot,
                            inspection.addressSnapshot,
                            inspection.localitySnapshot,
                            inspection.visitReasonSnapshot,
                        ).any { it.contains(needle, ignoreCase = true) }
                }
                .sortedByDescending { it.startedAt }
                .mapNotNull { inspection ->
                    aggregateFor(inspection)?.let { aggregate ->
                        InspectionListItem(inspection, null, InspectionProgressCalculator.calculate(aggregate))
                    }
                }
        }
    }

    override fun observeDraftInspectionCount(): Flow<Int> {
        return inspections.map { values -> values.count { !it.isDeleted && it.status == InspectionStatus.DRAFT } }
    }

    override fun observeActiveInspectionForVisit(visitId: String): Flow<ElectricalInspection?> {
        return inspections.map { values -> values.firstOrNull { it.visitId == visitId && !it.isDeleted } }
    }

    override fun observeAggregate(inspectionId: String): Flow<InspectionAggregate?> {
        return kotlinx.coroutines.flow.combine(inspections, aggregateVersion) { values, _ ->
            aggregateFor(values.firstOrNull { it.id == inspectionId && !it.isDeleted })
        }
    }

    override suspend fun findAggregate(inspectionId: String): InspectionAggregate? {
        return aggregateFor(inspections.value.firstOrNull { it.id == inspectionId && !it.isDeleted })
    }

    override suspend fun findActiveInspectionForVisit(visitId: String): ElectricalInspection? {
        return inspections.value.firstOrNull { it.visitId == visitId && !it.isDeleted }
    }

    override suspend fun startOrGetInspection(
        visit: Visit,
        client: Client,
        scope: InspectionScope,
    ): ElectricalInspection {
        findActiveInspectionForVisit(visit.id)?.let { return it }
        val now = Instant.parse("2026-08-04T12:00:00Z")
        val inspection = ElectricalInspection(
            id = UUID.randomUUID().toString(),
            visitId = visit.id,
            status = InspectionStatus.DRAFT,
            scope = scope,
            inspectionType = InspectionType.VISUAL,
            generalCondition = GeneralCondition.NOT_ASSESSED,
            supplyType = SupplyType.UNKNOWN,
            propertyType = if (scope == InspectionScope.VISUAL_INSPECTION) PropertyType.UNKNOWN else PropertyType.HOUSE,
            reviewReason = visit.reason.takeIf(String::isNotBlank).takeIf { scope == InspectionScope.VISUAL_INSPECTION },
            reviewedElement = null,
            taskDescription = null,
            visitReasonSnapshot = visit.reason,
            clientNameSnapshot = client.fullName,
            addressSnapshot = client.address.orEmpty(),
            localitySnapshot = client.locality.orEmpty(),
            technicianName = null,
            accessLimitations = null,
            originalTechnicalComment = null,
            finalClientReport = null,
            startedAt = now,
            completedAt = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
        saveInspection(inspection)
        return inspection
    }

    override suspend fun saveInspection(inspection: ElectricalInspection) {
        inspections.value = inspections.value.filterNot { it.id == inspection.id }.plus(inspection)
        touchAggregate()
    }

    override suspend fun savePillar(pillar: PillarInspection) {
        pillars.value = pillars.value.filterNot { it.inspectionId == pillar.inspectionId }.plus(pillar)
        touchAggregate()
    }

    override suspend fun savePillarMeasurement(measurement: PillarMeasurement) {
        pillarMeasurements.value = pillarMeasurements.value.filterNot { it.id == measurement.id }.plus(measurement)
        touchAggregate()
    }

    override suspend fun softDeletePillarMeasurement(id: String) {
        pillarMeasurements.value = pillarMeasurements.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
        touchAggregate()
    }

    override suspend fun saveMainPanel(mainPanel: MainPanelInspection) {
        panels.value = panels.value.filterNot { it.inspectionId == mainPanel.inspectionId }.plus(mainPanel)
        touchAggregate()
    }

    override suspend fun saveMainPanelMeasurement(measurement: MainPanelMeasurement) {
        mainPanelMeasurements.value = mainPanelMeasurements.value.filterNot { it.id == measurement.id }.plus(measurement)
        touchAggregate()
    }

    override suspend fun softDeleteMainPanelMeasurement(id: String) {
        mainPanelMeasurements.value = mainPanelMeasurements.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
        touchAggregate()
    }

    override suspend fun saveMainPanelCircuit(circuit: MainPanelCircuit) {
        mainPanelCircuits.value = mainPanelCircuits.value.filterNot { it.id == circuit.id }.plus(circuit)
        touchAggregate()
    }

    override suspend fun softDeleteMainPanelCircuit(id: String) {
        mainPanelCircuits.value = mainPanelCircuits.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
        touchAggregate()
    }

    override suspend fun saveFinding(finding: InspectionFinding) {
        findings.value = findings.value.filterNot { it.id == finding.id }.plus(finding)
        touchAggregate()
    }

    override suspend fun softDeleteFinding(id: String) {
        findings.value = findings.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
        touchAggregate()
    }

    override suspend fun saveUnverifiedItems(inspectionId: String, items: List<InspectionUnverifiedItem>) {
        unverifiedItems.value = unverifiedItems.value
            .map { if (it.inspectionId == inspectionId) it.copy(isDeleted = true) else it }
            .plus(items)
        touchAggregate()
    }

    private fun touchAggregate() {
        aggregateVersion.value = aggregateVersion.value + 1
    }

    private fun aggregateFor(inspection: ElectricalInspection?): InspectionAggregate? {
        if (inspection == null) return null
        return InspectionAggregate(
            inspection = inspection,
            pillar = pillars.value.firstOrNull { it.inspectionId == inspection.id },
            pillarMeasurements = pillarMeasurements.value.filter { it.inspectionId == inspection.id && !it.isDeleted }.sortedWith(compareBy({ it.sortOrder }, { it.createdAt })),
            mainPanel = panels.value.firstOrNull { it.inspectionId == inspection.id },
            mainPanelMeasurements = mainPanelMeasurements.value.filter { it.inspectionId == inspection.id && !it.isDeleted }.sortedWith(compareBy({ it.sortOrder }, { it.createdAt })),
            mainPanelCircuits = mainPanelCircuits.value.filter { it.inspectionId == inspection.id && !it.isDeleted }.sortedWith(compareBy({ it.sortOrder }, { it.createdAt })),
            findings = findings.value.filter { it.inspectionId == inspection.id && !it.isDeleted }.sortedBy { it.sortOrder },
            unverifiedItems = unverifiedItems.value.filter { it.inspectionId == inspection.id && !it.isDeleted },
        )
    }
}
