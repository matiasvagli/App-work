package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class DespejeRowData(
    val magnitude: String,
    val formula: String,
    val explanation: String,
    val accentColor: Color,
)

private data class LawSectionData(
    val title: String,
    val headerColor: Color,
    val description: String,
    val topLabel: String,
    val topBg: Color,
    val bottomLeftLabel: String,
    val bottomLeftBg: Color,
    val bottomRightLabel: String,
    val bottomRightBg: Color,
    val rows: List<DespejeRowData>,
    val units: List<String>,
    val reminders: List<String>,
)

@Composable
fun BasicFormulasReferenceTool(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ENCABEZADO
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TABLA DINÁMICA – FÓRMULAS ELÉCTRICAS BÁSICAS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Resumen de fórmulas y despejes para aplicar en circuitos eléctricos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                )
            }
        }

        // 1. LEY DE OHM
        LawSectionCard(
            data = LawSectionData(
                title = "1. LEY DE OHM",
                headerColor = Color(0xFFC62828),
                description = "Relaciona el voltaje (V), la corriente (I) y la resistencia (R) en un circuito.",
                topLabel = "V\nVoltaje (V)",
                topBg = Color(0xFFFFEBEE),
                bottomLeftLabel = "I\nIntensidad (A)",
                bottomLeftBg = Color(0xFFE0F7FA),
                bottomRightLabel = "R\nResistencia (Ω)",
                bottomRightBg = Color(0xFFFFF8E1),
                rows = listOf(
                    DespejeRowData("Voltaje (V)", "V = I · R", "Para calcular el voltaje, multiplica la intensidad (I) por la resistencia (R).", Color(0xFFC62828)),
                    DespejeRowData("Intensidad (I)", "I = V / R", "Para calcular la intensidad, divide el voltaje (V) por la resistencia (R).", Color(0xFF0288D1)),
                    DespejeRowData("Resistencia (R)", "R = V / I", "Para calcular la resistencia, divide el voltaje (V) por la intensidad (I).", Color(0xFFF57F17)),
                ),
                units = listOf("V (Voltio) – unidad de voltaje", "I (Amperio) – unidad de intensidad", "R (Ohmio) – unidad de resistencia"),
                reminders = listOf(
                    "• A mayor resistencia, menor será la intensidad (si el voltaje es constante).",
                    "• A mayor voltaje, mayor será la intensidad (si la resistencia es constante).",
                ),
            ),
        )

        // 2. POTENCIA ELÉCTRICA (WATT)
        LawSectionCard(
            data = LawSectionData(
                title = "2. POTENCIA ELÉCTRICA (WATT)",
                headerColor = Color(0xFF1565C0),
                description = "Relaciona la potencia (P), el voltaje (V) y la intensidad (I) en un circuito.",
                topLabel = "P\nPotencia (W)",
                topBg = Color(0xFFFFEBEE),
                bottomLeftLabel = "V\nVoltaje (V)",
                bottomLeftBg = Color(0xFFE0F7FA),
                bottomRightLabel = "I\nIntensidad (A)",
                bottomRightBg = Color(0xFFFFF8E1),
                rows = listOf(
                    DespejeRowData("Potencia (P)", "P = V · I", "Para calcular la potencia, multiplica el voltaje (V) por la intensidad (I).", Color(0xFFC62828)),
                    DespejeRowData("Voltaje (V)", "V = P / I", "Para calcular el voltaje, divide la potencia (P) por la intensidad (I).", Color(0xFF0288D1)),
                    DespejeRowData("Intensidad (I)", "I = P / V", "Para calcular la intensidad, divide la potencia (P) por el voltaje (V).", Color(0xFFF57F17)),
                ),
                units = listOf("P (Watt) – unidad de potencia", "V (Voltio) – unidad de voltaje", "I (Amperio) – unidad de intensidad"),
                reminders = listOf(
                    "• A mayor voltaje o mayor intensidad, mayor será la potencia.",
                    "• La potencia es el \"consumo\" o \"trabajo\" que realiza un equipo eléctrico.",
                ),
            ),
        )

        // 3. FÓRMULAS COMBINADAS ÚTILES Y OBSERVACIONES
        SectionCombinedFormulasAndNotes()
    }
}

@Composable
private fun LawSectionCard(data: LawSectionData) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = data.headerColor,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = data.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TriangleVisualCard(
                    topLabel = data.topLabel,
                    topBg = data.topBg,
                    bottomLeftLabel = data.bottomLeftLabel,
                    bottomLeftBg = data.bottomLeftBg,
                    bottomRightLabel = data.bottomRightLabel,
                    bottomRightBg = data.bottomRightBg,
                    modifier = Modifier.weight(1f),
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CÓMO USAR EL TRIÁNGULO", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text("• Tapa con el dedo la magnitud que quieres calcular.", style = MaterialTheme.typography.bodySmall)
                        Text("• Las otras dos te indican la operación que debes hacer.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            data.rows.forEach { row ->
                FormulaDespejeRow(
                    magnitude = row.magnitude,
                    formula = row.formula,
                    explanation = row.explanation,
                    accentColor = row.accentColor,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("UNIDADES", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFF1565C0))
                        data.units.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("RECORDATORIO", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                        data.reminders.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCombinedFormulasAndNotes() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF6A1B9A),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "FÓRMULAS COMBINADAS ÚTILES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Sustituyendo Ley de Ohm en Potencia", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6A1B9A))
                        Text("• Reemplazando I = V/R en P = V · I:", style = MaterialTheme.typography.bodySmall)
                        Text("P = V² / R", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828))
                        Text("• Reemplazando V = I · R en P = V · I:", style = MaterialTheme.typography.bodySmall)
                        Text("P = I² · R", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Sustituyendo en Ley de Ohm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6A1B9A))
                        Text("• A partir de P = V · I:", style = MaterialTheme.typography.bodySmall)
                        Text("V = P / I", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0288D1))
                        Text("• A partir de P = V · I:", style = MaterialTheme.typography.bodySmall)
                        Text("I = P / V", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0288D1))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECEFF1),
                border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OBSERVACIONES IMPORTANTES", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = Color(0xFF37474F))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Estas fórmulas son válidas para circuitos de corriente continua (CC) y corriente alterna (CA) en régimen resistivo.", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Asegúrate siempre de que las unidades sean correctas antes de aplicar las fórmulas.", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Desconecta siempre la energía antes de realizar cualquier manipulación en un circuito.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TriangleVisualCard(
    topLabel: String,
    topBg: Color,
    bottomLeftLabel: String,
    bottomLeftBg: Color,
    bottomRightLabel: String,
    bottomRightBg: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(shape = RoundedCornerShape(8.dp), color = topBg, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text(topLabel, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = bottomLeftBg, modifier = Modifier.weight(1f)) {
                    Text(bottomLeftLabel, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
                }
                Surface(shape = RoundedCornerShape(8.dp), color = bottomRightBg, modifier = Modifier.weight(1f)) {
                    Text(bottomRightLabel, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
private fun FormulaDespejeRow(
    magnitude: String,
    formula: String,
    explanation: String,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(magnitude, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = accentColor, modifier = Modifier.width(90.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            ) {
                Text(formula, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Text(explanation, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
    }
}
