package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.electricaltools.ui.NumericInputField
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.tan

@Composable
fun PowerFactorReferenceTool(modifier: Modifier = Modifier) {
    var power by remember { mutableStateOf("10") }
    var initial by remember { mutableStateOf("0,75") }
    var target by remember { mutableStateOf("0,95") }

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
                text = "Corrección de Factor de Potencia (cos φ)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            NumericInputField(
                label = "Potencia activa total de la instalación (P)",
                value = power,
                onValueChange = { power = it },
                suffix = "kW",
            )

            NumericInputField(
                label = "Factor de potencia actual (cos φ₁)",
                value = initial,
                onValueChange = { initial = it },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    NumericInputField(
                        label = "Factor de potencia objetivo (cos φ₂)",
                        value = target,
                        onValueChange = { target = it },
                    )
                }
            }

            FilledTonalButton(
                onClick = { target = "0,95" },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Fijar objetivo en 0,95 (Norma ENRE / Edenor / Edesur)")
            }
        }
    }

    val powerVal = power.replace(',', '.').toDoubleOrNull()
    val initialVal = initial.replace(',', '.').toDoubleOrNull()
    val targetVal = target.replace(',', '.').toDoubleOrNull()

    val kvarResult = if (powerVal != null && powerVal > 0 &&
        initialVal != null && initialVal in 0.05..1.0 &&
        targetVal != null && targetVal in 0.05..1.0 &&
        targetVal > initialVal
    ) {
        val phi1 = acos(initialVal)
        val phi2 = acos(targetVal)
        val deltaQ = powerVal * (tan(phi1) - tan(phi2))
        max(0.0, deltaQ)
    } else {
        null
    }

    ReferenceResultCard(
        title = "Potencia reactiva de compensación (Qc)",
        value = kvarResult?.let { "${formatDecimal(it)} kVAr" } ?: "Revisá los valores de entrada",
        note = "Compensación para evitar recargos por energía reactiva. En bancos automáticos, verificar pasos de conmutación y presencia de armónicos (THD).",
    )

    ReferenceDataTable(
        title = "Valores de referencia regulatorios",
        rows = listOf(
            "Mínimo reglamentario ENRE (Res. 85/2024)" to "cos φ ≥ 0,95 (Inductivo)",
            "Límite clásico anterior" to "cos φ = 0,85 (Con penalizaciones)",
            "Riesgo de sobrecompensación" to "Evitar cos φ capacitivo (genera sobretensión)",
        ),
    )
}
