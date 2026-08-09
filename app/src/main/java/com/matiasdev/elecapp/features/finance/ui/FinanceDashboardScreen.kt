package com.matiasdev.elecapp.features.finance.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.FinanceMetrics
import com.matiasdev.elecapp.features.finance.domain.FinancePeriodPreset
import com.matiasdev.elecapp.features.finance.domain.MoneyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    financeRepository: FinanceRepository,
    onBackClick: () -> Unit,
    onNavigateToReceipts: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: FinanceDashboardViewModel = viewModel(factory = FinanceDashboardViewModelFactory(financeRepository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val metrics = uiState.metrics

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Economía y Rendimiento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Period Selector Chips
            PeriodFilterRow(
                selectedPreset = uiState.preset,
                onPresetSelected = { viewModel.load(it) },
            )

            // Hero Summary Card
            FinanceHeroCard(metrics = metrics)

            // Income & Collection Visual Chart
            IncomeDistributionChart(metrics = metrics)

            // Performance Visual Bar Chart
            PerformanceBarChart(metrics = metrics)

            // Key Metrics 2x2 Grid
            KeyMetricsGrid(metrics = metrics)

            // Quick Action Buttons
            if (onNavigateToReceipts != null) {
                OutlinedButton(
                    onClick = onNavigateToReceipts,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver todos los comprobantes", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PeriodFilterRow(
    selectedPreset: FinancePeriodPreset,
    onPresetSelected: (FinancePeriodPreset) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FinancePeriodPreset.entries.forEach { preset ->
            FilterChip(
                selected = selectedPreset == preset,
                onClick = { onPresetSelected(preset) },
                label = { Text(preset.label(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
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
private fun FinanceHeroCard(metrics: FinanceMetrics) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Facturación Total Generada",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Text(
                text = MoneyFormatter.format(metrics.generatedCents),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = "💼 ${metrics.completedJobs} trabajos cerrados",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = "👥 ${metrics.servedClientCount} clientes",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeDistributionChart(metrics: FinanceMetrics) {
    val total = metrics.generatedCents.coerceAtLeast(1L)
    val collectedRatio = (metrics.collectedCents.toDouble() / total).coerceIn(0.0, 1.0)
    val pendingRatio = (metrics.pendingCents.toDouble() / total).coerceIn(0.0, 1.0)
    val pctCollected = (collectedRatio * 100).toInt()
    val pctPending = (pendingRatio * 100).toInt()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Estado de Cobranza",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "$pctCollected% Cobrado",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Dual Progress Visual Ratio Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (pctCollected > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(collectedRatio.toFloat().coerceAtLeast(0.01f))
                            .background(Color(0xFF2E7D32)), // Vibrant Green
                    )
                }
                if (pctPending > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(pendingRatio.toFloat().coerceAtLeast(0.01f))
                            .background(Color(0xFFE65100)), // Vibrant Amber/Orange
                    )
                }
            }

            // Breakdown Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Cobrado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(MoneyFormatter.format(metrics.collectedCents), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE65100)),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Pendiente", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(MoneyFormatter.format(metrics.pendingCents), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceBarChart(metrics: FinanceMetrics) {
    val hoursWorked = (metrics.workedMinutes / 60.0).let { "%.1f".format(it) }

    val barData = listOf(
        BarItem("Trabajos", metrics.completedJobs.toFloat(), "${metrics.completedJobs}", MaterialTheme.colorScheme.primary),
        BarItem("Horas", (metrics.workedMinutes / 60.0).toFloat(), "${hoursWorked}h", MaterialTheme.colorScheme.secondary),
        BarItem("Cobros", metrics.paymentCount.toFloat(), "${metrics.paymentCount}", MaterialTheme.colorScheme.tertiary),
        BarItem("Clientes", metrics.servedClientCount.toFloat(), "${metrics.servedClientCount}", Color(0xFF1565C0)),
    )

    val maxVal = barData.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Resumen de Actividad Operativa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom,
            ) {
                barData.forEach { item ->
                    val barRatio = (item.value / maxVal).coerceIn(0.15f, 1.0f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        Text(
                            text = item.labelValue,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = item.color,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(barRatio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(item.color),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private data class BarItem(
    val title: String,
    val value: Float,
    val labelValue: String,
    val color: Color,
)

@Composable
private fun KeyMetricsGrid(metrics: FinanceMetrics) {
    val hoursWorked = (metrics.workedMinutes / 60.0).let { "%.1f".format(it) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MetricKpiCard(
                    icon = Icons.Default.Schedule,
                    title = "Ganancia por Hora",
                    value = MoneyFormatter.format(metrics.generatedPerHourCents),
                    subtitle = "$hoursWorked hs registradas",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricKpiCard(
                    icon = Icons.Default.Receipt,
                    title = "Ticket Promedio",
                    value = MoneyFormatter.format(metrics.averageTicketCents),
                    subtitle = "Por trabajo cerrado",
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MetricKpiCard(
                    icon = Icons.Default.AttachMoney,
                    title = "Cobrado por Hora",
                    value = MoneyFormatter.format(metrics.collectedPerHourCents),
                    subtitle = "Tasa de cobro real",
                    color = Color(0xFF2E7D32),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricKpiCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Cobros Recibidos",
                    value = "${metrics.paymentCount}",
                    subtitle = "Pagos registrados",
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun MetricKpiCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
            )
        }
    }
}

private fun FinancePeriodPreset.label(): String = when (this) {
    FinancePeriodPreset.TODAY -> "Hoy"
    FinancePeriodPreset.LAST_7_DAYS -> "7 días"
    FinancePeriodPreset.THIS_MONTH -> "Mes"
    FinancePeriodPreset.PREVIOUS_MONTH -> "Mes ant."
    FinancePeriodPreset.THIS_YEAR -> "Año"
}
