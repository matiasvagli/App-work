package com.matiasdev.elecapp.features.reminders.data

import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeVisitReminderRepository(
    initialReminders: List<VisitReminder> = emptyList(),
) : VisitReminderRepository {
    private val reminders = MutableStateFlow(initialReminders)

    override fun observeForVisit(visitId: String): Flow<List<VisitReminder>> {
        return reminders.map { values -> values.filter { it.visitId == visitId } }
    }

    override suspend fun findEnabledForVisit(visitId: String): List<VisitReminder> {
        return reminders.value.filter { it.visitId == visitId && it.enabled }
    }

    override suspend fun findAllEnabled(): List<VisitReminder> {
        return reminders.value.filter { it.enabled }
    }

    override suspend fun replaceForVisit(visitId: String, reminders: List<VisitReminder>) {
        this.reminders.value = this.reminders.value.filterNot { it.visitId == visitId } + reminders
    }

    override suspend fun deleteForVisit(visitId: String) {
        reminders.value = reminders.value.filterNot { it.visitId == visitId }
    }

    fun currentReminders(): List<VisitReminder> = reminders.value
}
