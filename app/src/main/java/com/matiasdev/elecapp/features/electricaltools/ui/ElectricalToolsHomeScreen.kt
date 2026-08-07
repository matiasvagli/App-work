package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalToolsHomeScreen(
    onBackClick: () -> Unit,
    onPowerClick: () -> Unit,
    onVoltageDropClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onReferenceClick: (ReferenceTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Herramientas eléctricas") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
            )
        },
    ) { padding ->
        ElectricalToolsHomeContent(
            onPowerClick = onPowerClick,
            onVoltageDropClick = onVoltageDropClick,
            onHistoryClick = onHistoryClick,
            onReferenceClick = onReferenceClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun ElectricalToolsHomeContent(
    onPowerClick: () -> Unit,
    onVoltageDropClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onReferenceClick: (ReferenceTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = listOf(
        ToolCard("Potencia, corriente y tensión", "Activo", Icons.Default.Bolt, true, onPowerClick),
        ToolCard("Caída de tensión", "Activo", Icons.Default.LineAxis, true, onVoltageDropClick),
        ToolCard("Historial", "Cálculos guardados", Icons.Default.History, true, onHistoryClick),
        ToolCard("Sección orientativa de conductor", "Cálculo inicial", Icons.Default.Calculate, true) { onReferenceClick(ReferenceTool.CONDUCTOR) },
        ToolCard("Luminotecnia", "Cálculo inicial", Icons.Default.Lightbulb, true) { onReferenceClick(ReferenceTool.LIGHTING) },
        ToolCard("Capacitancia", "Cálculo inicial", Icons.Default.PieChart, true) { onReferenceClick(ReferenceTool.CAPACITANCE) },
        ToolCard("Corrección de factor de potencia", "Cálculo inicial", Icons.Default.EnergySavingsLeaf, true) { onReferenceClick(ReferenceTool.POWER_FACTOR) },
        ToolCard("Consumo energético", "Estimación", Icons.Default.EnergySavingsLeaf, true) { onReferenceClick(ReferenceTool.ENERGY) },
        ToolCard("Protecciones", "Selección orientativa", Icons.Default.Shield, true) { onReferenceClick(ReferenceTool.PROTECTION) },
        ToolCard("Tablas técnicas", "Consulta rápida", Icons.Default.TableChart, true) { onReferenceClick(ReferenceTool.TABLES) },
    )
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Herramientas eléctricas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cards) { card ->
                Card(onClick = card.action, enabled = card.enabled, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(card.icon, contentDescription = null)
                        Text(card.title, fontWeight = FontWeight.SemiBold)
                        Text(card.status, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private data class ToolCard(
    val title: String,
    val status: String,
    val icon: ImageVector,
    val enabled: Boolean,
    val action: () -> Unit,
)

enum class ReferenceTool {
    CONDUCTOR, LIGHTING, CAPACITANCE, POWER_FACTOR, ENERGY, PROTECTION, TABLES,
}

@Preview(showBackground = true)
@Composable
private fun ElectricalToolsHomePreview() {
    ElecAppTheme {
        ElectricalToolsHomeContent({}, {}, {}, {})
    }
}
