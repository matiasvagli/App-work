package com.matiasdev.elecapp.features.agenda.ui

import com.matiasdev.elecapp.features.agenda.domain.AgendaVisitGroup
import java.time.LocalDate
import java.time.YearMonth

enum class AgendaMode(val label: String) {
    CALENDAR("Calendario"),
    TODAY("Hoy"),
    UPCOMING("Próximas"),
}

data class AgendaUiState(
    val isLoading: Boolean = true,
    val mode: AgendaMode = AgendaMode.CALENDAR,
    val selectedDate: LocalDate = LocalDate.now(),
    val visibleMonth: YearMonth = YearMonth.now(),
    val todayUpcoming: List<VisitAgendaItem> = emptyList(),
    val todayDoneOrPast: List<VisitAgendaItem> = emptyList(),
    val upcomingGroups: List<AgendaVisitGroup> = emptyList(),
    val upcomingItemsByVisitId: Map<String, VisitAgendaItem> = emptyMap(),
    val calendarItems: List<VisitAgendaItem> = emptyList(),
    val errorMessage: String? = null,
)
