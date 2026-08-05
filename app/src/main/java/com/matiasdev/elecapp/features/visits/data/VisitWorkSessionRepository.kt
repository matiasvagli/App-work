package com.matiasdev.elecapp.features.visits.data

import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface VisitWorkSessionRepository {
    fun observeByVisitId(visitId: String): Flow<List<VisitWorkSession>>

    fun observeActiveByVisitId(visitId: String): Flow<VisitWorkSession?>

    fun observeAllActive(): Flow<List<VisitWorkSession>>

    suspend fun getSessionsForVisit(visitId: String): List<VisitWorkSession>

    suspend fun startVisitWork(visitId: String)

    suspend fun pauseWork(visitId: String)

    suspend fun resumeWork(visitId: String)

    suspend fun completeVisitWork(visitId: String, completionNotes: String?, pendingWorkNotes: String?)

    suspend fun addManualSession(
        visitId: String,
        startedAt: Instant,
        endedAt: Instant,
        notes: String?,
        allowOverlap: Boolean = false,
        allowFuture: Boolean = false,
    )

    suspend fun updateNotes(id: String, notes: String?)
}
