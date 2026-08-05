package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter
import com.matiasdev.elecapp.features.electricaltools.summary.label

@Composable
fun NumericInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> EnumSegmentedField(
    label: String,
    selected: T,
    values: List<T>,
    text: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                FilterChip(selected = value == selected, onClick = { onSelected(value) }, label = { Text(text(value)) })
            }
        }
    }
}

@Composable
fun CalculationOriginChip(source: CalculationSource) {
    AssistChip(onClick = {}, label = { Text(source.label()) })
}

@Composable
fun TechnicalClassificationChip(classification: TechnicalClassification) {
    AssistChip(onClick = {}, label = { Text(classification.label()) })
}

@Composable
fun AssociationSummaryCard(association: CalculationAssociationDraft, onClear: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Este cálculo se guardará en:", fontWeight = FontWeight.SemiBold)
            Text(association.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (association.hasAssociation) {
                OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Quitar asociación") }
            }
        }
    }
}

@Composable
fun CalculationResultCard(title: String, value: String, classification: TechnicalClassification?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            classification?.let { TechnicalClassificationChip(it) }
        }
    }
}

@Composable
fun FormulaExplanationCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TechnicalDisclaimer(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun CalculationActions(
    canSave: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onSave, enabled = canSave, modifier = Modifier.fillMaxWidth()) { Text("Guardar cálculo") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) { Text("Copiar") }
            OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) { Text("Compartir") }
        }
        OutlinedButton(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Text("Nuevo cálculo") }
    }
}

fun TechnicalCalculation.primaryResultText(): String {
    return TechnicalValueFormatter.withUnit(primaryResultValue, primaryResultUnit, 4).ifBlank { "Sin resultado principal" }
}

@Preview(showBackground = true)
@Composable
private fun ComponentsPreview() {
    ElecAppTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CalculationResultCard("Resultado principal", "20,45 A", TechnicalClassification.REQUIRES_REVIEW)
            CalculationOriginChip(CalculationSource.CALCULATED)
            TechnicalDisclaimer("Resultado orientativo. Requiere revisión técnica cuando corresponda.")
        }
    }
}
