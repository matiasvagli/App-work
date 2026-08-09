package com.matiasdev.elecapp.features.electricaltools.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalVariable
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalCalculationTextGenerator
import com.matiasdev.elecapp.features.electricaltools.summary.TechnicalValueFormatter
import com.matiasdev.elecapp.features.electricaltools.summary.label
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerCurrentVoltageScreen(
    repository: TechnicalCalculationRepository,
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    inspectionRepository: InspectionRepository,
    electricalRuleConfigRepository: ElectricalRuleConfigRepository,
    clientId: String?,
    visitId: String?,
    inspectionId: String?,
    duplicateId: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PowerCurrentVoltageViewModel = viewModel(
        factory = PowerCurrentVoltageViewModelFactory(
            repository,
            clientRepository,
            visitRepository,
            inspectionRepository,
            clientId,
            visitId,
            inspectionId,
            duplicateId,
            electricalRuleConfigRepository,
        ),
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
                title = { Text("Potencia, corriente y tensión", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        PowerCurrentVoltageContent(
            state = state,
            onUpdate = viewModel::update,
            onCalculate = viewModel::calculate,
            onSave = viewModel::save,
            onClearAssociation = viewModel::clearAssociation,
            onNew = viewModel::newCalculation,
            onCopy = {
                state.asCalculation()?.let { clipboard.setText(AnnotatedString(TechnicalCalculationTextGenerator.generate(it))) }
            },
            onShare = {
                state.asCalculation()?.let { shareCalculationText(context, TechnicalCalculationTextGenerator.generate(it)) }
            },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun PowerCurrentVoltageContent(
    state: PowerCurrentVoltageUiState,
    onUpdate: (PowerCurrentVoltageUiState.() -> PowerCurrentVoltageUiState) -> Unit,
    onCalculate: () -> Unit,
    onSave: () -> Unit,
    onClearAssociation: () -> Unit,
    onNew: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Section: Configuración del Sistema
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
                    text = "Configuración del cálculo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                EnumSegmentedField("Sistema eléctrico", state.systemType, ElectricalSystemType.entries.toList(), ElectricalSystemType::label) {
                    onUpdate { copy(systemType = it) }
                }
                EnumSegmentedField("Variable a calcular", state.variableToCalculate, ElectricalVariable.entries.toList(), ElectricalVariable::label) {
                    onUpdate { copy(variableToCalculate = it) }
                }
            }
        }

        // Section: Parámetros de Entrada
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
                    text = "Parámetros de entrada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (state.variableToCalculate != ElectricalVariable.VOLTAGE) {
                    NumericInputField("Tensión nominal", state.voltage, { onUpdate { copy(voltage = it) } }, suffix = "V", leadingIcon = Icons.Default.Bolt)
                }
                if (state.variableToCalculate != ElectricalVariable.CURRENT) {
                    NumericInputField("Corriente", state.current, { onUpdate { copy(current = it) } }, suffix = "A", leadingIcon = Icons.Default.ElectricMeter)
                }
                if (state.variableToCalculate != ElectricalVariable.POWER) {
                    NumericInputField(
                        "Potencia activa",
                        state.power,
                        { onUpdate { copy(power = it) } },
                        suffix = if (state.powerInKilowatts) "kW" else "W",
                        leadingIcon = Icons.Default.Speed,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(state.powerInKilowatts, { onUpdate { copy(powerInKilowatts = it) } })
                        Text("Ingresar potencia en kW", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (state.systemType != ElectricalSystemType.DC) {
                    NumericInputField("Factor de potencia (cos φ)", state.powerFactor, { onUpdate { copy(powerFactor = it) } })
                }
                NumericInputField("Eficiencia (η)", state.efficiency, { onUpdate { copy(efficiency = it) } }, suffix = "% o 0-1")
            }
        }

        // Section: Origen y Observaciones
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
                    text = "Origen de datos y notas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                EnumSegmentedField("Origen del dato", state.source, CalculationSource.entries.toList(), CalculationSource::label) {
                    onUpdate { copy(source = it) }
                }
                ContextFields(state, onUpdate)
            }
        }

        AssociationSummaryCard(state.association, onClearAssociation)

        Button(
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calcular resultado", fontWeight = FontWeight.Bold)
        }

        state.errors.forEach {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        state.result?.let { result ->
            val value = when (result.calculatedVariable) {
                ElectricalVariable.POWER -> TechnicalValueFormatter.withUnit(result.powerWatts, "W")
                ElectricalVariable.CURRENT -> TechnicalValueFormatter.withUnit(result.currentAmps, "A")
                ElectricalVariable.VOLTAGE -> TechnicalValueFormatter.withUnit(result.voltageVolts, "V")
            }
            CalculationResultCard("Resultado principal", value, null)
            FormulaExplanationCard("DC: P = V x I. AC monofásico: P = V x I x cosφ x η. AC trifásico: P = √3 x V x I x cosφ x η.")
            TechnicalDisclaimer("Eficiencia vacía se asume como 100 %. En AC, si el factor de potencia no se informa, solo se estima con supuesto visible.")
            CalculationActions(true, onSave, onCopy, onShare, onNew)
        }
    }
}

@Composable
private fun ContextFields(
    state: PowerCurrentVoltageUiState,
    onUpdate: (PowerCurrentVoltageUiState.() -> PowerCurrentVoltageUiState) -> Unit,
) {
    if (state.source == CalculationSource.MEASURED) {
        OutlinedTextField(
            value = state.instrumentName,
            onValueChange = { onUpdate { copy(instrumentName = it) } },
            label = { Text("Instrumento o referencia") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    OutlinedTextField(
        value = state.measurementContext,
        onValueChange = { onUpdate { copy(measurementContext = it) } },
        label = { Text("Contexto de medición") },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.assumptions,
        onValueChange = { onUpdate { copy(assumptions = it) } },
        label = { Text("Supuestos u observaciones") },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(state.dataProvidedByClient, { onUpdate { copy(dataProvidedByClient = it) } })
        Text("Datos declarados por el cliente", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun PowerCurrentVoltageUiState.asCalculation() = result?.let {
    buildPowerCalculation(savedCalculationId, this.run {
        com.matiasdev.elecapp.features.electricaltools.domain.PowerCurrentVoltageInput(
            systemType = systemType,
            variableToCalculate = variableToCalculate,
            voltageVolts = voltage.parseDouble(),
            currentAmps = current.parseDouble(),
            activePowerWatts = power.parseDouble()?.let { p -> if (powerInKilowatts) p * 1000 else p },
            powerFactor = powerFactor.parseDouble(),
            efficiency = efficiency.parseDouble()?.let { e -> if (e > 1) e / 100 else e },
            source = source,
            context = contextFromFields(source, instrumentName, measurementContext, assumptions, dataProvidedByClient),
        )
    }, it, association)
}

@Preview(showBackground = true)
@Composable
private fun PowerEmptyPreview() {
    ElecAppTheme {
        PowerCurrentVoltageContent(PowerCurrentVoltageUiState(), {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PowerWithDataPreview() {
    ElecAppTheme {
        PowerCurrentVoltageContent(PowerCurrentVoltageUiState(voltage = "220", power = "4500", powerFactor = "0.9"), {}, {}, {}, {}, {}, {}, {})
    }
}

