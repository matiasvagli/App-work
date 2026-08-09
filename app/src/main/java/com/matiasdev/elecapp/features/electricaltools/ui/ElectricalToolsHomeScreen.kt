package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.core.ui.components.ElecBadge
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
                title = { Text("Herramientas eléctricas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
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
    val primaryTools = listOf(
        PrimaryToolCard(
            title = "Potencia, corriente y tensión",
            subtitle = "Cálculo de ley de Ohm, AC monofásica y trifásica con cosφ y eficiencia.",
            badgeText = "Activo",
            badgeBg = Color(0xFFE8F5E9),
            badgeTextColor = Color(0xFF1B5E20),
            icon = Icons.Default.Bolt,
            iconBg = Color(0xFFFFF8E1),
            iconTint = Color(0xFFF57F17),
            action = onPowerClick,
        ),
        PrimaryToolCard(
            title = "Caída de tensión",
            subtitle = "Verificación de % y ΔV en cobre o aluminio según tipo de sistema.",
            badgeText = "Activo",
            badgeBg = Color(0xFFE8F5E9),
            badgeTextColor = Color(0xFF1B5E20),
            icon = Icons.Default.LineAxis,
            iconBg = Color(0xFFE3F2FD),
            iconTint = Color(0xFF1976D2),
            action = onVoltageDropClick,
        ),
        PrimaryToolCard(
            title = "Historial de cálculos",
            subtitle = "Consulta, copia, duplicación y exportación de registros guardados.",
            badgeText = "Guardados",
            badgeBg = MaterialTheme.colorScheme.primaryContainer,
            badgeTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Default.History,
            iconBg = Color(0xFFF3E5F5),
            iconTint = Color(0xFF7B1FA2),
            action = onHistoryClick,
        ),
    )

    val referenceTools = listOf(
        ReferenceToolItem("Sección de conductor", "Cálculo preliminar", Icons.Default.Calculate, Color(0xFFE0F2F1), Color(0xFF00695C)) { onReferenceClick(ReferenceTool.CONDUCTOR) },
        ReferenceToolItem("Luminotecnia", "Dimensionamiento", Icons.Default.Lightbulb, Color(0xFFFFF8E1), Color(0xFFF57F17)) { onReferenceClick(ReferenceTool.LIGHTING) },
        ReferenceToolItem("Capacitancia", "Compensación kVAr", Icons.Default.PieChart, Color(0xFFE8EAF6), Color(0xFF283593)) { onReferenceClick(ReferenceTool.CAPACITANCE) },
        ReferenceToolItem("Factor de potencia", "Corrección cosφ", Icons.Default.EnergySavingsLeaf, Color(0xFFE8F5E9), Color(0xFF2E7D32)) { onReferenceClick(ReferenceTool.POWER_FACTOR) },
        ReferenceToolItem("Consumo energético", "Estimación kWh y $", Icons.Default.EnergySavingsLeaf, Color(0xFFF1F8E9), Color(0xFF33691E)) { onReferenceClick(ReferenceTool.ENERGY) },
        ReferenceToolItem("Protecciones", "Calibre orientativo", Icons.Default.Shield, Color(0xFFFFEBEE), Color(0xFFC62828)) { onReferenceClick(ReferenceTool.PROTECTION) },
        ReferenceToolItem("Tablas técnicas", "Referencia rápida", Icons.Default.TableChart, Color(0xFFF3E5F5), Color(0xFF6A1B9A)) { onReferenceClick(ReferenceTool.TABLES) },
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Hero Banner Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Centro de Cálculos Eléctricos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dimensionamiento, normativas y verificación rápida para instalaciones profesionales.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Calculadores Principales
        item {
            Text(
                text = "Calculadores principales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // List of Primary Tools
        items(primaryTools.size) { index ->
            val tool = primaryTools[index]
            ElevatedCard(
                onClick = tool.action,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tool.iconBg,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = tool.iconTint,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ElecBadge(
                                text = tool.badgeText,
                                containerColor = tool.badgeBg,
                                contentColor = tool.badgeTextColor,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tool.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }

        // Section Title: Herramientas de Referencia
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Herramientas de referencia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Grid of Reference Tools inside a single item
        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((referenceTools.size + 1) / 2 * 135).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(referenceTools) { item ->
                    Card(
                        onClick = item.action,
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = item.iconBg,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.iconTint,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PrimaryToolCard(
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val badgeBg: Color,
    val badgeTextColor: Color,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val action: () -> Unit,
)

private data class ReferenceToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
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

