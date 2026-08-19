package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class ResistorColor(
    val name: String,
    val digit: Int?,
    val multiplier: Double?,
    val multiplierLabel: String,
    val tolerance: Double?,
    val toleranceLabel: String,
    val colorHex: Color,
    val textColor: Color = Color.White,
)

private val resistorColors = listOf(
    ResistorColor("Negro", 0, 1.0, "1 Ω", null, "", Color(0xFF212121)),
    ResistorColor("Marrón", 1, 10.0, "10 Ω", 1.0, "±1% (F)", Color(0xFF795548)),
    ResistorColor("Rojo", 2, 100.0, "100 Ω", 2.0, "±2% (G)", Color(0xFFD32F2F)),
    ResistorColor("Naranja", 3, 1000.0, "1 kΩ", null, "", Color(0xFFFF9800)),
    ResistorColor("Amarillo", 4, 10000.0, "10 kΩ", null, "", Color(0xFFFFEB3B), Color.Black),
    ResistorColor("Verde", 5, 100000.0, "100 kΩ", 0.5, "±0.5% (D)", Color(0xFF4CAF50)),
    ResistorColor("Azul", 6, 1000000.0, "1 MΩ", 0.25, "±0.25% (C)", Color(0xFF1976D2)),
    ResistorColor("Violeta", 7, 10000000.0, "10 MΩ", 0.10, "±0.10% (B)", Color(0xFF7B1FA2)),
    ResistorColor("Gris", 8, null, "", 0.05, "±0.05%", Color(0xFF9E9E9E)),
    ResistorColor("Blanco", 9, null, "", null, "", Color(0xFFEEEEEE), Color.Black),
    ResistorColor("Oro", null, 0.1, "0.1 Ω", 5.0, "±5% (J)", Color(0xFFFFC107), Color.Black),
    ResistorColor("Plata", null, 0.01, "0.01 Ω", 10.0, "±10% (K)", Color(0xFFCFD8DC), Color.Black),
)

