package com.matiasdev.elecapp.features.reminders.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitReminderDao {
    @Query("SELECT * FROM visit_reminders WHERE visit_id = :visitId ORDER BY minutes_before DESC")
    fun observeForVisit(visitId: String): Flow<List<VisitReminderEntity>>

    @Query("SELECT * FROM visit_reminders WHERE visit_id = :visitId AND enabled = 1")
    suspend fun findEnabledForVisit(visitId: String): List<VisitReminderEntity>

    @Query("SELECT * FROM visit_reminders WHERE enabled = 1")
    suspend fun findAllEnabled(): List<VisitReminderEntity>

    @Upsert
    suspend fun upsertAll(reminders: List<VisitReminderEntity>)

    @Query("DELETE FROM visit_reminders WHERE visit_id = :visitId")
    suspend fun deleteForVisit(visitId: String)
}
