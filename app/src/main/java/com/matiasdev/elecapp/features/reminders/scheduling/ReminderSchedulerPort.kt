package com.matiasdev.elecapp.features.reminders.scheduling

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.visits.domain.Visit

interface ReminderSchedulerPort {
    fun schedule(visit: Visit, client: Client, reminders: List<VisitReminder>)

    fun cancelAll(reminders: List<VisitReminder>)
}
