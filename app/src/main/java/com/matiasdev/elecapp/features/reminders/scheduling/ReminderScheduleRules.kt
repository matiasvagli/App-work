package com.matiasdev.elecapp.features.reminders.scheduling

import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.Instant
import kotlin.math.abs

fun reminderTriggerAt(visit: Visit, reminder: VisitReminder): Instant {
    return visit.scheduledAt.minusSeconds(reminder.minutesBefore * 60L)
}

fun shouldScheduleReminder(visit: Visit, reminder: VisitReminder, now: Instant = Instant.now()): Boolean {
    return reminder.enabled && reminderTriggerAt(visit, reminder).isAfter(now)
}

fun pendingIntentRequestCode(reminderId: String): Int {
    return abs(reminderId.hashCode())
}
