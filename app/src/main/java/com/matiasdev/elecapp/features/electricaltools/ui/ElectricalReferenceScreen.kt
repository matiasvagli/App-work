package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import java.util.Locale
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.tan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalReferenceScreen(tool: ReferenceTool, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool.title(), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(tool.description(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            when (tool) {
                ReferenceTool.CONDUCTOR -> ConductorTool()
                ReferenceTool.LIGHTING -> LightingTool()
                ReferenceTool.CAPACITANCE -> CapacitanceTool()
                ReferenceTool.POWER_FACTOR -> PowerFactorTool()
                ReferenceTool.ENERGY -> EnergyTool()
                ReferenceTool.PROTECTION -> ProtectionTool()
                ReferenceTool.TABLES -> TechnicalTables()
            }

            TechnicalDisclaimer("Resultado orientativo. Verificá la edición vigente de AEA 90364, las condiciones reales de instalación, la coordinación de protecciones y los requisitos de la distribuidora antes de ejecutar o certificar.")
        }
    }
}

@Composable
private fun ConductorTool() {
    var current by remember { mutableStateOf("16") }
    var length by remember { mutableStateOf("20") }
    var selected by remember { mutableStateOf("PVC en cañería") }
    val locale = LocalLocale.current.platformLocale
    val references = listOf("PVC en cañería" to 0.75, "Bandeja ventilada" to 1.0, "A confirmar en obra" to 0.6)
    val ampacity = listOf(1.5 to 10.0, 2.5 to 16.0, 4.0 to 25.0, 6.0 to 32.0, 10.0 to 40.0, 16.0 to 63.0)
    val result = current.toDoubleOrNull()?.takeIf { it > 0 }?.let { load -> ampacity.firstOrNull { it.second >= load / (references.first { it.first == selected }.second) } }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parámetros de entrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            NumericInputField("Corriente de diseño", current, { current = it }, suffix = "A")
            NumericInputField("Longitud del tramo", length, { length = it }, suffix = "m")
            ChipRow(references.map { it.first }, selected) { selected = it }
        }
    }

    ResultCard("Sección inicial sugerida", result?.let { "${fmt(result.first)} mm² de cobre" } ?: "Completá una corriente positiva", "Referencia inicial: ${selected.lowercase(locale)}.")
    ReferenceTable("Tabla orientativa de partida", listOf("Sección" to "Corriente", *ampacity.map { "${fmt(it.first)} mm²" to "${fmt(it.second)} A" }.toTypedArray()))
}

@Composable
private fun LightingTool() {
    var area by remember { mutableStateOf("20") }
    var lux by remember { mutableStateOf("300") }
    var lumens by remember { mutableStateOf("1600") }
    var factor by remember { mutableStateOf("0,7") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parámetros de iluminación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            NumericInputField("Superficie", area, { area = it }, suffix = "m²")
            NumericInputField("Iluminancia objetivo", lux, { lux = it }, suffix = "lux")
            NumericInputField("Flujo por luminaria", lumens, { lumens = it }, suffix = "lm")
            NumericInputField("Factor de utilización y mantenimiento", factor, { factor = it })
        }
    }

    val result = listOf(area, lux, lumens, factor).map { it.replace(',', '.').toDoubleOrNull() }.takeIf { it.all { value -> value != null && value > 0 } }?.let { ceil(it[0]!! * it[1]!! / (it[2]!! * it[3]!!)) }
    ResultCard("Luminarias estimadas", result?.let { fmt(it) } ?: "Completá valores positivos", "La distribución, el deslumbramiento y la uniformidad requieren verificación en el ambiente.")
}

@Composable
private fun CapacitanceTool() {
    var kvar by remember { mutableStateOf("5") }
    var voltage by remember { mutableStateOf("220") }
    var frequency by remember { mutableStateOf("50") }
    var phase by remember { mutableStateOf("Monofásico") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parámetros del banco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            NumericInputField("Potencia reactiva", kvar, { kvar = it }, suffix = "kVAr")
            NumericInputField("Tensión", voltage, { voltage = it }, suffix = "V")
            NumericInputField("Frecuencia", frequency, { frequency = it }, suffix = "Hz")
            ChipRow(listOf("Monofásico", "Trifásico"), phase) { phase = it }
        }
    }

    val values = listOf(kvar, voltage, frequency).map { it.replace(',', '.').toDoubleOrNull() }
    val result = values.takeIf { it.all { v -> v != null && v > 0 } }?.let {
        val denominator = 2 * Math.PI * it[2]!! * it[1]!! * it[1]!!
        (it[0]!! * 1000 / denominator) * 1_000_000 * if (phase == "Trifásico") 2.0 / 3.0 else 1.0
    }
    ResultCard("Capacitancia teórica", result?.let { "${fmt(it)} µF" } ?: "Completá valores positivos", "Confirmá si el banco es individual o automático y controlá resonancia y armónicos antes de instalar.")
}

