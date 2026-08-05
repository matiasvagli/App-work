package com.matiasdev.elecapp.features.settings.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeReminderSettingsRepository(
    initialSettings: ReminderSettings = ReminderSettings(),
) : ReminderSettingsRepository {
    private val values = MutableStateFlow(initialSettings)

    override val settings: Flow<ReminderSettings> = values

    override suspend fun save(firstMinutes: Int?, secondMinutes: Int?) {
        values.value = ReminderSettings(firstMinutes, secondMinutes)
    }
}
