package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.runtime.Composable
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository

@Composable
fun InspectionFinalReportScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
) {
    InspectionTextSectionScreen(
        repository = repository,
        inspectionId = inspectionId,
        section = InspectionTextSection.FINAL_REPORT,
        title = "Informe final",
        label = "Informe para el cliente",
        saveButtonText = "Guardar informe",
        savedMessage = "Informe guardado",
        warning = "Revisá el contenido antes de enviarlo al cliente.",
        onBackClick = onBackClick,
        showCopyShare = true,
    )
}
