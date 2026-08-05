package com.matiasdev.elecapp.features.settings.data

import kotlinx.coroutines.flow.Flow

interface ReminderSettingsRepository {
    val settings: Flow<ReminderSettings>

    suspend fun save(firstMinutes: Int?, secondMinutes: Int?)
}
