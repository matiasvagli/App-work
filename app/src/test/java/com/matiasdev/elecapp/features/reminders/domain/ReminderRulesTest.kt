package com.matiasdev.elecapp.features.reminders.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReminderRulesTest {
    @Test
    fun `rejects duplicated reminders`() {
        val result = ReminderRules.validate(
            scheduledAt = Instant.parse("2026-08-04T12:00:00Z"),
            minutesBefore = listOf(30, 30),
            now = Instant.parse("2026-08-04T10:00:00Z"),
        )

        assertFalse(result.isValid)
        assertEquals("No se pueden repetir recordatorios", result.errorMessage)
    }

    @Test
    fun `skips reminders whose trigger time already passed`() {
        val result = ReminderRules.validate(
            scheduledAt = Instant.parse("2026-08-04T12:00:00Z"),
            minutesBefore = listOf(15, 180),
            now = Instant.parse("2026-08-04T10:00:00Z"),
        )

        assertEquals(listOf(15), result.validMinutes)
        assertEquals(listOf(180), result.skippedPastMinutes)
    }

    @Test
    fun `converts custom hours and days to minutes`() {
        assertEquals(
            120,
            ReminderInput(ReminderOption.CUSTOM, customValue = "2", customUnit = ReminderUnit.HOURS)
                .toMinutesOrNull(),
        )
        assertEquals(
            2880,
            ReminderInput(ReminderOption.CUSTOM, customValue = "2", customUnit = ReminderUnit.DAYS)
                .toMinutesOrNull(),
        )
    }

    @Test
    fun `summarizes common and custom reminders with readable text`() {
        assertEquals("1 día antes y 1 hora antes", reminderSummary(listOf(60, 1440)))
        assertEquals("2 horas antes", reminderSummary(listOf(120)))
        assertEquals("2 días antes", reminderSummary(listOf(2880)))
    }
}
