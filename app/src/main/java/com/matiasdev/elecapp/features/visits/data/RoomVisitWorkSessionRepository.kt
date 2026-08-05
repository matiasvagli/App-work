package com.matiasdev.elecapp.features.visits.data

import androidx.room.withTransaction
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.data.AppDatabase
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomVisitWorkSessionRepository(
    private val database: AppDatabase,
    private val visitDao: VisitDao,
    private val sessionDao: VisitWorkSessionDao,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : VisitWorkSessionRepository {
    override fun observeByVisitId(visitId: String): Flow<List<VisitWorkSession>> {
        return sessionDao.observeByVisitId(visitId).map { it.map(VisitWorkSessionEntity::toDomain) }
    }

    override fun observeActiveByVisitId(visitId: String): Flow<VisitWorkSession?> {
        return sessionDao.observeActiveByVisitId(visitId).map { it?.toDomain() }
    }

    override fun observeAllActive(): Flow<List<VisitWorkSession>> {
        return sessionDao.observeAllActive().map { it.map(VisitWorkSessionEntity::toDomain) }
    }

    override suspend fun getSessionsForVisit(visitId: String): List<VisitWorkSession> {
        return sessionDao.getSessionsForVisit(visitId).map(VisitWorkSessionEntity::toDomain)
    }

    override suspend fun startVisitWork(visitId: String) {
        database.withTransaction {
            val visit = visitDao.findActiveById(visitId) ?: error("Visita no encontrada")
            val status = VisitStatus.valueOf(visit.status)
            if (status in listOf(VisitStatus.COMPLETED, VisitStatus.CANCELLED)) {
                error("La visita no puede iniciarse")
            }
            val now = timeProvider.now()
            if (status != VisitStatus.IN_PROGRESS || visit.startedAt == null) {
                visitDao.startVisit(visitId, now.toEpochMilli(), now.toEpochMilli())
            }
            if (sessionDao.getActiveByVisitId(visitId) == null) {
                sessionDao.insert(newSession(visitId, now, VisitWorkSessionStatus.RUNNING, endedAt = null, notes = null))
            }
        }
    }

    override suspend fun pauseWork(visitId: String) {
        database.withTransaction {
            if (sessionDao.getActiveByVisitId(visitId) != null) {
                val now = timeProvider.now().toEpochMilli()
                sessionDao.closeActiveSession(visitId, now, VisitWorkSessionStatus.PAUSED.name, now)
            }
        }
    }

    override suspend fun resumeWork(visitId: String) {
        database.withTransaction {
            val visit = visitDao.findActiveById(visitId) ?: error("Visita no encontrada")
            if (VisitStatus.valueOf(visit.status) != VisitStatus.IN_PROGRESS) error("La visita no está en curso")
            if (sessionDao.getActiveByVisitId(visitId) == null) {
                val now = timeProvider.now()
                sessionDao.insert(newSession(visitId, now, VisitWorkSessionStatus.RUNNING, endedAt = null, notes = null))
            }
        }
    }

    override suspend fun completeVisitWork(visitId: String, completionNotes: String?, pendingWorkNotes: String?) {
        database.withTransaction {
            val visit = visitDao.findActiveById(visitId) ?: error("Visita no encontrada")
            if (VisitStatus.valueOf(visit.status) != VisitStatus.IN_PROGRESS) error("La visita no está en curso")
            val now = timeProvider.now().toEpochMilli()
            if (sessionDao.getActiveByVisitId(visitId) != null) {
                sessionDao.closeActiveSession(visitId, now, VisitWorkSessionStatus.COMPLETED.name, now)
            }
            visitDao.completeVisit(
                id = visitId,
                completedAt = now,
                completionNotes = completionNotes?.trim()?.ifBlank { null },
                pendingWorkNotes = pendingWorkNotes?.trim()?.ifBlank { null },
                updatedAt = now,
            )
        }
    }

    override suspend fun addManualSession(
        visitId: String,
        startedAt: Instant,
        endedAt: Instant,
        notes: String?,
        allowOverlap: Boolean,
        allowFuture: Boolean,
    ) {
        require(endedAt > startedAt) { "La hora de fin debe ser posterior al inicio" }
        val now = timeProvider.now()
        require(allowFuture || (startedAt <= now && endedAt <= now)) { "La sesión manual no puede estar en el futuro" }
        database.withTransaction {
            val sessions = sessionDao.getSessionsForVisit(visitId).map(VisitWorkSessionEntity::toDomain)
            require(allowOverlap || !VisitWorkSessionDurations.overlaps(startedAt, endedAt, sessions)) {
                "La sesión se superpone con otro registro"
            }
            sessionDao.insert(newSession(visitId, startedAt, VisitWorkSessionStatus.COMPLETED, endedAt, notes))
        }
    }

    override suspend fun updateNotes(id: String, notes: String?) {
        sessionDao.updateNotes(id, notes?.trim()?.ifBlank { null }, timeProvider.now().toEpochMilli())
    }

    private fun newSession(
        visitId: String,
        startedAt: Instant,
        status: VisitWorkSessionStatus,
        endedAt: Instant?,
        notes: String?,
    ): VisitWorkSessionEntity {
        val now = timeProvider.now()
        return VisitWorkSessionEntity(
            id = UUID.randomUUID().toString(),
            visitId = visitId,
            startedAt = startedAt.toEpochMilli(),
            endedAt = endedAt?.toEpochMilli(),
            status = status.name,
            notes = notes?.trim()?.ifBlank { null },
            createdAt = now.toEpochMilli(),
            updatedAt = now.toEpochMilli(),
            isDeleted = false,
        )
    }
}
