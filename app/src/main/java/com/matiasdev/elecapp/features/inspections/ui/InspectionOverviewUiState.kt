package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalMeasurementReviewSummary
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.inspections.domain.AutoInspectionCalculation
import com.matiasdev.elecapp.features.inspections.domain.InspectionAggregate
import com.matiasdev.elecapp.features.inspections.domain.InspectionProgress
import com.matiasdev.elecapp.features.visits.domain.Visit

data class InspectionOverviewUiState(
    val isLoading: Boolean = true,
    val aggregate: InspectionAggregate? = null,
    val visit: Visit? = null,
    val calculations: List<TechnicalCalculation> = emptyList(),
    val autoCalculations: List<AutoInspectionCalculation> = emptyList(),
    val measurementReviewSummary: ElectricalMeasurementReviewSummary = ElectricalMeasurementReviewSummary(),
    val measurementReviewExpanded: Boolean = false,
    val progress: InspectionProgress? = null,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val completionMissingItems: List<String> = emptyList(),
    val showCompleteConfirmation: Boolean = false,
    val showReopenConfirmation: Boolean = false,
    val navigateHomeAfterCompletion: Boolean = false,
)
