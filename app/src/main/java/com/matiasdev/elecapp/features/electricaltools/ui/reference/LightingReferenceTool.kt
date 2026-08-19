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
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LightingReferenceTool(modifier: Modifier = Modifier) {
    var area by remember { mutableStateOf("20") }
    var lux by remember { mutableStateOf("300") }
    var lumens by remember { mutableStateOf("1600") }
    var factor by remember { mutableStateOf("0,7") }
    var selectedPresetLux by remember { mutableStateOf("Cocina / Baño (300 lx)") }
    var selectedPresetLamp by remember { mutableStateOf("Panel 18W (1500 lm)") }

    val luxPresets = listOf(
        "Pasillo / Escalera (100 lx)" to "100",
        "Estar / Dormitorio (150 lx)" to "150",
        "Cocina / Baño (300 lx)" to "300",
        "Oficina / Estudio (500 lx)" to "500",
        "Taller / Local (750 lx)" to "750",
    )

    val lampPresets = listOf(
        "Foco LED 9W (800 lm)" to "800",
        "LED 12W (1050 lm)" to "1050",
        "Panel 18W (1500 lm)" to "1500",
        "Panel 60x60 36W (3200 lm)" to "3200",
        "Reflector 50W (4500 lm)" to "4500",
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
                text = "Parámetros de iluminación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            NumericInputField(
                label = "Superficie del local",
                value = area,
                onValueChange = { area = it },
                suffix = "m²",
            )

            // Ambientes recomendados
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nivel de iluminancia recomendado (IRAM / AEA)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    luxPresets.forEach { (label, value) ->
                        val isSelected = selectedPresetLux == label
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPresetLux = label
                                lux = value
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
                label = "Iluminancia objetivo",
                value = lux,
                onValueChange = {
                    lux = it
                    selectedPresetLux = ""
                },
                suffix = "lux",
            )

            // Tipos de luminarias típicas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Flujo por luminaria típica",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    lampPresets.forEach { (label, value) ->
                        val isSelected = selectedPresetLamp == label
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPresetLamp = label
                                lumens = value
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
                label = "Flujo luminoso por luminaria",
                value = lumens,
                onValueChange = {
                    lumens = it
                    selectedPresetLamp = ""
                },
                suffix = "lm",
            )

            NumericInputField(
                label = "Factor de utilización y mantenimiento (η × fm)",
                value = factor,
                onValueChange = { factor = it },
            )
        }
    }

    val parsedValues = listOf(area, lux, lumens, factor).map { it.replace(',', '.').toDoubleOrNull() }
    val result = if (parsedValues.all { it != null && it > 0 }) {
        val totalLumensNeeded = (parsedValues[0]!! * parsedValues[1]!!) / (parsedValues[2]!! * parsedValues[3]!!)
        ceil(totalLumensNeeded)
    } else {
        null
    }

    ReferenceResultCard(
        title = "Luminarias estimadas",
        value = result?.let { "${formatDecimal(it)} luminarias" } ?: "Completá valores válidos",
        note = "Método de los lúmenes preliminar. La distribución uniforme, alturas de montaje y deslumbramiento deben revisarse en el local.",
    )

    ReferenceDataTable(
        title = "Niveles mínimos de servicio (Guía rápida)",
        rows = listOf(
            "Pasillos, escaleras y depósitos" to "100 lux",
            "Dormitorios, salas y comedores" to "150 - 200 lux",
            "Cocinas, baños y mesadas" to "300 - 400 lux",
            "Escritorios, oficinas y aulas" to "500 lux",
            "Talleres, dibujo y locales comerciales" to "500 - 750 lux",
        ),
    )
}
