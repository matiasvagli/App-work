package com.matiasdev.elecapp.features.electricaltools.ui.diagrams

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.ui.components.ElecBadge
import com.matiasdev.elecapp.features.electricaltools.domain.DiagramType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalDiagramRegistry
import com.matiasdev.elecapp.features.electricaltools.ui.shareCalculationText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalDiagramDetailScreen(
    diagramId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val diagram = remember(diagramId) {
        ElectricalDiagramRegistry.getById(diagramId) ?: ElectricalDiagramRegistry.allDiagrams.first()
    }
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Esquema Visual", "Paso a Paso", "Materiales y Tips")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(diagram.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val text = generateDiagramSummaryText(diagram)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Diagrama ${diagram.title}", text))
                        Toast.makeText(context, "Guía copiada al portapapeles", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar guía")
                    }
                    IconButton(onClick = {
                        val text = generateDiagramSummaryText(diagram)
                        shareCalculationText(context, text, "Compartir esquema ${diagram.title}")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Header con Categoría y Tensión
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = diagram.category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ElecBadge(
                        text = diagram.badgeText,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // Selector de Pestañas
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            // Contenido de la pestaña activa
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (selectedTabIndex) {
                    0 -> VisualDiagramTab(diagram)
                    1 -> StepByStepTab(diagram)
                    2 -> MaterialsAndTipsTab(diagram)
                }
            }
        }
    }
}

@Composable
private fun VisualDiagramTab(diagram: ElectricalDiagram) {
    // Renderizado del componente visual específico según el tipo
    when (diagram.type) {
        DiagramType.GENERAL_DISTRIBUTION -> GeneralDistributionVisualDiagram()
        DiagramType.CIRCUIT_BREAKERS_AND_RCD -> ProtectionsVisualDiagram()
        DiagramType.PHOTOCELL -> PhotocellVisualDiagram()
        DiagramType.MOTION_SENSOR -> MotionSensorVisualDiagram()
        DiagramType.THREE_WAY_SWITCH -> CombinationSwitchVisualDiagram()
        DiagramType.DOORBELL_AND_ELECTRIC_LOCK -> DoorbellAndLockVisualDiagram()
        DiagramType.WATER_PUMP_TANK_CISTERN -> WaterPumpVisualDiagram()
    }

    // Leyenda de Bornes
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Identificación de Bornes y Funciones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            diagram.terminalLegend.forEach { terminal ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(terminal.code, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("• ${terminal.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(terminal.functionDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    WireColorLegend()

    SafetyAlertCard(
        warningText = diagram.securityWarning,
        normativeRef = diagram.aeaReference,
    )
}

@Composable
private fun StepByStepTab(diagram: ElectricalDiagram) {
    Text(
        text = "Guía de Conexionado Paso a Paso",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    diagram.stepByStepGuide.forEach { step ->
        WireConnectionCard(
            stepNumber = step.stepNumber,
            wireName = step.wireName,
            wireColor = Color(step.wireColorHex),
            fromText = step.fromTerminal,
            toText = step.toTerminal,
            description = step.description,
        )
    }

    SafetyAlertCard(
        warningText = diagram.securityWarning,
        normativeRef = diagram.aeaReference,
    )
}

@Composable
private fun MaterialsAndTipsTab(diagram: ElectricalDiagram) {
    // Componentes requeridos
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Lista de Materiales Necesarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            diagram.requiredComponents.forEach { component ->
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp, end = 8.dp),
                    )
                    Text(
                        text = component,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    // Consejos prácticos de instalación
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF57F17))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consejos Técnicos de Taller y Obra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            diagram.practicalTips.forEach { tip ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }

    SafetyAlertCard(
        warningText = diagram.securityWarning,
        normativeRef = diagram.aeaReference,
    )
}

private fun generateDiagramSummaryText(diagram: ElectricalDiagram): String = buildString {
    appendLine("══════════════════════════════════════════")
    appendLine("⚡ ${diagram.title.uppercase()}")
    appendLine("Categoría: ${diagram.category} (${diagram.badgeText})")
    appendLine("══════════════════════════════════════════")
    appendLine()
    appendLine("📌 DESCRIPCIÓN:")
    appendLine(diagram.subtitle)
    appendLine()
    appendLine("📋 MATERIALES REQUERIDOS:")
    diagram.requiredComponents.forEach { appendLine("• $it") }
    appendLine()
    appendLine("🔌 PASO A PASO DE CONEXIONADO:")
    diagram.stepByStepGuide.forEach { step ->
        appendLine("${step.stepNumber}. ${step.title}")
        appendLine("   Cable: ${step.wireName}")
        appendLine("   Desde: ${step.fromTerminal} ➔ Hacia: ${step.toTerminal}")
        appendLine("   Detalle: ${step.description}")
        appendLine()
    }
    appendLine("💡 CONSEJOS TÉCNICOS:")
    diagram.practicalTips.forEach { appendLine("• $it") }
    appendLine()
    appendLine("⚠️ SEGURIDAD Y NORMATIVA:")
    appendLine(diagram.securityWarning)
    appendLine("Ref: ${diagram.aeaReference}")
    appendLine("══════════════════════════════════════════")
}.trimEnd()
