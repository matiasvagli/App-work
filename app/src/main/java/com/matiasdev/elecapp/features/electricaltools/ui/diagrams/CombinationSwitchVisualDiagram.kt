package com.matiasdev.elecapp.features.electricaltools.ui.diagrams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CombinationSwitchVisualDiagram(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. LLAVE COMBINADA SIMPLE (2 Puntos)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "LLAVE COMBINADA SIMPLE (2 Puntos)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Esquema de conexión
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("NEUTRO", fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                            Text("───── ⊗ (Lámpara) ─────", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text("RETORNO", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• FASE (Marrón): Entra directo al borne común (centro) de la Llave 1.", style = MaterialTheme.typography.bodySmall)
                        Text("• 2 CABLES AUXILIARES: Unen los bornes 1-1 y 2-2 entre ambas llaves.", style = MaterialTheme.typography.bodySmall)
                        Text("• RETORNO (Naranja/Rojo): Sale del borne común de la Llave 2 hacia la lámpara.", style = MaterialTheme.typography.bodySmall)
                        Text("• NEUTRO (Celeste): Va directo al portalámparas (nunca pasa por las llaves).", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Cuadro de Simbología Unifilar
                UnifilarSymbolBox(
                    title = "SIMBOLOGÍA EN PLANO",
                    subtitle = "Símbolo reglamentario de Llave de Combinación",
                    isCross = false,
                )
            }
        }

        // 2. LLAVE COMBINADA DOBLE O DOBLE INVERSORA (4 Vías / Cruzamiento)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "LLAVE COMBINADA DOBLE O DOBLE INVERSORA (3 o más Puntos)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Troncales superiores
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF4CAF50), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TIERRA (PE): Acompaña cañerías y cajas metálicas", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF795548), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FASE (L): Va al común de la primera Llave Combinada", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5D4037), fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF03A9F4), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("NEUTRO (N): Troncal que alimenta todas las luminarias en paralelo", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Explicación de conmutación
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Extremo 1: Llave Combinada Simple (3 bornes).", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("• Centro: 1 o más Llaves Doble Inversoras (4 bornes) con cruzamiento en 'X' de los auxiliares.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("• Extremo 2: Llave Combinada Simple (3 bornes) de donde sale el Retorno.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("• Luminarias en paralelo: Todas conectadas entre la línea de Retorno y Neutro.", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Cuadro de Simbología Unifilar Cruzamiento
                UnifilarSymbolBox(
                    title = "SIMBOLOGÍA EN PLANO",
                    subtitle = "Símbolo reglamentario de Llave de Cruzamiento / Doble Inversora",
                    isCross = true,
                )
            }
        }
    }
}

@Composable
private fun UnifilarSymbolBox(title: String, subtitle: String, isCross: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Dibujo Canvas del Símbolo Unifilar
            Canvas(modifier = Modifier.size(70.dp, 70.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = 10.dp.toPx()
                val strokeWidth = 2.5.dp.toPx()

                // Círculo central
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )

                if (!isCross) {
                    // Símbolo de Llave Combinada Simple (Palanca diagonal quebrada)
                    // Brazo superior derecho
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x + radius * 0.7f, center.y - radius * 0.7f),
                        end = Offset(center.x + 22.dp.toPx(), center.y - 22.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x + 22.dp.toPx(), center.y - 22.dp.toPx()),
                        end = Offset(center.x + 12.dp.toPx(), center.y - 22.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )

                    // Brazo inferior izquierdo
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x - radius * 0.7f, center.y + radius * 0.7f),
                        end = Offset(center.x - 22.dp.toPx(), center.y + 22.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x - 22.dp.toPx(), center.y + 22.dp.toPx()),
                        end = Offset(center.x - 12.dp.toPx(), center.y + 22.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                } else {
                    // Símbolo de Llave de Cruzamiento (4 patitas en X)
                    // Arriba Derecha
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x + radius * 0.7f, center.y - radius * 0.7f),
                        end = Offset(center.x + 20.dp.toPx(), center.y - 20.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x + 20.dp.toPx(), center.y - 20.dp.toPx()),
                        end = Offset(center.x + 26.dp.toPx(), center.y - 12.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )

                    // Arriba Izquierda
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x - radius * 0.7f, center.y - radius * 0.7f),
                        end = Offset(center.x - 20.dp.toPx(), center.y - 20.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x - 20.dp.toPx(), center.y - 20.dp.toPx()),
                        end = Offset(center.x - 26.dp.toPx(), center.y - 12.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )

                    // Abajo Derecha
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
                        end = Offset(center.x + 20.dp.toPx(), center.y + 20.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x + 20.dp.toPx(), center.y + 20.dp.toPx()),
                        end = Offset(center.x + 26.dp.toPx(), center.y + 12.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )

                    // Abajo Izquierda
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x - radius * 0.7f, center.y + radius * 0.7f),
                        end = Offset(center.x - 20.dp.toPx(), center.y + 20.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(center.x - 20.dp.toPx(), center.y + 20.dp.toPx()),
                        end = Offset(center.x - 26.dp.toPx(), center.y + 12.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
