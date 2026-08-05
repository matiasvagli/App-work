package com.matiasdev.elecapp.features.reminders.domain

import java.time.Instant

data class ReminderValidationResult(
    val validMinutes: List<Int>,
    val skippedPastMinutes: List<Int>,
    val errorMessage: String? = null,
) {
    val isValid: Boolean = errorMessage == null
}

object ReminderRules {
    fun validate(
        scheduledAt: Instant,
        minutesBefore: List<Int>,
        now: Instant = Instant.now(),
    ): ReminderValidationResult {
        val positiveMinutes = minutesBefore.filter { it > 0 }
        if (positiveMinutes.size != minutesBefore.size) {
            return ReminderValidationResult(emptyList(), emptyList(), "Los recordatorios deben ser mayores a cero")
        }
        if (positiveMinutes.distinct().size != positiveMinutes.size) {
            return ReminderValidationResult(emptyList(), emptyList(), "No se pueden repetir recordatorios")
        }

        val grouped = positiveMinutes.groupBy { scheduledAt.minusSeconds(it * 60L).isAfter(now) }
        return ReminderValidationResult(
            validMinutes = grouped[true].orEmpty(),
            skippedPastMinutes = grouped[false].orEmpty(),
        )
    }
}
