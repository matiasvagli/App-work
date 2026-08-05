package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus

data class VisitDetailUiState(
    val isLoading: Boolean = true,
    val visit: Visit? = null,
    val client: Client? = null,
    val inspection: ElectricalInspection? = null,
    val quote: Quote? = null,
    val materialList: MaterialList? = null,
    val errorMessage: String? = null,
    val visitPendingDelete: Visit? = null,
    val statusPendingChange: VisitStatus? = null,
    val showCancelledInspectionWarning: Boolean = false,
    val showStartVisitConfirmation: Boolean = false,
    val showDistantStartWarning: Boolean = false,
    val showCompletionDialog: Boolean = false,
    val completionNotes: String = "",
    val pendingWorkNotes: String = "",
    val snackbarMessage: String? = null,
)
