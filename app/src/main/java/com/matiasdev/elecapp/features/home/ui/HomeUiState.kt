package com.matiasdev.elecapp.features.home.ui

import com.matiasdev.elecapp.features.agenda.ui.VisitAgendaItem

data class HomeUiState(
    val isLoading: Boolean = true,
    val nextVisit: VisitAgendaItem? = null,
    val currentVisit: VisitAgendaItem? = null,
    val todayCount: Int = 0,
    val tomorrowCount: Int = 0,
    val draftInspectionCount: Int = 0,
    val draftQuoteCount: Int = 0,
    val draftMaterialListCount: Int = 0,
    val errorMessage: String? = null,
)
