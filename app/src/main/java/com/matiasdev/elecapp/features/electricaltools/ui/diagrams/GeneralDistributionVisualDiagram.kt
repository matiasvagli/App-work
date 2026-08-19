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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.GridGoldenratio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
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
fun GeneralDistributionVisualDiagram(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Jerarquía de Distribución y Límites de Responsabilidad (AEA 90364)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Jerarquía de Tableros y Límites (AEA 90364)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                // Bloque Distribuidora
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESPONSABILIDAD DE LA DISTRIBUIDORA", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = Color(0xFFE65100))
                        }
                        Text("RAD / RDD (Red de Distribuidora) ➔ DPLA / LAD (Fusibles de toma) ➔ Medidor (M)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                // Línea divisoria reglamentaria
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "┈┈┈┈┈ LÍMITE DE APLICACIÓN REGLAMENTARIA (PROPIETARIO / MATRICULADO) ┈┈┈┈┈",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }

                // Bloque Instalación del Usuario
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("INSTALACIÓN DEL INMUEBLE (MATRICULADO)", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }

                        TableroStepRow("TP", "Tablero Principal (TP)", "A ≤ 2 metros del medidor. Interruptor termomagnético general.")
                        TableroStepRow("CT", "Línea Seccional (CT)", "Conductor seccional hacia el interior + conductor PE a jabalina.")
                        TableroStepRow("TSG", "Tablero Seccional General (TSG)", "Disyuntor general y llaves termomagnéticas de circuitos terminales.")
                        TableroStepRow("TSn", "Subtableros (TS1, TS2, etc.)", "Cada tablero secundario debe poseer su PROPIO DISYUNTOR.")
                    }
                }
            }
        }

        // 2. Tipos de Cajas y Canalización
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tipos de Cajas en la Cañería (AEA 90364)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                CajaTypeRow(
                    title = "Caja de Paso",
                    desc = "Entran y salen los mismos cables, de largo y sin cortes ni derivaciones.",
                    tag = "Paso directo",
                )

                CajaTypeRow(
                    title = "Caja de Paso y Derivación",
                    desc = "Pasan cables de largo y se derivan solamente algunos circuitos secundarios.",
                    tag = "Pasante + Ramal",
                )

                CajaTypeRow(
                    title = "Caja de Derivación",
                    desc = "Se derivan y reparten los circuitos que ingresan hacia bocas de efecto o terminales.",
                    tag = "Reparto",
                )
            }
        }

        // 3. Reglas de Oro de Tiradas de Cañerías
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reglas de Distancias y Curvas (AEA)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Tirada de cañería HORIZONTAL: Máximo 12 metros entre cajas.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("• Tirada de cañería VERTICAL: Máximo 15 metros entre cajas.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("• Cantidad MÁXIMA de curvas por tirada: 3 curvas (máx 270° totales).", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Text("• Boca de Efecto: Ubicación de llaves, teclas, interruptores de comando.", style = MaterialTheme.typography.bodySmall)
                        Text("• Boca Terminal: Ubicación de tomas, luces, ventiladores u otros receptores.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // 4. Correspondencia Trifásica R / S / T y Neutro
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Correspondencia Trifásica y Código de Colores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PhaseBox("Fase R (L1)", "Castaño / Marrón", Color(0xFF795548), Modifier.weight(1f))
                    PhaseBox("Fase S (L2)", "Negro / Verde*", Color(0xFF212121), Modifier.weight(1f))
                    PhaseBox("Fase T (L3)", "Rojo", Color(0xFFD32F2F), Modifier.weight(1f))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE1F5FE),
                    border = BorderStroke(1.dp, Color(0xFF81D4FA)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Neutro (N): Celeste claro / Azul", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF0277BD))
                        Text("Tierra (PE): Verde-Amarillo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
private fun TableroStepRow(code: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp, 24.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(code, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CajaTypeRow(title: String, desc: String, tag: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PhaseBox(phase: String, colorName: String, colorHex: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colorHex.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, colorHex.copy(alpha = 0.5f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(12.dp).background(colorHex, CircleShape))
            Spacer(modifier = Modifier.height(4.dp))
            Text(phase, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = colorHex)
            Text(colorName, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}
