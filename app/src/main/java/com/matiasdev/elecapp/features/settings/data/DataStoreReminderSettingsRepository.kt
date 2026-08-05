package com.matiasdev.elecapp.features.settings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderSettingsDataStore by preferencesDataStore(name = "reminder_settings")

class DataStoreReminderSettingsRepository(
    private val context: Context,
) : ReminderSettingsRepository {
    override val settings: Flow<ReminderSettings> = context.reminderSettingsDataStore.data
        .map { preferences ->
            ReminderSettings(
                defaultFirstReminderMinutes = preferences[DEFAULT_FIRST],
                defaultSecondReminderMinutes = preferences[DEFAULT_SECOND],
            )
        }

    override suspend fun save(firstMinutes: Int?, secondMinutes: Int?) {
        context.reminderSettingsDataStore.edit { preferences ->
            if (firstMinutes == null) preferences.remove(DEFAULT_FIRST) else preferences[DEFAULT_FIRST] = firstMinutes
            if (secondMinutes == null) preferences.remove(DEFAULT_SECOND) else preferences[DEFAULT_SECOND] = secondMinutes
        }
    }

    private companion object {
        val DEFAULT_FIRST = intPreferencesKey("default_first_reminder_minutes")
        val DEFAULT_SECOND = intPreferencesKey("default_second_reminder_minutes")
    }
}
