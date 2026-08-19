package com.matiasdev.elecapp.features.electricaltools.ui.reference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TechnicalTablesReferenceTool(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Tablas de Referencia Rápida para Obra",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        // Código de Colores Normalizado (IRAM / AEA 90364)
        ReferenceDataTable(
            title = "Código de colores de conductores (IRAM 2183 / AEA)",
            rows = listOf(
                "Fase R / L1" to "Castaño (Marrón)",
                "Fase S / L2" to "Negro",
                "Fase T / L3" to "Rojo",
                "Neutro (N)" to "Celeste claro",
                "Conductor de Protección (PE / Tierra)" to "Bicolor Verde-Amarillo",
                "Retornos de iluminación / Combinación" to "Blanco o Gris (u otro distinto a N/PE)",
            ),
        )

        // Diámetros mínimos de cañerías según cantidad de cables (Regla del 35% AEA 771.12.3)
        ReferenceDataTable(
            title = "Cañerías recomendadas según conductores (Máx 35% ocupación)",
            rows = listOf(
                "Hasta 3 cables de 1,5 mm²" to "RS 19 (Ø ext 19 mm / 3/4\")",
                "Hasta 3 cables de 2,5 mm² + PE" to "RS 19 o RS 22 (7/8\")",
                "Hasta 5 cables de 2,5 mm² + PE" to "RS 22 (Ø ext 22 mm / 7/8\")",
                "Hasta 4 cables de 4,0 mm² + PE" to "RS 25 (Ø ext 25 mm / 1\")",
                "Alimentador principal 6 a 10 mm²" to "RS 25 (1\") a RS 32 (1 1/4\")",
            ),
        )

        // Resistividades y constantes de materiales
        ReferenceDataTable(
            title = "Resistividad y propiedades de materiales (a 20 °C)",
            rows = listOf(
                "Cobre electrolítico (ρ)" to "0,017241 Ω·mm²/m",
                "Aluminio eléctrico (ρ)" to "0,028264 Ω·mm²/m",
                "Coeficiente térmico Cobre (α)" to "0,00393 1/°C",
                "Coeficiente térmico Aluminio (α)" to "0,00403 1/°C",
            ),
        )

        // Secciones comerciales habituales
        ReferenceDataTable(
            title = "Secciones comerciales normalizadas",
            rows = listOf(
                "Cobre unipolar / subterráneo" to "1,5 · 2,5 · 4 · 6 · 10 · 16 · 25 · 35 · 50 mm²",
                "Aluminio unipolar / preensamblado" to "16 · 25 · 35 · 50 · 70 · 95 mm²",
                "Serie estándar termomagnéticas" to "6 · 10 · 16 · 20 · 25 · 32 · 40 · 50 · 63 A",
                "Sensibilidad diferencial residencial" to "30 mA (Clase AC / A)",
            ),
        )

        Text(
            text = "Nota: Las tablas técnicas son guías rápidas de consulta en obra. En proyectos certificados deben verificarse los factores de agrupamiento y temperatura vigentes en la reglamentación.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
