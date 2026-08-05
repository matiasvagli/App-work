package com.matiasdev.elecapp.features.visits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.summary.label
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.summary.label

@Composable
fun VisitDocumentsSection(
    uiState: VisitDetailUiState,
    onQuoteClick: () -> Unit,
    onMaterialClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Documentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Presupuesto: ${quoteVisitLabel(uiState.quote?.status)}")
        Button(onClick = onQuoteClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.quote == null) "Crear presupuesto" else "Ver presupuesto")
        }
        Text("Materiales: ${materialVisitLabel(uiState.materialList?.status)}")
        OutlinedButton(onClick = onMaterialClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.materialList == null) "Crear lista de materiales" else "Ver lista de materiales")
        }
    }
}

private fun quoteVisitLabel(status: QuoteStatus?): String = when (status) {
    null -> "No creado"
    else -> status.label()
}

private fun materialVisitLabel(status: MaterialListStatus?): String = when (status) {
    null -> "Sin lista"
    else -> status.label()
}
