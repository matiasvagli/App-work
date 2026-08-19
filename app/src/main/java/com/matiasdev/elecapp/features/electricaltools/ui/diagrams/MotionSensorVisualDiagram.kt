package com.matiasdev.elecapp.features.electricaltools.ui.diagrams

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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
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

@Composable
fun MotionSensorVisualDiagram(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Esquema Funcional de Conexionado del Sensor PIR
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SENSOR DE MOVIMIENTO PIR (120V / 230Vca)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Bornes y conexión directa
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Llegada 220V (L - N)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("Salida a Lámpara (⊗)", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), style = MaterialTheme.typography.labelMedium)
                        }

                        Text("• Borne L (Fase): Entrada de Fase permanente de red (Marrón/Negro).", style = MaterialTheme.typography.bodySmall)
                        Text("• Borne N (Neutro): Neutro común que energiza el sensor y va directo al casquillo de la lámpara (Celeste).", style = MaterialTheme.typography.bodySmall)
                        Text("• Borne L' / SALIDA (Retorno): Fase conmutada por el relé que energiza la luminaria al detectar movimiento.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("• Lente PIR + LED Testigo: Ventana infrarroja con LED indicador de detección en tiempo real.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // 2. Perillas de Calibración (Trimmers)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Perillas de Calibración (Trimmers)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TrimmerBox(
                        title = "TIME (TIEMPO)",
                        minLabel = "MÍN (10 seg)",
                        maxLabel = "MÁX (7 min)",
                        desc = "Tiempo que la luz permanece encendida tras el último movimiento.",
                        modifier = Modifier.weight(1f),
                    )
                    TrimmerBox(
                        title = "LUX (LUZ)",
                        minLabel = "🌙 MÍN (Noche)",
                        maxLabel = "☀️ MÁX (Día)",
                        desc = "Nivel de luminosidad ambiente requerido para activar.",
                        modifier = Modifier.weight(1f),
                    )
                    TrimmerBox(
                        title = "SENS (ALCANCE)",
                        minLabel = "MÍN (2 m)",
                        maxLabel = "MÁX (12 m)",
                        desc = "Sensibilidad / radio de detección de presencia.",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 3. Área de Cobertura y Modelos de Instalación
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sensors, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Área de Detección y Modelos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Altura de Instalación Recomendada: Entre 2 y 4 metros para cono óptimo.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("• Alcance Máximo: Hasta 12 metros en condiciones normales.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("• Modelos habituales: Sensor de pared orientable con articulación, sensor para embutir en cielo raso (360°), y reflectores LED con PIR integrado.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrimmerBox(
    title: String,
    minLabel: String,
    maxLabel: String,
    desc: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(minLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(maxLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
