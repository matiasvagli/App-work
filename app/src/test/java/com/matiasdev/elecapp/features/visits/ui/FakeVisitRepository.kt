package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeVisitRepository(
    initialVisits: List<Visit> = emptyList(),
) : VisitRepository {
    private val visits = MutableStateFlow(initialVisits)

    override fun observeActiveVisitsForClient(clientId: String): Flow<List<Visit>> {
        return visits.map { values ->
            values
                .filter { it.clientId == clientId && !it.isDeleted }
                .sortedBy { it.scheduledAt }
        }
    }

    override fun observeActiveVisitsInRange(startMillis: Long, endMillis: Long): Flow<List<Visit>> {
        return visits.map { values ->
            values
                .filter { !it.isDeleted && it.scheduledAt.toEpochMilli() >= startMillis && it.scheduledAt.toEpochMilli() < endMillis }
                .sortedBy { it.scheduledAt }
        }
    }

    override fun observeActiveVisitsFrom(startMillis: Long): Flow<List<Visit>> {
        return visits.map { values ->
            values
                .filter { !it.isDeleted && it.scheduledAt.toEpochMilli() >= startMillis }
                .sortedBy { it.scheduledAt }
        }
    }

    override fun observeActiveVisitById(id: String): Flow<Visit?> {
        return visits.map { values -> values.firstOrNull { it.id == id && !it.isDeleted } }
    }

    override fun observeNextFutureVisit(fromMillis: Long): Flow<Visit?> {
        return visits.map { values ->
            values
                .filter {
                    !it.isDeleted &&
                        it.scheduledAt.toEpochMilli() >= fromMillis &&
                        it.status !in listOf(VisitStatus.COMPLETED, VisitStatus.CANCELLED, VisitStatus.IN_PROGRESS)
                }
                .sortedBy { it.scheduledAt }
                .firstOrNull()
        }
    }

    override fun observeCurrentInProgressVisit(): Flow<Visit?> {
        return visits.map { values ->
            values
                .filter { !it.isDeleted && it.status == VisitStatus.IN_PROGRESS }
                .maxByOrNull { it.startedAt ?: it.updatedAt }
        }
    }

    override suspend fun findActiveById(id: String): Visit? {
        return visits.value.firstOrNull { it.id == id && !it.isDeleted }
    }

    override suspend fun findFutureSchedulable(fromMillis: Long): List<Visit> {
        return visits.value
            .filter {
                !it.isDeleted &&
                    it.scheduledAt.toEpochMilli() >= fromMillis &&
                    it.status !in listOf(VisitStatus.COMPLETED, VisitStatus.CANCELLED, VisitStatus.IN_PROGRESS)
            }
            .sortedBy { it.scheduledAt }
    }

    override suspend fun save(visit: Visit) {
        visits.value = visits.value.filterNot { it.id == visit.id }.plus(visit)
    }

    override suspend fun updateStatus(id: String, status: VisitStatus) {
        visits.value = visits.value.map { visit ->
            if (visit.id == id) visit.copy(status = status, updatedAt = Instant.now()) else visit
        }
    }

    override suspend fun startVisit(id: String) {
        val now = Instant.now()
        visits.value = visits.value.map { visit ->
            if (visit.id == id) visit.copy(status = VisitStatus.IN_PROGRESS, startedAt = now, updatedAt = now) else visit
        }
    }

    override suspend fun completeVisit(id: String, completionNotes: String?, pendingWorkNotes: String?) {
        val now = Instant.now()
        visits.value = visits.value.map { visit ->
            if (visit.id == id) {
                visit.copy(
                    status = VisitStatus.COMPLETED,
                    completedAt = now,
                    completionNotes = completionNotes?.trim()?.ifBlank { null },
                    pendingWorkNotes = pendingWorkNotes?.trim()?.ifBlank { null },
                    updatedAt = now,
                )
            } else {
                visit
            }
        }
    }

    override suspend fun softDelete(id: String) {
        visits.value = visits.value.map { visit ->
            if (visit.id == id) visit.copy(isDeleted = true, updatedAt = Instant.now()) else visit
        }
    }

    fun currentVisits(): List<Visit> = visits.value
}
