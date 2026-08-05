package com.matiasdev.elecapp.features.settings.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSettingsRepositoryTest {
    @Test
    fun `saves default reminder values`() = runTest {
        val repository = FakeReminderSettingsRepository()

        repository.save(firstMinutes = 30, secondMinutes = 1440)

        assertEquals(ReminderSettings(30, 1440), repository.settings.first())
    }
}
