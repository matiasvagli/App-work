package com.matiasdev.elecapp.features.visits.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query(
        """
        SELECT * FROM visits
        WHERE client_id = :clientId AND is_deleted = 0
        ORDER BY scheduled_at ASC
        """,
    )
    fun observeActiveVisitsForClient(clientId: String): Flow<List<VisitEntity>>

    @Query(
        """
        SELECT * FROM visits
        WHERE is_deleted = 0 AND scheduled_at >= :startMillis AND scheduled_at < :endMillis
        ORDER BY scheduled_at ASC
        """,
    )
    fun observeActiveVisitsInRange(startMillis: Long, endMillis: Long): Flow<List<VisitEntity>>

    @Query(
        """
        SELECT * FROM visits
        WHERE is_deleted = 0 AND scheduled_at >= :startMillis
        ORDER BY scheduled_at ASC
        """,
    )
    fun observeActiveVisitsFrom(startMillis: Long): Flow<List<VisitEntity>>

    @Query(
        """
        SELECT * FROM visits
        WHERE is_deleted = 0
            AND scheduled_at >= :fromMillis
            AND status NOT IN ('COMPLETED', 'CANCELLED', 'IN_PROGRESS')
        ORDER BY scheduled_at ASC
        LIMIT 1
        """,
    )
    fun observeNextFutureVisit(fromMillis: Long): Flow<VisitEntity?>

    @Query(
        """
        SELECT * FROM visits
        WHERE is_deleted = 0 AND status = 'IN_PROGRESS'
        ORDER BY started_at DESC, updated_at DESC
        LIMIT 1
        """,
    )
    fun observeCurrentInProgressVisit(): Flow<VisitEntity?>

    @Query("SELECT * FROM visits WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeActiveVisitById(id: String): Flow<VisitEntity?>

    @Query("SELECT * FROM visits WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun findActiveById(id: String): VisitEntity?

    @Query(
        """
        SELECT * FROM visits
        WHERE is_deleted = 0
            AND scheduled_at >= :fromMillis
            AND status NOT IN ('COMPLETED', 'CANCELLED', 'IN_PROGRESS')
        ORDER BY scheduled_at ASC
        """,
    )
    suspend fun findFutureSchedulable(fromMillis: Long): List<VisitEntity>

    @Upsert
    suspend fun upsert(visit: VisitEntity)

    @Query("UPDATE visits SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE visits SET status = 'IN_PROGRESS', started_at = :startedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun startVisit(id: String, startedAt: Long, updatedAt: Long)

    @Query(
        """
        UPDATE visits
        SET status = 'COMPLETED',
            completed_at = :completedAt,
            completion_notes = :completionNotes,
            pending_work_notes = :pendingWorkNotes,
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun completeVisit(
        id: String,
        completedAt: Long,
        completionNotes: String?,
        pendingWorkNotes: String?,
        updatedAt: Long,
    )

    @Query("UPDATE visits SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)
}
