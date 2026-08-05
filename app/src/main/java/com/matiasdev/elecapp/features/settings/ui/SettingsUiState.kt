package com.matiasdev.elecapp.features.settings.ui

data class SettingsUiState(
    val firstReminderMinutes: String = "",
    val secondReminderMinutes: String = "",
    val secondReminderEnabled: Boolean = false,
    val savedMessage: String? = null,
)
