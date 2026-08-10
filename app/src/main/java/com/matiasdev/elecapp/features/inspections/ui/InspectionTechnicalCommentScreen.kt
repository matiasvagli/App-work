package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.runtime.Composable
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository

/**
 * Observación técnica: última sección del relevamiento.
 *
 * Guardar vuelve al overview, no a la pantalla anterior. Volver atrás dejaba al técnico
 * rebotando entre hallazgos y observaciones sin salida hacia adelante.
 */
@Composable
fun InspectionTechnicalCommentScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSaved: () -> Unit,
    onHomeClick: () -> Unit,
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
        onSaved = onSaved,
        onPreviousClick = onPreviousClick,
        onNextClick = onSaved,
        onHomeClick = onHomeClick,
        nextLabel = "Terminar",
        saveOnNextClick = true,
    )
}
