package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter
import com.matiasdev.elecapp.features.electricaltools.summary.label
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageDropScreen(
    repository: TechnicalCalculationRepository,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    clientId: String?,
    visitId: String?,
    inspectionId: String?,
    duplicateId: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoltageDropViewModel = viewModel(
        factory = VoltageDropViewModelFactory(repository, clientRepository, visitRepository, inspectionRepository, clientId, visitId, inspectionId, duplicateId),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Caída de tensión") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
            )
        },
    ) { padding ->
        VoltageDropContent(
            state = state,
            onUpdate = viewModel::update,
            onCalculate = viewModel::calculate,
            onSave = viewModel::save,
            onClearAssociation = viewModel::clearAssociation,
            onNew = viewModel::newCalculation,
            onCopy = { state.asCalculation()?.let { clipboard.setText(AnnotatedString(TechnicalCalculationTextGenerator.generate(it))) } },
            onShare = { state.asCalculation()?.let { shareCalculationText(context, TechnicalCalculationTextGenerator.generate(it)) } },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun VoltageDropContent(
    state: VoltageDropUiState,
    onUpdate: (VoltageDropUiState.() -> VoltageDropUiState) -> Unit,
    onCalculate: () -> Unit,
    onSave: () -> Unit,
    onClearAssociation: () -> Unit,
    onNew: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TechnicalDisclaimer("Resultado orientativo. Requiere revisión técnica antes de definir una corrección.")
        EnumSegmentedField("Sistema", state.systemType, ElectricalSystemType.entries.toList(), ElectricalSystemType::label) { onUpdate { copy(systemType = it) } }
        NumericInputField("Tensión nominal", state.nominalVoltage, { onUpdate { copy(nominalVoltage = it) } }, suffix = "V")
        EnumSegmentedField("Corriente", state.currentMode, VoltageDropCurrentMode.entries.toList(), { if (it == VoltageDropCurrentMode.DIRECT_CURRENT) "Ingresar corriente" else "Derivar desde potencia" }) {
            onUpdate { copy(currentMode = it) }
        }
        if (state.currentMode == VoltageDropCurrentMode.DIRECT_CURRENT) {
            NumericInputField("Corriente", state.current, { onUpdate { copy(current = it) } }, suffix = "A")
        } else {
            NumericInputField("Potencia activa", state.power, { onUpdate { copy(power = it) } }, suffix = if (state.powerInKilowatts) "kW" else "W")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(state.powerInKilowatts, { onUpdate { copy(powerInKilowatts = it) } })
                Text("Ingresar potencia en kW")
            }
            if (state.systemType != ElectricalSystemType.DC) NumericInputField("Factor de potencia", state.powerFactor, { onUpdate { copy(powerFactor = it) } })
            NumericInputField("Eficiencia", state.efficiency, { onUpdate { copy(efficiency = it) } }, suffix = "% o 0-1")
        }
        NumericInputField("Longitud de ida", state.length, { onUpdate { copy(length = it) } }, suffix = "m")
        NumericInputField("Sección del conductor", state.section, { onUpdate { copy(section = it) } }, suffix = "mm²")
        EnumSegmentedField("Material", state.material, TechnicalConductorMaterial.entries.toList(), TechnicalConductorMaterial::label) { onUpdate { copy(material = it) } }
        EnumSegmentedField("Temperatura", state.temperatureMode, TemperatureMode.entries.toList(), TemperatureMode::label) { onUpdate { copy(temperatureMode = it) } }
        if (state.temperatureMode == TemperatureMode.CUSTOM) NumericInputField("Temperatura del conductor", state.temperature, { onUpdate { copy(temperature = it) } }, suffix = "°C")
        EnumSegmentedField("Origen del dato", state.source, CalculationSource.entries.toList(), CalculationSource::label) { onUpdate { copy(source = it) } }
        VoltageDropContextFields(state, onUpdate)
        AssociationSummaryCard(state.association, onClearAssociation)
        Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth()) { Text("Calcular") }
        state.errors.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
        state.result?.let { result ->
            CalculationResultCard("Caída porcentual", TechnicalValueFormatter.withUnit(result.voltageDropPercent, "%"), result.classification)
            Text("Caída: ${TechnicalValueFormatter.withUnit(result.voltageDropVolts, "V")} · Tensión final: ${TechnicalValueFormatter.withUnit(result.estimatedEndVoltageVolts, "V")}")
            Text("Corriente utilizada: ${TechnicalValueFormatter.withUnit(result.currentUsedAmps, "A")}")
            FormulaExplanationCard("DC y monofásico: ΔV = 2 x L x I x ρ / S. Trifásico: ΔV = √3 x L x I x ρ / S.")
            TechnicalDisclaimer(VoltageDropCalculator.TECHNICAL_DISCLAIMER)
            CalculationActions(true, onSave, onCopy, onShare, onNew)
        }
    }
}

@Composable
private fun VoltageDropContextFields(
    state: VoltageDropUiState,
    onUpdate: (VoltageDropUiState.() -> VoltageDropUiState) -> Unit,
) {
    if (state.source == CalculationSource.MEASURED) {
        OutlinedTextField(state.instrumentName, { onUpdate { copy(instrumentName = it) } }, label = { Text("Instrumento o referencia") }, modifier = Modifier.fillMaxWidth())
    }
    OutlinedTextField(state.measurementContext, { onUpdate { copy(measurementContext = it) } }, label = { Text("Contexto") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(state.assumptions, { onUpdate { copy(assumptions = it) } }, label = { Text("Supuestos u observaciones") }, modifier = Modifier.fillMaxWidth())
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(state.dataProvidedByClient, { onUpdate { copy(dataProvidedByClient = it) } })
        Text("Datos declarados por el cliente")
    }
}

private fun VoltageDropUiState.asCalculation() = result?.let {
    buildVoltageDropCalculation(savedCalculationId, this.run {
        com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput(
            systemType = systemType,
            nominalVoltageVolts = nominalVoltage.parseDouble() ?: Double.NaN,
            currentMode = currentMode,
            currentAmps = current.parseDouble(),
            activePowerWatts = power.parseDouble()?.let { p -> if (powerInKilowatts) p * 1000 else p },
            powerFactor = powerFactor.parseDouble(),
            efficiency = efficiency.parseDouble()?.let { e -> if (e > 1) e / 100 else e },
            conductorLengthMeters = length.parseDouble() ?: Double.NaN,
            conductorSectionMm2 = section.parseDouble() ?: Double.NaN,
            conductorMaterial = material,
            temperatureMode = temperatureMode,
            conductorTemperatureCelsius = temperature.parseDouble(),
            source = source,
            context = contextFromFields(source, instrumentName, measurementContext, assumptions, dataProvidedByClient),
        )
    }, it, association)
}

@Preview(showBackground = true)
@Composable
private fun VoltageDropEmptyPreview() {
    ElecAppTheme {
        VoltageDropContent(VoltageDropUiState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun VoltageDropReviewPreview() {
    ElecAppTheme {
        VoltageDropContent(VoltageDropUiState(nominalVoltage = "220", current = "20.45", length = "38", section = "2.5"), {}, {}, {}, {}, {}, {}, {})
    }
}
