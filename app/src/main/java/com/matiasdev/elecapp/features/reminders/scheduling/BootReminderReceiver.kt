package com.matiasdev.elecapp.features.reminders.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.matiasdev.elecapp.ElecApplication
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContainer = (context.applicationContext as ElecApplication).appContainer
                val scheduler = appContainer.reminderScheduler
                val reminders = appContainer.reminderRepository.findAllEnabled()
                val visits = appContainer.visitRepository.findFutureSchedulable(Instant.now().toEpochMilli())
                visits.forEach { visit ->
                    val client = appContainer.clientRepository.findById(visit.clientId)?.takeUnless { it.isDeleted }
                    if (client != null) {
                        scheduler.schedule(visit, client, reminders.filter { it.visitId == visit.id })
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
