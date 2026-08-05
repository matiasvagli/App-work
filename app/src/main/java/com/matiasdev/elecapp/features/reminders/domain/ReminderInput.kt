package com.matiasdev.elecapp.features.reminders.domain

enum class ReminderUnit(val label: String, val multiplier: Int) {
    MINUTES("minutos", 1),
    HOURS("horas", 60),
    DAYS("días", 1440),
}

data class ReminderInput(
    val option: ReminderOption = ReminderOption.NONE,
    val customValue: String = "",
    val customUnit: ReminderUnit = ReminderUnit.MINUTES,
) {
    fun toMinutesOrNull(): Int? {
        return when (option) {
            ReminderOption.NONE -> null
            ReminderOption.CUSTOM -> customValue.toIntOrNull()?.let { it * customUnit.multiplier }
            else -> option.minutes
        }
    }
}

fun reminderInputFromMinutes(minutes: Int?): ReminderInput {
    return when (minutes) {
        null -> ReminderInput()
        15 -> ReminderInput(ReminderOption.FIFTEEN_MINUTES)
        30 -> ReminderInput(ReminderOption.THIRTY_MINUTES)
        60 -> ReminderInput(ReminderOption.ONE_HOUR)
        120 -> ReminderInput(ReminderOption.TWO_HOURS)
        1440 -> ReminderInput(ReminderOption.ONE_DAY)
        else -> ReminderInput(ReminderOption.CUSTOM, customValue = minutes.toString())
    }
}
