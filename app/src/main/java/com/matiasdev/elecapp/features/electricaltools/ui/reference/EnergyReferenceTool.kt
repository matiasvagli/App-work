package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnergyReferenceTool(modifier: Modifier = Modifier) {
    var power by remember { mutableStateOf("1500") }
    var quantity by remember { mutableStateOf("1") }
    var hours by remember { mutableStateOf("4") }
    var days by remember { mutableStateOf("30") }
    var tariff by remember { mutableStateOf("150") }
    var selectedAppliance by remember { mutableStateOf("Termotanque (1500W)") }

    val appliancePresets = listOf(
        "Aire Acond. Split (2200W)" to "2200",
        "Termotanque (1500W)" to "1500",
        "Heladera c/ freezer (150W)" to "150",
        "Bomba 1/2 HP (375W)" to "375",
        "Bomba 1 HP (750W)" to "750",
        "Microondas (1200W)" to "1200",
        "Pava eléctrica (1800W)" to "1800",
        "Horno eléctrico (2000W)" to "2000",
        "Luces LED Casa (50W)" to "50",
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
                text = "Estimación de consumo energético",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Selector rápido de electrodoméstico
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Cargas típicas (selección rápida)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    appliancePresets.forEach { (label, watts) ->
                        val isSelected = selectedAppliance == label
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedAppliance = label
                                power = watts
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            NumericInputField(
                label = "Potencia de cada equipo",
                value = power,
                onValueChange = {
                    power = it
                    selectedAppliance = ""
                },
                suffix = "W",
            )

            NumericInputField(
                label = "Cantidad de equipos",
                value = quantity,
                onValueChange = { quantity = it },
                suffix = "unidades",
            )

            NumericInputField(
                label = "Horas promedio de uso por día",
                value = hours,
                onValueChange = { hours = it },
                suffix = "h/día",
            )

            NumericInputField(
                label = "Días del período",
                value = days,
                onValueChange = { days = it },
                suffix = "días",
            )

            NumericInputField(
                label = "Tarifa eléctrica de referencia",
                value = tariff,
                onValueChange = { tariff = it },
                suffix = "$/kWh",
            )
        }
    }

    val values = listOf(power, quantity, hours, days, tariff).map { it.replace(',', '.').toDoubleOrNull() }
    val kwh = if (values.all { it != null && it >= 0 }) {
        (values[0]!! * values[1]!! * values[2]!! * values[3]!!) / 1000.0
    } else {
        null
    }
    val cost = if (kwh != null && values[4] != null) kwh * values[4]!! else null

    ReferenceResultCard(
        title = "Consumo estimado del período",
        value = kwh?.let { "${formatDecimal(it)} kWh" } ?: "Completá valores válidos",
        note = cost?.let { "Costo estimado de energía: $${formatDecimal(it)} (Tarifa: $${tariff} / kWh sin impuestos/cargos fijos)." }
            ?: "La tarifa es editable y no incluye cargos fijos, impuestos municipales o provinciales.",
    )

    ReferenceDataTable(
        title = "Consumo mensual de referencia (30 días)",
        rows = listOf(
            "Heladera con freezer (24h ciclando ~8h)" to "36 kWh/mes",
            "Aire Acondicionado 3000 Fg (6h/día)" to "200 - 240 kWh/mes",
            "Termotanque eléctrico (3h/día)" to "135 kWh/mes",
            "Bomba centrífuga 1/2 HP (1h/día)" to "11 kWh/mes",
            "Iluminación LED completa casa (5h/día)" to "10 - 15 kWh/mes",
        ),
    )
}
