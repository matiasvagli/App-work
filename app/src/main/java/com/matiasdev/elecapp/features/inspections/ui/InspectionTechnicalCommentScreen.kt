package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.runtime.Composable
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository

@Composable
fun InspectionTechnicalCommentScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
) {
    InspectionTextSectionScreen(
        repository = repository,
        inspectionId = inspectionId,
        section = InspectionTextSection.TECHNICAL_COMMENT,
        title = "Observación técnica",
        label = "Comentario original del electricista",
        saveButtonText = "Guardar observación",
        savedMessage = "Observación técnica guardada",
        warning = null,
        onBackClick = onBackClick,
        navigateBackOnSave = true,
    )
}
