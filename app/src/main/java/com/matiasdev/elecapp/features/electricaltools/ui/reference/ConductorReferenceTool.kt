package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.electricaltools.ui.NumericInputField

@Composable
fun ConductorReferenceTool(modifier: Modifier = Modifier) {
    var current by remember { mutableStateOf("16") }
    var length by remember { mutableStateOf("20") }
    var selectedInstallation by remember { mutableStateOf("PVC en cañería") }

    val references = listOf(
        "PVC en cañería" to 0.75,
        "Bandeja ventilada / Aire" to 1.0,
        "Varios circuitos agrupados" to 0.60,
    )

    // Escala práctica de seguridad respetando coordinación con térmicas curva C
    val practicalAmpacity = listOf(
        1.5 to 10.0,
        2.5 to 16.0,
        4.0 to 25.0,
        6.0 to 32.0,
        10.0 to 40.0,
        16.0 to 63.0,
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Sección preliminar de conductor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            NumericInputField(
                label = "Corriente de diseño (A)",
                value = current,
                onValueChange = { current = it },
                suffix = "A",
            )

            NumericInputField(
                label = "Longitud aproximada del tramo (m)",
                value = length,
                onValueChange = { length = it },
                suffix = "m",
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Condición de instalación",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReferenceChipRow(references.map { it.first }, selectedInstallation) { selectedInstallation = it }
            }
        }
    }

    val currentVal = current.replace(',', '.').toDoubleOrNull()
    val deratingFactor = references.firstOrNull { it.first == selectedInstallation }?.second ?: 0.75
    val suggestedConductor = currentVal?.takeIf { it > 0 }?.let { load ->
        val adjustedLoad = load / deratingFactor
        practicalAmpacity.firstOrNull { it.second >= adjustedLoad }
    }

    ReferenceResultCard(
        title = "Sección inicial sugerida",
        value = suggestedConductor?.let { "${formatDecimal(it.first)} mm² de cobre" } ?: "Completá una corriente válida",
        note = "Criterio práctico de partida para instalación en ${selectedInstallation.lowercase()}. Para tramos largos (> 20m), verificar siempre caída de tensión en el calculador principal.",
    )

    ReferenceDataTable(
        title = "Escala de partida práctica y protecciones habituales",
        rows = listOf(
            "1,5 mm² Cu" to "Carga hasta 10 A (Térmica 10 A)",
            "2,5 mm² Cu" to "Carga hasta 16 A (Térmica 15/16 A)",
            "4,0 mm² Cu" to "Carga hasta 25 A (Térmica 20/25 A)",
            "6,0 mm² Cu" to "Carga hasta 32 A (Térmica 32 A)",
            "10,0 mm² Cu" to "Carga hasta 40 A (Térmica 40 A)",
            "16,0 mm² Cu" to "Carga hasta 63 A (Térmica 50/63 A)",
        ),
    )
}
