package com.matiasdev.elecapp.features.visits.data

import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.WorkHistoryItem
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomVisitRepository(
    private val visitDao: VisitDao,
) : VisitRepository {
    override fun observeActiveVisitsForClient(clientId: String): Flow<List<Visit>> {
        return visitDao.observeActiveVisitsForClient(clientId)
            .map { visits -> visits.map(VisitEntity::toDomain) }
    }

    override fun observeActiveVisitsInRange(startMillis: Long, endMillis: Long): Flow<List<Visit>> {
        return visitDao.observeActiveVisitsInRange(startMillis, endMillis)
            .map { visits -> visits.map(VisitEntity::toDomain) }
    }

    override fun observeActiveVisitsFrom(startMillis: Long): Flow<List<Visit>> {
        return visitDao.observeActiveVisitsFrom(startMillis)
            .map { visits -> visits.map(VisitEntity::toDomain) }
    }

    override fun observeActiveVisitById(id: String): Flow<Visit?> {
        return visitDao.observeActiveVisitById(id).map { it?.toDomain() }
    }

    override fun observeNextFutureVisit(fromMillis: Long): Flow<Visit?> {
        return visitDao.observeNextFutureVisit(fromMillis).map { it?.toDomain() }
    }

    override fun observeCurrentInProgressVisit(): Flow<Visit?> {
        return visitDao.observeCurrentInProgressVisit().map { it?.toDomain() }
    }

    override fun observeCompletedWorkHistory(): Flow<List<WorkHistoryItem>> {
        return visitDao.observeCompletedWorkHistory()
            .map { items -> items.map(WorkHistoryItemEntity::toDomain) }
    }

    override fun observeCompletedWorkHistoryForClient(clientId: String): Flow<List<WorkHistoryItem>> {
        return visitDao.observeCompletedWorkHistoryForClient(clientId)
            .map { items -> items.map(WorkHistoryItemEntity::toDomain) }
    }

    override suspend fun findActiveById(id: String): Visit? {
        return visitDao.findActiveById(id)?.toDomain()
    }

    override suspend fun findFutureSchedulable(fromMillis: Long): List<Visit> {
        return visitDao.findFutureSchedulable(fromMillis).map(VisitEntity::toDomain)
    }

    override suspend fun save(visit: Visit) {
        visitDao.upsert(visit.toEntity())
    }

    override suspend fun updateStatus(id: String, status: VisitStatus) {
        visitDao.updateStatus(id, status.name, Instant.now().toEpochMilli())
    }

    override suspend fun startVisit(id: String) {
        val now = Instant.now().toEpochMilli()
        visitDao.startVisit(id, startedAt = now, updatedAt = now)
    }

    override suspend fun completeVisit(id: String, completionNotes: String?, pendingWorkNotes: String?) {
        val now = Instant.now().toEpochMilli()
        visitDao.completeVisit(
            id = id,
            completedAt = now,
            completionNotes = completionNotes?.trim()?.ifBlank { null },
            pendingWorkNotes = pendingWorkNotes?.trim()?.ifBlank { null },
            updatedAt = now,
        )
    }

    override suspend fun softDelete(id: String) {
        visitDao.softDelete(id, Instant.now().toEpochMilli())
    }
}
