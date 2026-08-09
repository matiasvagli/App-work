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

    @Query(
        """
        SELECT
            v.id AS visit_id,
            v.client_id AS client_id,
            c.full_name AS client_name,
            v.completed_at AS completed_at,
            v.reason AS reason,
            vc.work_type AS work_type,
            vc.work_performed AS work_description,
            CASE WHEN vc.technical_report_snapshot IS NOT NULL AND TRIM(vc.technical_report_snapshot) != '' THEN 1 ELSE 0 END AS has_technical_report,
            CASE WHEN vc.client_report IS NOT NULL AND TRIM(vc.client_report) != '' THEN 1 ELSE 0 END AS has_client_report,
            CASE
                WHEN v.started_at IS NULL THEN NULL
                WHEN ((v.completed_at - v.started_at) / 60000) < 0 THEN 0
                ELSE ((v.completed_at - v.started_at) / 60000)
            END AS duration_minutes
        FROM visits v
        INNER JOIN clients c ON c.id = v.client_id AND c.is_deleted = 0
        LEFT JOIN visit_completions vc ON vc.visit_id = v.id AND vc.is_deleted = 0
        WHERE v.is_deleted = 0
            AND v.status = 'COMPLETED'
            AND v.completed_at IS NOT NULL
        ORDER BY v.completed_at DESC
        """,
    )
    fun observeCompletedWorkHistory(): Flow<List<WorkHistoryItemEntity>>

    @Query(
        """
        SELECT
            v.id AS visit_id,
            v.client_id AS client_id,
            c.full_name AS client_name,
            v.completed_at AS completed_at,
            v.reason AS reason,
            vc.work_type AS work_type,
            vc.work_performed AS work_description,
            CASE WHEN vc.technical_report_snapshot IS NOT NULL AND TRIM(vc.technical_report_snapshot) != '' THEN 1 ELSE 0 END AS has_technical_report,
            CASE WHEN vc.client_report IS NOT NULL AND TRIM(vc.client_report) != '' THEN 1 ELSE 0 END AS has_client_report,
            CASE
                WHEN v.started_at IS NULL THEN NULL
                WHEN ((v.completed_at - v.started_at) / 60000) < 0 THEN 0
                ELSE ((v.completed_at - v.started_at) / 60000)
            END AS duration_minutes
        FROM visits v
        INNER JOIN clients c ON c.id = v.client_id AND c.is_deleted = 0
        LEFT JOIN visit_completions vc ON vc.visit_id = v.id AND vc.is_deleted = 0
        WHERE v.is_deleted = 0
            AND v.client_id = :clientId
            AND v.status = 'COMPLETED'
            AND v.completed_at IS NOT NULL
        ORDER BY v.completed_at DESC
        """,
    )
    fun observeCompletedWorkHistoryForClient(clientId: String): Flow<List<WorkHistoryItemEntity>>

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

    @Query(
        """
        SELECT * FROM visits
        WHERE is_deleted = 0 AND status = 'COMPLETED'
            AND completed_at >= :startMillis AND completed_at < :endMillis
        ORDER BY completed_at DESC
        """,
    )
    suspend fun findCompletedInRange(startMillis: Long, endMillis: Long): List<VisitEntity>

    @Upsert
    suspend fun upsert(visit: VisitEntity)

    @Query("UPDATE visits SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query(
        """
        UPDATE visits
        SET status = 'IN_PROGRESS',
            started_at = COALESCE(started_at, :startedAt),
            updated_at = :updatedAt
        WHERE id = :id AND is_deleted = 0 AND status NOT IN ('COMPLETED', 'CANCELLED')
        """,
    )
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
