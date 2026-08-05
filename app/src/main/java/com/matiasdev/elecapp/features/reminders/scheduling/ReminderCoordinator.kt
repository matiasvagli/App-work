package com.matiasdev.elecapp.features.reminders.scheduling

import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.reminders.data.VisitReminderRepository
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.visits.domain.Visit

class ReminderCoordinator(
    private val clientRepository: ClientRepository,
    private val reminderRepository: VisitReminderRepository,
    private val scheduler: ReminderSchedulerPort,
) {
    suspend fun replaceAndSchedule(visit: Visit, reminders: List<VisitReminder>) {
        val previous = reminderRepository.findEnabledForVisit(visit.id)
        scheduler.cancelAll(previous)
        reminderRepository.replaceForVisit(visit.id, reminders)
        val client = clientRepository.findById(visit.clientId)?.takeUnless { it.isDeleted } ?: return
        scheduler.schedule(visit, client, reminders.filter { it.enabled })
    }

    suspend fun cancelForVisit(visitId: String) {
        val reminders = reminderRepository.findEnabledForVisit(visitId)
        scheduler.cancelAll(reminders)
    }
}
