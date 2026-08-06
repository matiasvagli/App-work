package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class RoomInspectionRepository(
    private val dao: InspectionDao,
) : InspectionRepository {
    override fun observeInspectionList(status: InspectionStatus?, query: String): Flow<List<InspectionListItem>> {
        return dao.observeInspectionList(status?.name, query.trim()).map { items ->
            items.map(InspectionListItemEntity::toDomain)
        }
    }

    override fun observeDraftInspectionCount(): Flow<Int> = dao.observeDraftInspectionCount()

    override fun observeActiveInspectionForVisit(visitId: String): Flow<ElectricalInspection?> {
        return dao.observeActiveInspectionForVisit(visitId).map { it?.toDomain() }
    }

    override fun observeAggregate(inspectionId: String): Flow<InspectionAggregate?> {
        return dao.observeActiveInspectionById(inspectionId).flatMapLatest { inspection ->
            if (inspection == null) {
                flowOf(null)
            } else {
                combine(
                    dao.observePillar(inspectionId),
                    dao.observeMainPanel(inspectionId),
                    dao.observeFindings(inspectionId),
                    dao.observeUnverifiedItems(inspectionId),
                ) { pillar, panel, findings, unverified ->
                    InspectionAggregate(
                        inspection = inspection.toDomain(),
                        pillar = pillar?.toDomain(),
                        mainPanel = panel?.toDomain(),
                        findings = findings.map(InspectionFindingEntity::toDomain),
                        unverifiedItems = unverified.map(InspectionUnverifiedItemEntity::toDomain),
                    )
                }
            }
        }
    }

    override suspend fun findAggregate(inspectionId: String): InspectionAggregate? {
        val inspection = dao.findActiveInspectionById(inspectionId)?.toDomain() ?: return null
        return InspectionAggregate(
            inspection = inspection,
            pillar = dao.findPillar(inspectionId)?.toDomain(),
            mainPanel = dao.findMainPanel(inspectionId)?.toDomain(),
            findings = dao.findFindings(inspectionId).map(InspectionFindingEntity::toDomain),
            unverifiedItems = dao.findUnverifiedItems(inspectionId).map(InspectionUnverifiedItemEntity::toDomain),
        )
    }

    override suspend fun findActiveInspectionForVisit(visitId: String): ElectricalInspection? {
        return dao.findActiveInspectionForVisit(visitId)?.toDomain()
    }

    override suspend fun startOrGetInspection(
        visit: Visit,
        client: Client,
        scope: InspectionScope,
    ): ElectricalInspection {
        dao.findActiveInspectionForVisit(visit.id)?.let { return it.toDomain() }
        val now = Instant.now()
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
        dao.upsertInspection(inspection.toEntity())
        return inspection
    }

    override suspend fun saveInspection(inspection: ElectricalInspection) {
        dao.upsertInspection(inspection.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun savePillar(pillar: PillarInspection) {
        dao.upsertPillar(pillar.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun saveMainPanel(mainPanel: MainPanelInspection) {
        dao.upsertMainPanel(mainPanel.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun saveFinding(finding: InspectionFinding) {
        dao.upsertFinding(finding.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun softDeleteFinding(id: String) {
        dao.softDeleteFinding(id, Instant.now().toEpochMilli())
    }

    override suspend fun saveUnverifiedItems(inspectionId: String, items: List<InspectionUnverifiedItem>) {
        val now = Instant.now()
        dao.replaceUnverifiedItems(
            inspectionId = inspectionId,
            updatedAt = now.toEpochMilli(),
            items = items.map { it.copy(updatedAt = now).toEntity() },
        )
    }
}
