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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
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

@Composable
fun ProtectionsVisualDiagram(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. INTERRUPTOR TERMOMAGNÉTICO (TÉRMICA)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFC62828))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interruptor Termomagnético (Térmica)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // Dualidad Bimetal vs Bobina
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProtectionPrincipleCard(
                        title = "🔥 Protección Térmica (Bimetal)",
                        desc = "Actúa ante SOBRECARGAS prolongadas. El bimetal se deforma por calor (Efecto Joule) provocando el disparo con retardo de tiempo.",
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                    )
                    ProtectionPrincipleCard(
                        title = "⚡ Protección Magnética (Bobina)",
                        desc = "Actúa ante CORTOCIRCUITOS. Una corriente abrupta crea un fuerte campo magnético que atrae el percutor instantáneamente (t ≤ 0,1 s).",
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Curvas de disparo B, C, D
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Rangos de Disparo Magnético (según IEC 60898-1):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        CurveRow("Curva B", "3 a 5 × In", "Líneas muy largas, generadores o cargas puramente resistivas.")
                        CurveRow("Curva C (Estándar)", "5 a 10 × In", "Uso general domiciliario: tomas, iluminación y electrodomésticos.")
                        CurveRow("Curva D", "10 a 20 × In", "Arranque pesado de motores, transformadores y compresores.")
                    }
                }

                // Ecuaciones de límites térmicos (Ejemplo 16A)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Ejemplo de Cálculos y Límites de Corriente (Térmica 16A Curva C):", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = Color(0xFFE65100))
                        Text("• 1,13 × In = 18,08 A ➔ NO dispara en 1 hora (condición de no desconexión).", style = MaterialTheme.typography.bodySmall)
                        Text("• 1,45 × In = 23,20 A ➔ DISPARA en menos de 1 hora (condición de desconexión térmica obligatoria).", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Text("• 2,55 × In = 40,80 A ➔ Dispara entre 1 y 60 segundos.", style = MaterialTheme.typography.bodySmall)
                        Text("• Disparo magnético (5 a 10 In) = 80 A a 160 A ➔ Disparo instantáneo en t ≤ 0,1 s.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. INTERRUPTORES DIFERENCIALES (DISYUNTORES): TIPOS AC, A, F, B
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Disyuntores Diferenciales (Tipos AC, A, F, B)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                DifferentialTypeCard(
                    typeName = "TIPO AC (Alterna Senoidal Pura)",
                    detected = "Corriente alterna senoidal pura (50/60 Hz).",
                    notDetected = "NO detecta corrientes continuas pulsantes ni frecuencia variable.",
                    apps = "Iluminación tradicional y resistencias puras (cada vez menos recomendado para viviendas modernas).",
                    badgeColor = Color(0xFF1565C0),
                )

                DifferentialTypeCard(
                    typeName = "TIPO A (Alterna + Continua Pulsante)",
                    detected = "Alterna senoidal + corriente continua pulsante rectificada.",
                    notDetected = "NO detecta continua pura (DC lisa).",
                    apps = "Viviendas modernas: lavarropas, hornos, fuentes conmutadas, microondas y placas de inducción.",
                    badgeColor = Color(0xFF2E7D32),
                )

                DifferentialTypeCard(
                    typeName = "TIPO F (Frecuencia Variable / Inverter)",
                    detected = "Alterna + pulsante + frecuencia variable monofásica (hasta 1 kHz).",
                    notDetected = "NO detecta continua pura (DC).",
                    apps = "Equipos con tecnología Inverter: aires acondicionados inverter, lavarropas y bombas con variador monofásico.",
                    badgeColor = Color(0xFFE65100),
                )

                DifferentialTypeCard(
                    typeName = "TIPO B (Universal / DC Pura)",
                    detected = "TODO tipo de fugas: alterna, pulsante, continua pura (DC lisa) y alta frecuencia hasta varios kHz.",
                    notDetected = "Protección total universal contra cualquier forma de onda.",
                    apps = "Cargadores de autos eléctricos (EV), inversores solares fotovoltaicos, variadores trifásicos y UPS.",
                    badgeColor = Color(0xFF6A1B9A),
                )
            }
        }
    }
}

@Composable
private fun ProtectionPrincipleCard(title: String, desc: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = color)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CurveRow(curve: String, range: String, application: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.width(130.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                Text(curve, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(range, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(application, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DifferentialTypeCard(
    typeName: String,
    detected: String,
    notDetected: String,
    apps: String,
    badgeColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(badgeColor, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(typeName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium, color = badgeColor)
            }
            Text("• SÍ Detecta: $detected", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("• $notDetected", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
            Text("• Aplicación: $apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
