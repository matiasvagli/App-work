package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import kotlinx.coroutines.flow.map

@Composable
fun InspectionFinalReportScreen(
    repository: InspectionRepository,
    technicalCalculationRepository: TechnicalCalculationRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onFinishedClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    val hasCalculationsFlow = remember(inspectionId) {
        technicalCalculationRepository.observeByInspection(inspectionId).map { it.isNotEmpty() }
    }
    val hasCalculations by hasCalculationsFlow.collectAsStateWithLifecycle(false)
    val warning = buildString {
        append("Revisá el contenido antes de enviarlo al cliente.")
        if (hasCalculations) {
            append("\n\nEste relevamiento tiene mediciones o cálculos asociados. Incluilos al redactar el informe cuando correspondan.")
        }
    }
    InspectionTextSectionScreen(
        repository = repository,
        inspectionId = inspectionId,
        section = InspectionTextSection.FINAL_REPORT,
        title = "Informe final",
        label = "Informe para el cliente",
        saveButtonText = "Guardar informe",
        savedMessage = "Informe guardado",
        warning = warning,
        onBackClick = onBackClick,
        showCopyShare = true,
        showPrimarySaveButton = false,
        onSaved = onFinishedClick,
        onPreviousClick = onPreviousClick,
        onNextClick = {},
        onHomeClick = onHomeClick,
        nextLabel = "Finalizar",
        saveOnNextClick = true,
    )
}
