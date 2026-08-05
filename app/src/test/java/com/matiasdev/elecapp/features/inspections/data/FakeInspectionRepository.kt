package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgressCalculator
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
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
    private val panels = MutableStateFlow<List<MainPanelInspection>>(emptyList())
    private val findings = MutableStateFlow<List<InspectionFinding>>(emptyList())
    private val unverifiedItems = MutableStateFlow<List<InspectionUnverifiedItem>>(emptyList())

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
        return inspections.map { values -> aggregateFor(values.firstOrNull { it.id == inspectionId && !it.isDeleted }) }
    }

    override suspend fun findAggregate(inspectionId: String): InspectionAggregate? {
        return aggregateFor(inspections.value.firstOrNull { it.id == inspectionId && !it.isDeleted })
    }

    override suspend fun findActiveInspectionForVisit(visitId: String): ElectricalInspection? {
        return inspections.value.firstOrNull { it.visitId == visitId && !it.isDeleted }
    }

    override suspend fun startOrGetInspection(visit: Visit, client: Client): ElectricalInspection {
        findActiveInspectionForVisit(visit.id)?.let { return it }
        val now = Instant.parse("2026-08-04T12:00:00Z")
        val inspection = ElectricalInspection(
            id = UUID.randomUUID().toString(),
            visitId = visit.id,
            status = InspectionStatus.DRAFT,
            inspectionType = InspectionType.VISUAL,
            generalCondition = GeneralCondition.NOT_ASSESSED,
            supplyType = SupplyType.UNKNOWN,
            propertyType = PropertyType.HOUSE,
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
    }

    override suspend fun savePillar(pillar: PillarInspection) {
        pillars.value = pillars.value.filterNot { it.inspectionId == pillar.inspectionId }.plus(pillar)
    }

    override suspend fun saveMainPanel(mainPanel: MainPanelInspection) {
        panels.value = panels.value.filterNot { it.inspectionId == mainPanel.inspectionId }.plus(mainPanel)
    }

    override suspend fun saveFinding(finding: InspectionFinding) {
        findings.value = findings.value.filterNot { it.id == finding.id }.plus(finding)
    }

    override suspend fun softDeleteFinding(id: String) {
        findings.value = findings.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
    }

    override suspend fun saveUnverifiedItems(inspectionId: String, items: List<InspectionUnverifiedItem>) {
        unverifiedItems.value = unverifiedItems.value
            .map { if (it.inspectionId == inspectionId) it.copy(isDeleted = true) else it }
            .plus(items)
    }

    private fun aggregateFor(inspection: ElectricalInspection?): InspectionAggregate? {
        if (inspection == null) return null
        return InspectionAggregate(
            inspection = inspection,
            pillar = pillars.value.firstOrNull { it.inspectionId == inspection.id },
            mainPanel = panels.value.firstOrNull { it.inspectionId == inspection.id },
            findings = findings.value.filter { it.inspectionId == inspection.id && !it.isDeleted }.sortedBy { it.sortOrder },
            unverifiedItems = unverifiedItems.value.filter { it.inspectionId == inspection.id && !it.isDeleted },
        )
    }
}
