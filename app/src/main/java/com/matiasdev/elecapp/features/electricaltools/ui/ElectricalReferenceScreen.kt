package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matiasdev.elecapp.features.electricaltools.ui.reference.BasicFormulasReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.CapacitanceReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.ConductorReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.EnergyReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.LightingReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.PowerFactorReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.ProtectionReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.ResistorColorCodeReferenceTool
import com.matiasdev.elecapp.features.electricaltools.ui.reference.TechnicalTablesReferenceTool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalReferenceScreen(tool: ReferenceTool, onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool.title(), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tool.description(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when (tool) {
                ReferenceTool.CONDUCTOR -> ConductorReferenceTool()
                ReferenceTool.LIGHTING -> LightingReferenceTool()
                ReferenceTool.CAPACITANCE -> CapacitanceReferenceTool()
                ReferenceTool.POWER_FACTOR -> PowerFactorReferenceTool()
                ReferenceTool.ENERGY -> EnergyReferenceTool()
                ReferenceTool.BASIC_FORMULAS -> BasicFormulasReferenceTool()
                ReferenceTool.PROTECTION -> ProtectionReferenceTool()
                ReferenceTool.RESISTOR_COLOR -> ResistorColorCodeReferenceTool()
                ReferenceTool.TABLES -> TechnicalTablesReferenceTool()
            }

            TechnicalDisclaimer(
                text = "Resultado orientativo. Verificá la edición vigente de AEA 90364, las condiciones reales de instalación, la coordinación de protecciones y los requisitos de la distribuidora antes de ejecutar o certificar.",
            )
        }
    }
}

private fun ReferenceTool.title(): String = when (this) {
    ReferenceTool.CONDUCTOR -> "Sección orientativa de conductor"
    ReferenceTool.LIGHTING -> "Luminotecnia"
    ReferenceTool.CAPACITANCE -> "Capacitancia"
    ReferenceTool.POWER_FACTOR -> "Corrección de factor de potencia"
    ReferenceTool.ENERGY -> "Consumo energético"
    ReferenceTool.BASIC_FORMULAS -> "Fórmulas eléctricas básicas"
    ReferenceTool.PROTECTION -> "Protecciones"
    ReferenceTool.RESISTOR_COLOR -> "Código de resistencias"
    ReferenceTool.TABLES -> "Tablas técnicas"
}

private fun ReferenceTool.description(): String = when (this) {
    ReferenceTool.CONDUCTOR -> "Estimá una sección inicial de cobre a partir de corriente, longitud y una condición de instalación seleccionada."
    ReferenceTool.LIGHTING -> "Estimá cuántas luminarias necesitás a partir de superficie, iluminancia y flujo luminoso."
    ReferenceTool.CAPACITANCE -> "Calculá una capacitancia teórica para una potencia reactiva objetivo."
    ReferenceTool.POWER_FACTOR -> "Estimá la potencia reactiva de compensación necesaria para pasar de un factor de potencia a otro."
    ReferenceTool.ENERGY -> "Proyectá consumo y costo con potencia, cantidad, horas y tarifa editables."
    ReferenceTool.BASIC_FORMULAS -> "Resumen de fórmulas y despejes de Ley de Ohm y Potencia Eléctrica (V, I, R, P)."
    ReferenceTool.PROTECTION -> "Obtené un calibre de partida para revisar junto con el conductor y la coordinación."
    ReferenceTool.RESISTOR_COLOR -> "Decodificá resistencias de 4 y 5 bandas seleccionando los colores de cada franja."
    ReferenceTool.TABLES -> "Consultá magnitudes rápidas, código de colores y cañerías para obra."
}
