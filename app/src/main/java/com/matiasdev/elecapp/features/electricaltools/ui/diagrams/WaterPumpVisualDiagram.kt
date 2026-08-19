package com.matiasdev.elecapp.features.electricaltools.ui.diagrams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.unit.dp

@Composable
fun WaterPumpVisualDiagram(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Arquitectura del Tablero: Potencia 220V + Comando 12V",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            // Circuito de Potencia 220V
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Circuito de Potencia / Fuerza (220Vca)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Text("• Alimentación 220V → Térmica Bipolar → Disyuntor 30mA.", style = MaterialTheme.typography.bodySmall)
                    Text("• Contactos principales del Contactor (1-2, 3-4) → Relé Térmico → Bornera Bomba.", style = MaterialTheme.typography.bodySmall)
                    Text("• Motor de la Bomba conectado a Puesta a Tierra (PE Verde-Amarillo obligatoria).", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Circuito de Comando en 12V MBTS
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF3E5F5),
                border = BorderStroke(1.dp, Color(0xFFAB47BC)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF7B1FA2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Circuito de Comando Seguro en 12Vca (MBTS)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF4A148C))
                    }
                    Text("• Transformador de Aislación de Seguridad: 220Vca a 12Vca (o 24Vca).", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A148C))
                    Text("• Salida 12V entra a la Llave Selectora de 3 posiciones (MANUAL - 0 - AUTO).", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A148C))
                    Text("• Modo MANUAL: Envía 12V directo a Bobina A1 pasando por contacto NC 95-96 del Relé Térmico.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A148C))
                    Text("• Modo AUTO (Flotantes en serie): 12V → Flotante Cisterna (NC con agua) → Flotante Tanque (NC pide agua) → Relé 95-96 → Bobina A1.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF4A148C))
                    Text("• Retorno 0V: Borne A2 del Contactor cierra al borne 0V del transformador.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A148C))
                }
            }

            // Lógica de Flotantes y Señalización
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3. Lógica de Nivel y Señalización", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Text("• Cisterna vacía: El flotante cae, abre el circuito y detiene la bomba para evitar marcha en seco. Enciende Luz Roja de falta de agua.", style = MaterialTheme.typography.bodySmall)
                    Text("• Tanque lleno: El flotante sube, abre el circuito y detiene el llenado.", style = MaterialTheme.typography.bodySmall)
                    Text("• Marcha (Luz Verde): Activada por el contacto auxiliar NA (13-14) del contactor.", style = MaterialTheme.typography.bodySmall)
                    Text("• Falla Térmica (Luz Ámbar): Activada por contacto NA (97-98) del relé ante sobrecarga.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
