package com.matiasdev.elecapp.features.reminders.domain

enum class ReminderOption(val minutes: Int?, val label: String) {
    NONE(null, "Sin recordatorio"),
    FIFTEEN_MINUTES(15, "15 minutos antes"),
    THIRTY_MINUTES(30, "30 minutos antes"),
    ONE_HOUR(60, "1 hora antes"),
    TWO_HOURS(120, "2 horas antes"),
    ONE_DAY(1440, "1 día antes"),
    CUSTOM(null, "Personalizado"),
}

fun reminderSummary(minutes: List<Int>): String {
    if (minutes.isEmpty()) return "Sin recordatorios"
    return minutes.sortedDescending()
        .joinToString(" y ") { minutesBefore ->
            when (minutesBefore) {
                15 -> "15 minutos antes"
                30 -> "30 minutos antes"
                60 -> "1 hora antes"
                120 -> "2 horas antes"
                1440 -> "1 día antes"
                else -> customReminderLabel(minutesBefore)
            }
        }
}

fun customReminderLabel(minutesBefore: Int): String {
    return when {
        minutesBefore % 1440 == 0 -> "${minutesBefore / 1440} días antes"
        minutesBefore % 60 == 0 -> "${minutesBefore / 60} horas antes"
        else -> "$minutesBefore minutos antes"
    }
}
