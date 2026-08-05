package com.matiasdev.elecapp.features.visits.data

import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import kotlinx.coroutines.flow.Flow

interface VisitRepository {
    fun observeActiveVisitsForClient(clientId: String): Flow<List<Visit>>

    fun observeActiveVisitsInRange(startMillis: Long, endMillis: Long): Flow<List<Visit>>

    fun observeActiveVisitsFrom(startMillis: Long): Flow<List<Visit>>

    fun observeActiveVisitById(id: String): Flow<Visit?>

    fun observeNextFutureVisit(fromMillis: Long): Flow<Visit?>

    fun observeCurrentInProgressVisit(): Flow<Visit?>

    suspend fun findActiveById(id: String): Visit?

    suspend fun findFutureSchedulable(fromMillis: Long): List<Visit>

    suspend fun save(visit: Visit)

    suspend fun updateStatus(id: String, status: VisitStatus)

    suspend fun startVisit(id: String)

    suspend fun completeVisit(id: String, completionNotes: String?, pendingWorkNotes: String?)

    suspend fun softDelete(id: String)
}
