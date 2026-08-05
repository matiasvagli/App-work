package com.matiasdev.elecapp.features.reminders.scheduling

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.visits.domain.Visit

class FakeReminderScheduler : ReminderSchedulerPort {
    val scheduledReminderIds = mutableListOf<String>()
    val cancelledReminderIds = mutableListOf<String>()

    override fun schedule(visit: Visit, client: Client, reminders: List<VisitReminder>) {
        scheduledReminderIds += reminders.map { it.id }
    }

    override fun cancelAll(reminders: List<VisitReminder>) {
        cancelledReminderIds += reminders.map { it.id }
    }
}
