package com.matiasdev.elecapp.features.reminders.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class VisitReminderScheduler(
    private val context: Context,
) : ReminderSchedulerPort {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(visit: Visit, client: Client, reminders: List<VisitReminder>) {
        reminders.forEach { reminder ->
            cancel(reminder.id)
            if (!shouldScheduleReminder(visit, reminder)) return@forEach
            val triggerAt = reminderTriggerAt(visit, reminder).toEpochMilli()
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent(visit, client, reminder.id),
            )
        }
    }

    fun cancel(reminderId: String) {
        existingPendingIntent(reminderId)?.let(alarmManager::cancel)
    }

    override fun cancelAll(reminders: List<VisitReminder>) {
        reminders.forEach { cancel(it.id) }
    }

    private fun pendingIntent(visit: Visit, client: Client, reminderId: String): PendingIntent {
        val location = listOfNotNull(client.address, client.locality).joinToString(", ")
        val time = DateTimeFormatter.ofPattern("HH:mm").format(visit.scheduledAt.atZone(ZoneId.systemDefault()))
        val intent = Intent(context, VisitReminderReceiver::class.java)
            .putExtra(EXTRA_REMINDER_ID, reminderId)
            .putExtra(EXTRA_VISIT_ID, visit.id)
            .putExtra(EXTRA_CLIENT_NAME, client.fullName)
            .putExtra(EXTRA_VISIT_TIME, time)
            .putExtra(EXTRA_LOCATION, location)
            .putExtra(EXTRA_REASON, visit.reason)
        return PendingIntent.getBroadcast(
            context,
            pendingIntentRequestCode(reminderId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun existingPendingIntent(reminderId: String): PendingIntent? {
        return PendingIntent.getBroadcast(
            context,
            pendingIntentRequestCode(reminderId),
            Intent(context, VisitReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
