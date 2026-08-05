package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeVisitWorkSessionRepository(
    initialSessions: List<VisitWorkSession> = emptyList(),
    private val visitRepository: FakeVisitRepository? = null,
    private val timeProvider: TimeProvider = TimeProvider { Instant.parse("2026-01-01T00:00:00Z") },
) : VisitWorkSessionRepository {
    private val sessions = MutableStateFlow(initialSessions)

    override fun observeByVisitId(visitId: String): Flow<List<VisitWorkSession>> {
        return sessions.map { values -> values.filter { it.visitId == visitId && !it.isDeleted }.sortedBy { it.startedAt } }
    }

    override fun observeActiveByVisitId(visitId: String): Flow<VisitWorkSession?> {
        return sessions.map { values -> values.firstOrNull { it.visitId == visitId && it.status == VisitWorkSessionStatus.RUNNING && !it.isDeleted } }
    }

    override fun observeAllActive(): Flow<List<VisitWorkSession>> {
        return sessions.map { values -> values.filterNot { it.isDeleted } }
    }

    override suspend fun getSessionsForVisit(visitId: String): List<VisitWorkSession> {
        return sessions.value.filter { it.visitId == visitId && !it.isDeleted }.sortedBy { it.startedAt }
    }

    override suspend fun startVisitWork(visitId: String) {
        visitRepository?.startVisit(visitId)
        if (active(visitId) == null) sessions.value = sessions.value + session(visitId, VisitWorkSessionStatus.RUNNING)
    }

    override suspend fun pauseWork(visitId: String) {
        closeActive(visitId, VisitWorkSessionStatus.PAUSED)
    }

    override suspend fun resumeWork(visitId: String) {
        if (active(visitId) == null) sessions.value = sessions.value + session(visitId, VisitWorkSessionStatus.RUNNING)
    }

    override suspend fun completeVisitWork(visitId: String, completionNotes: String?, pendingWorkNotes: String?) {
        closeActive(visitId, VisitWorkSessionStatus.COMPLETED)
        visitRepository?.completeVisit(visitId, completionNotes, pendingWorkNotes)
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
        require(allowOverlap || !VisitWorkSessionDurations.overlaps(startedAt, endedAt, sessions.value)) {
            "La sesión se superpone con otro registro"
        }
        val now = timeProvider.now()
        require(allowFuture || (startedAt <= now && endedAt <= now)) { "La sesión manual no puede estar en el futuro" }
        sessions.value = sessions.value + session(visitId, VisitWorkSessionStatus.COMPLETED, startedAt, endedAt, notes)
    }

    override suspend fun updateNotes(id: String, notes: String?) {
        val now = timeProvider.now()
        sessions.value = sessions.value.map { if (it.id == id) it.copy(notes = notes, updatedAt = now) else it }
    }

    fun currentSessions(): List<VisitWorkSession> = sessions.value

    private fun active(visitId: String): VisitWorkSession? {
        return sessions.value.firstOrNull { it.visitId == visitId && it.status == VisitWorkSessionStatus.RUNNING && !it.isDeleted }
    }

    private fun closeActive(visitId: String, status: VisitWorkSessionStatus) {
        val now = timeProvider.now()
        sessions.value = sessions.value.map {
            if (it.visitId == visitId && it.status == VisitWorkSessionStatus.RUNNING) it.copy(endedAt = now, status = status, updatedAt = now) else it
        }
    }

    private fun session(
        visitId: String,
        status: VisitWorkSessionStatus,
        startedAt: Instant = timeProvider.now(),
        endedAt: Instant? = null,
        notes: String? = null,
    ): VisitWorkSession {
        val now = timeProvider.now()
        return VisitWorkSession(UUID.randomUUID().toString(), visitId, startedAt, endedAt, status, notes, now, now, false)
    }
}
