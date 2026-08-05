package com.matiasdev.elecapp

import android.app.Application
import com.matiasdev.elecapp.app.AppContainer
import com.matiasdev.elecapp.features.reminders.scheduling.createReminderNotificationChannel

class ElecApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        createReminderNotificationChannel(this)
        appContainer = AppContainer(this)
    }
}
