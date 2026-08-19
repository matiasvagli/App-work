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
import kotlin.math.PI

@Composable
fun CapacitanceReferenceTool(modifier: Modifier = Modifier) {
    var kvar by remember { mutableStateOf("5") }
    var voltage by remember { mutableStateOf("220") }
    var frequency by remember { mutableStateOf("50") }
    var phase by remember { mutableStateOf("Monofásico") }

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
                text = "Cálculo de Capacitancia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            NumericInputField(
                label = "Potencia reactiva deseada (Q)",
                value = kvar,
                onValueChange = { kvar = it },
                suffix = "kVAr",
            )

            NumericInputField(
                label = "Tensión del sistema (V)",
                value = voltage,
                onValueChange = { voltage = it },
                suffix = "V",
            )

            NumericInputField(
                label = "Frecuencia de red (f)",
                value = frequency,
                onValueChange = { frequency = it },
                suffix = "Hz",
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tipo de conexión",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReferenceChipRow(listOf("Monofásico", "Trifásico (Triángulo)"), phase) { phase = it }
            }
        }
    }

    val values = listOf(kvar, voltage, frequency).map { it.replace(',', '.').toDoubleOrNull() }
    val result = if (values.all { it != null && it > 0 }) {
        val qWatts = values[0]!! * 1000.0
        val v = values[1]!!
        val f = values[2]!!
        val omega = 2 * PI * f
        if (phase.startsWith("Trifásico")) {
            // En banco trifásico triángulo: C_fase = Q / (3 * omega * V_linea^2)
            val cFarads = qWatts / (3 * omega * v * v)
            cFarads * 1_000_000
        } else {
            // Monofásico: C = Q / (omega * V^2)
            val cFarads = qWatts / (omega * v * v)
            cFarads * 1_000_000
        }
    } else {
        null
    }

    val note = if (phase.startsWith("Trifásico")) {
        "Capacitancia por rama en conexión triángulo (Δ). En presencia de variadores o armónicos, prever reactancias de desintonización."
    } else {
        "Capacitancia requerida para carga monofásica. Verificar tensión de aislamiento nominal del capacitor."
    }

    ReferenceResultCard(
        title = "Capacitancia requerida",
        value = result?.let { "${formatDecimal(it)} µF" } ?: "Completá valores válidos",
        note = note,
    )
}
