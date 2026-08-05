package com.matiasdev.elecapp.features.reminders.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VisitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createReminderNotificationChannel(context)
        showVisitReminderNotification(context, intent)
    }
}