@Composable
private fun PowerFactorTool() {
    var power by remember { mutableStateOf("10") }
    var initial by remember { mutableStateOf("0,75") }
    var target by remember { mutableStateOf("0,95") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Corrección cos φ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            NumericInputField("Potencia activa", power, { power = it }, suffix = "kW")
            NumericInputField("Factor de potencia actual", initial, { initial = it })
            NumericInputField("Factor de potencia objetivo", target, { target = it })
        }
    }

    val values = listOf(power, initial, target).map { it.replace(',', '.').toDoubleOrNull() }
    val result = values.takeIf { it[0] != null && it[0]!! > 0 && it[1] != null && it[2] != null && it[1]!! in 0.01..1.0 && it[2]!! in 0.01..1.0 }?.let {
        it[0]!! * (tan(acos(it[1]!!)) - tan(acos(it[2]!!)))
    }
    ResultCard("Compensación aproximada", result?.let { "${fmt(max(0.0, it))} kVAr" } ?: "Revisá los factores", "Para usuarios alcanzados por ENRE, 0,95 es una referencia regulatoria actual de Edenor/Edesur; la distribuidora y la jurisdicción pueden cambiar el criterio.")
}

@Composable
private fun EnergyTool() {
    var power by remember { mutableStateOf("1000") }
    var quantity by remember { mutableStateOf("1") }
    var hours by remember { mutableStateOf("8") }
    var days by remember { mutableStateOf("22") }
    var tariff by remember { mutableStateOf("150") }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Estimación de consumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            NumericInputField("Potencia de cada equipo", power, { power = it }, suffix = "W")
            NumericInputField("Cantidad", quantity, { quantity = it }, suffix = "equipos")
            NumericInputField("Horas de uso por día", hours, { hours = it }, suffix = "h")
            NumericInputField("Días del período", days, { days = it }, suffix = "días")
            NumericInputField("Tarifa de referencia", tariff, { tariff = it }, suffix = "$/kWh")
        }
    }

    val values = listOf(power, quantity, hours, days, tariff).map { it.replace(',', '.').toDoubleOrNull() }
    val result = values.takeIf { it.all { v -> v != null && v >= 0 } }?.let { it[0]!! * it[1]!! * it[2]!! * it[3]!! / 1000 }
    ResultCard("Consumo del período", result?.let { "${fmt(it)} kWh" } ?: "Completá valores válidos", result?.let { "Costo estimado: $${fmt(it * values[4]!!)}" } ?: "La tarifa es editable y no representa una factura real.")
}

@Composable
private fun ProtectionTool() {
    var current by remember { mutableStateOf("18") }
    var selected by remember { mutableStateOf("1,2 × carga") }
    val multiplier = if (selected.startsWith("1,25")) 1.25 else 1.2
    val series = listOf(6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parámetros de protección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            NumericInputField("Corriente de diseño", current, { current = it }, suffix = "A")
            ChipRow(listOf("1,2 × carga", "1,25 × carga"), selected) { selected = it }
        }
    }

    val breaker = current.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }?.let { load -> series.firstOrNull { it >= load * multiplier } }
    ResultCard("Calibre orientativo", breaker?.let { "$it A" } ?: "Completá una corriente positiva", "La elección final debe coordinarse con la capacidad admisible del conductor, cortocircuito disponible, curva y selectividad.")
}

@Composable
private fun TechnicalTables() {
    Text("Valores rápidos de referencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    ReferenceTable("Resistividad aproximada a 20 °C", listOf("Material" to "Ω·mm²/m", "Cobre" to "0,0175", "Aluminio" to "0,0285"))
    ReferenceTable("Secciones comerciales habituales", listOf("Cobre" to "1,5 · 2,5 · 4 · 6 · 10 · 16 mm²", "Aluminio" to "16 · 25 · 35 · 50 mm²"))
    ReferenceTable("Serie usual de protecciones", listOf("Pequeños calibres" to "6 · 10 · 16 · 20 · 25 · 32 A", "Mayores calibres" to "40 · 50 · 63 · 80 · 100 A"))
    Text("Estas tablas no reemplazan tablas de intensidad admisible, factores de corrección ni verificación de cortocircuito.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ChipRow(values: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { item ->
            val isSelected = item == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(item) },
                label = { Text(item, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun ResultCard(title: String, value: String, note: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReferenceTable(title: String, rows: List<Pair<String, String>>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            rows.forEachIndexed { index, (key, value) ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bg,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(key, style = MaterialTheme.typography.bodyMedium)
                        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun ReferenceTool.title(): String = when (this) {
    ReferenceTool.CONDUCTOR -> "Sección orientativa de conductor"
    ReferenceTool.LIGHTING -> "Luminotecnia"
    ReferenceTool.CAPACITANCE -> "Capacitancia"
    ReferenceTool.POWER_FACTOR -> "Corrección de factor de potencia"
    ReferenceTool.ENERGY -> "Consumo energético"
    ReferenceTool.PROTECTION -> "Protecciones"
    ReferenceTool.TABLES -> "Tablas técnicas"
}

private fun ReferenceTool.description(): String = when (this) {
    ReferenceTool.CONDUCTOR -> "Estimá una sección inicial de cobre a partir de corriente, longitud y una condición de instalación seleccionada."
    ReferenceTool.LIGHTING -> "Estimá cuántas luminarias necesitás a partir de superficie, iluminancia y flujo luminoso."
    ReferenceTool.CAPACITANCE -> "Calculá una capacitancia teórica para una potencia reactiva objetivo."
    ReferenceTool.POWER_FACTOR -> "Estimá la potencia reactiva de compensación necesaria para pasar de un factor de potencia a otro."
    ReferenceTool.ENERGY -> "Proyectá consumo y costo con potencia, cantidad, horas y tarifa editables."
    ReferenceTool.PROTECTION -> "Obtené un calibre de partida para revisar junto con el conductor y la coordinación."
    ReferenceTool.TABLES -> "Consultá magnitudes rápidas para cálculos preliminares."
}

private fun fmt(value: Double): String = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.').replace('.', ',')

