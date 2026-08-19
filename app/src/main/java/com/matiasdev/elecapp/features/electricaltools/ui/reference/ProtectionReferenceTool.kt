package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun ProtectionReferenceTool(modifier: Modifier = Modifier) {
    var current by remember { mutableStateOf("18") }
    var selectedFactor by remember { mutableStateOf("1,20 × carga") }
    var selectedCurve by remember { mutableStateOf("Curva C (Estándar)") }

    val factorOptions = listOf("1,15 × carga", "1,20 × carga", "1,25 × carga")
    val multiplier = when (selectedFactor) {
        "1,15 × carga" -> 1.15
        "1,25 × carga" -> 1.25
        else -> 1.20
    }

    val standardSeries = listOf(6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100, 125)
    val curveOptions = listOf(
        "Curva B (3 a 5 In)" to "Cargas puramente resistivas, líneas muy largas o generadores con bajo cortocircuito.",
        "Curva C (5 a 10 In)" to "Uso general domiciliario y comercial: tomas corrientes, iluminación y pequeños motores.",
        "Curva D (10 a 20 In)" to "Motores con arranque pesado, compresores, transformadores y circuitos con alta inserción (inrush).",
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
                text = "Dimensionamiento de protecciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            NumericInputField(
                label = "Corriente de diseño de la carga (Ib)",
                value = current,
                onValueChange = { current = it },
                suffix = "A",
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Margen de seguridad sobre corriente nominal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReferenceChipRow(factorOptions, selectedFactor) { selectedFactor = it }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Curva de disparo magnético",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReferenceChipRow(curveOptions.map { it.first }, selectedCurve) { selectedCurve = it }
            }

            // Explicación de la curva seleccionada
            val curveExplanation = curveOptions.firstOrNull { it.first == selectedCurve }?.second
            if (curveExplanation != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = selectedCurve,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = curveExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    val currentVal = current.replace(',', '.').toDoubleOrNull()
    val suggestedBreaker = currentVal?.takeIf { it > 0 }?.let { load ->
        standardSeries.firstOrNull { it >= load * multiplier }
    }

    // Regla de coordinación de disyuntor diferencial
    val suggestedDifferential = when {
        suggestedBreaker == null -> null
        suggestedBreaker <= 25 -> "25 A / 30 mA (Sensibilidad alta para protección de personas)"
        suggestedBreaker <= 40 -> "40 A / 30 mA (Coordinado para tableros hasta 40A)"
        suggestedBreaker <= 63 -> "63 A / 30 mA (Coordinado para cabeceras hasta 63A)"
        else -> "≥ ${suggestedBreaker} A / 30 mA (o selectivo 300 mA en cabecera general)"
    }

    ReferenceResultCard(
        title = "Calibre termomagnético orientativo (In)",
        value = suggestedBreaker?.let { "$it A ($selectedCurve)" } ?: "Completá una corriente válida",
        note = "Condición reglamentaria de protección: Ib ≤ In ≤ Iz (La corriente nominal In debe ser menor o igual a la capacidad admisible del conductor Iz).",
    )

    if (suggestedDifferential != null) {
        ReferenceDataTable(
            title = "Coordinación con interruptor diferencial",
            rows = listOf(
                "Térmica calculada (In)" to "$suggestedBreaker A",
                "Disyuntor sugerido" to suggestedDifferential,
                "Sensibilidad para personas" to "30 mA (Obligatorio en circuitos terminales)",
                "Disparo térmico Curva C" to "1,13 In (No disparo) a 1,45 In (Disparo térmico)",
            ),
        )
    }
}
