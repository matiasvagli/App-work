package com.matiasdev.elecapp.features.reminders.data

import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import kotlinx.coroutines.flow.Flow

interface VisitReminderRepository {
    fun observeForVisit(visitId: String): Flow<List<VisitReminder>>

    suspend fun findEnabledForVisit(visitId: String): List<VisitReminder>

    suspend fun findAllEnabled(): List<VisitReminder>

    suspend fun replaceForVisit(visitId: String, reminders: List<VisitReminder>)

    suspend fun deleteForVisit(visitId: String)
}
