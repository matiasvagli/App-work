package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.reminders.domain.ReminderInput
import java.time.LocalDate
import java.time.LocalTime

data class VisitFormUiState(
    val isLoading: Boolean = true,
    val client: Client? = null,
    val clientSearchQuery: String = "",
    val clientSearchResults: List<Client> = emptyList(),
    val isClientSearchLoading: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0),
    val durationMinutes: String = "",
    val reason: String = "",
    val notes: String = "",
    val firstReminder: ReminderInput = ReminderInput(),
    val secondReminderEnabled: Boolean = false,
    val secondReminder: ReminderInput = ReminderInput(),
    val reasonError: String? = null,
    val dateTimeError: String? = null,
    val durationError: String? = null,
    val reminderError: String? = null,
    val reminderWarning: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

val VisitFormUiState.hasRequiredData: Boolean
    get() = client != null && reason.trim().isNotBlank()
