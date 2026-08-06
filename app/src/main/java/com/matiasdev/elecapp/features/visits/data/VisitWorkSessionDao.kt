package com.matiasdev.elecapp.features.visits.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitWorkSessionDao {
    @Query(
        """
        SELECT * FROM visit_work_sessions
        WHERE visit_id = :visitId AND is_deleted = 0
        ORDER BY started_at ASC
        """,
    )
    fun observeByVisitId(visitId: String): Flow<List<VisitWorkSessionEntity>>

    @Query(
        """
        SELECT * FROM visit_work_sessions
        WHERE is_deleted = 0
        ORDER BY started_at ASC
        """,
    )
    fun observeAllActive(): Flow<List<VisitWorkSessionEntity>>

    @Query(
        """
        SELECT * FROM visit_work_sessions
        WHERE visit_id = :visitId AND status = 'RUNNING' AND is_deleted = 0
        ORDER BY started_at DESC
        LIMIT 1
        """,
    )
    fun observeActiveByVisitId(visitId: String): Flow<VisitWorkSessionEntity?>

    @Query(
        """
        SELECT * FROM visit_work_sessions
        WHERE visit_id = :visitId AND status = 'RUNNING' AND is_deleted = 0
        ORDER BY started_at DESC
        LIMIT 1
        """,
    )
    suspend fun getActiveByVisitId(visitId: String): VisitWorkSessionEntity?

    @Query(
        """
        SELECT * FROM visit_work_sessions
        WHERE status = 'RUNNING' AND is_deleted = 0
        ORDER BY started_at DESC
        """,
    )
    suspend fun getAllActive(): List<VisitWorkSessionEntity>

    @Query(
        """
        SELECT * FROM visit_work_sessions
        WHERE visit_id = :visitId AND is_deleted = 0
        ORDER BY started_at ASC
        """,
    )
    suspend fun getSessionsForVisit(visitId: String): List<VisitWorkSessionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: VisitWorkSessionEntity)

    @Query(
        """
        UPDATE visit_work_sessions
        SET ended_at = :endedAt, status = :status, updated_at = :updatedAt
        WHERE visit_id = :visitId AND status = 'RUNNING' AND is_deleted = 0
        """,
    )
    suspend fun closeActiveSession(visitId: String, endedAt: Long, status: String, updatedAt: Long)

    @Query(
        """
        UPDATE visit_work_sessions
        SET notes = :notes, updated_at = :updatedAt
        WHERE id = :id AND is_deleted = 0
        """,
    )
    suspend fun updateNotes(id: String, notes: String?, updatedAt: Long)

    @Query("UPDATE visit_work_sessions SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)
}