@Composable
fun ResistorColorCodeReferenceTool(modifier: Modifier = Modifier) {
    var bandCount by remember { mutableStateOf("4 Bandas") }
    val is5Bands = bandCount == "5 Bandas"

    var band1 by remember { mutableStateOf("Marrón") }
    var band2 by remember { mutableStateOf("Negro") }
    var band3 by remember { mutableStateOf("Rojo") } // Multiplicador en 4 bandas o 3er dígito en 5 bandas
    var band4 by remember { mutableStateOf("Rojo") } // Multiplicador en 5 bandas o Tolerancia en 4 bandas
    var band5 by remember { mutableStateOf("Oro") }  // Tolerancia en 5 bandas

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Código de Colores de Resistencias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Selector 4 / 5 Bandas
            ReferenceChipRow(listOf("4 Bandas", "5 Bandas"), bandCount) { bandCount = it }

            // Visualización gráfica de la resistencia
            ResistorBodyGraphic(
                is5Bands = is5Bands,
                b1 = resistorColors.first { it.name == band1 }.colorHex,
                b2 = resistorColors.first { it.name == band2 }.colorHex,
                b3 = resistorColors.first { it.name == band3 }.colorHex,
                b4 = resistorColors.first { it.name == band4 }.colorHex,
                b5 = if (is5Bands) resistorColors.first { it.name == band5 }.colorHex else null,
            )

            // Selectores de Bandas
            BandSelectorRow("1ª Banda (Dígito 1)", band1, resistorColors.filter { it.digit != null }) { band1 = it }
            BandSelectorRow("2ª Banda (Dígito 2)", band2, resistorColors.filter { it.digit != null }) { band2 = it }

            if (is5Bands) {
                BandSelectorRow("3ª Banda (Dígito 3)", band3, resistorColors.filter { it.digit != null }) { band3 = it }
                BandSelectorRow("4ª Banda (Multiplicador)", band4, resistorColors.filter { it.multiplier != null }) { band4 = it }
                BandSelectorRow("5ª Banda (Tolerancia)", band5, resistorColors.filter { it.tolerance != null }) { band5 = it }
            } else {
                BandSelectorRow("3ª Banda (Multiplicador)", band3, resistorColors.filter { it.multiplier != null }) { band3 = it }
                BandSelectorRow("4ª Banda (Tolerancia)", band4, resistorColors.filter { it.tolerance != null }) { band4 = it }
            }
        }
    }

    // Cálculo del valor resistivo
    val d1 = resistorColors.firstOrNull { it.name == band1 }?.digit ?: 0
    val d2 = resistorColors.firstOrNull { it.name == band2 }?.digit ?: 0

    val (resistanceOhms, tolerancePercent) = if (is5Bands) {
        val d3 = resistorColors.firstOrNull { it.name == band3 }?.digit ?: 0
        val mult = resistorColors.firstOrNull { it.name == band4 }?.multiplier ?: 1.0
        val tol = resistorColors.firstOrNull { it.name == band5 }?.tolerance ?: 5.0
        val value = ((d1 * 100) + (d2 * 10) + d3) * mult
        Pair(value, tol)
    } else {
        val mult = resistorColors.firstOrNull { it.name == band3 }?.multiplier ?: 1.0
        val tol = resistorColors.firstOrNull { it.name == band4 }?.tolerance ?: 5.0
        val value = ((d1 * 10) + d2) * mult
        Pair(value, tol)
    }

    val formattedValue = formatResistance(resistanceOhms)
    val minR = resistanceOhms * (1.0 - (tolerancePercent / 100.0))
    val maxR = resistanceOhms * (1.0 + (tolerancePercent / 100.0))

    ReferenceResultCard(
        title = "Valor Nominal Calculado",
        value = "$formattedValue  (±$tolerancePercent%)",
        note = "Rango real por tolerancia: ${formatResistance(minR)} a ${formatResistance(maxR)}",
    )

    // Tabla Completa de Colores
    ReferenceDataTable(
        title = "Tabla Normalizada de Códigos de Color",
        rows = listOf(
            "Negro" to "0 · Multiplicador: 1 Ω",
            "Marrón" to "1 · Mult: 10 Ω · Tol: ±1% (F)",
            "Rojo" to "2 · Mult: 100 Ω · Tol: ±2% (G)",
            "Naranja" to "3 · Mult: 1 kΩ",
            "Amarillo" to "4 · Mult: 10 kΩ",
            "Verde" to "5 · Mult: 100 kΩ · Tol: ±0.5% (D)",
            "Azul" to "6 · Mult: 1 MΩ · Tol: ±0.25% (C)",
            "Violeta" to "7 · Mult: 10 MΩ · Tol: ±0.10% (B)",
            "Gris" to "8 · Tol: ±0.05%",
            "Blanco" to "9",
            "Oro" to "Mult: 0.1 Ω · Tol: ±5% (J)",
            "Plata" to "Mult: 0.01 Ω · Tol: ±10% (K)",
        ),
    )
}

@Composable
private fun ResistorBodyGraphic(
    is5Bands: Boolean,
    b1: Color,
    b2: Color,
    b3: Color,
    b4: Color,
    b5: Color?,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5E0C3),
        border = BorderStroke(2.dp, Color(0xFFD7CCC8)),
        modifier = Modifier.fillMaxWidth().height(60.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.width(10.dp).height(50.dp).background(b1, RoundedCornerShape(2.dp)))
            Box(modifier = Modifier.width(10.dp).height(46.dp).background(b2, RoundedCornerShape(2.dp)))
            Box(modifier = Modifier.width(10.dp).height(46.dp).background(b3, RoundedCornerShape(2.dp)))
            Box(modifier = Modifier.width(10.dp).height(46.dp).background(b4, RoundedCornerShape(2.dp)))
            if (is5Bands && b5 != null) {
                Box(modifier = Modifier.width(10.dp).height(50.dp).background(b5, RoundedCornerShape(2.dp)))
            }
        }
    }
}

@Composable
private fun BandSelectorRow(
    label: String,
    selected: String,
    options: List<ResistorColor>,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ReferenceChipRow(
            values = options.map { it.name },
            selected = selected,
            onSelected = onSelect,
        )
    }
}

private fun formatResistance(ohms: Double): String {
    return when {
        ohms >= 1_000_000 -> "${formatDecimal(ohms / 1_000_000)} MΩ"
        ohms >= 1_000 -> "${formatDecimal(ohms / 1_000)} kΩ"
        else -> "${formatDecimal(ohms)} Ω"
    }
}
