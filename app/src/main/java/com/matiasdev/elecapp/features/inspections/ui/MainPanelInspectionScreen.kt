package com.matiasdev.elecapp.features.inspections.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.BreakerCurve
import com.matiasdev.elecapp.features.inspections.domain.CircuitDestination
import com.matiasdev.elecapp.features.inspections.domain.circuitDestinationsWithFreeText
import com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.summary.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPanelInspectionScreen(
    repository: InspectionRepository,
    inspectionId: String,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainPanelInspectionViewModel = viewModel(factory = MainPanelInspectionViewModelFactory(repository, inspectionId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Tablero principal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
        } else {
            MainPanelForm(uiState, viewModel, onPreviousClick, onNextClick, onHomeClick, Modifier.padding(padding))
        }
    }
}

@Composable
private fun MainPanelForm(
    uiState: MainPanelInspectionUiState,
    viewModel: MainPanelInspectionViewModel,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        InspectionFormBlock("Estado general") {
            InspectionDropdownField("Estado de la sección", uiState.reviewStatus, InspectionSectionReviewStatus.entries.toList(), InspectionSectionReviewStatus::label) {
                viewModel.update { copy(reviewStatus = it) }
            }
            if (uiState.reviewStatus == InspectionSectionReviewStatus.NOT_VERIFIED && uiState.scope == InspectionScope.VISUAL_INSPECTION) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.addToUnverified,
                        onCheckedChange = { checked -> viewModel.update { copy(addToUnverified = checked) } },
                    )
                    Text("Agregar a elementos no verificados")
                }
            }
            InspectionDropdownField("Accesibilidad", uiState.accessible, AccessStatus.entries.toList(), AccessStatus::label) {
                viewModel.update { copy(accessible = it) }
            }
            InspectionDropdownField("Estado general", uiState.generalCondition, listOf(GeneralCondition.GOOD, GeneralCondition.FAIR, GeneralCondition.POOR, GeneralCondition.NOT_ASSESSED), GeneralCondition::label) {
                viewModel.update { copy(generalCondition = it) }
            }
        }
        if (uiState.reviewStatus != InspectionSectionReviewStatus.REVIEWED || uiState.accessible == AccessStatus.NO) {
            SavedIndicator(uiState)
            InspectionSectionNavigation(onPreviousClick = onPreviousClick, onNextClick = onNextClick, onHomeClick = onHomeClick)
            return@Column
        }
        InspectionFormBlock("Alimentación al tablero") {
            DecimalField(
                "Distancia aproximada desde pilar (m)",
                uiState.feederDistanceMeters,
                { viewModel.update { copy(feederDistanceMeters = it) } },
                uiState.feederDistanceError,
            )
            InspectionDropdownField("Sección del conductor de alimentación", uiState.feederConductorSectionMm2, feederConductorSectionOptions, ::sectionOptionLabel) {
                viewModel.update { copy(feederConductorSectionMm2 = it) }
            }
            InspectionDropdownField("Material", uiState.feederConductorMaterial, ConductorMaterial.entries.toList(), ConductorMaterial::label) {
                viewModel.update { copy(feederConductorMaterial = it) }
            }
            // Solo tiene sentido preguntar el origen cuando hay algun dato cuyo origen declarar.
            if (uiState.feederDistanceMeters.isNotBlank() || uiState.feederConductorSectionMm2 != null) {
                InspectionDropdownField("Origen del dato", uiState.feederDataOrigin, measurementOrigins, MeasurementOrigin::label) {
                    viewModel.update { copy(feederDataOrigin = it) }
                }
            }
        }
        InspectionFormBlock("Tensión de entrada al tablero") {
            MeasurementForm(
                uiState = uiState,
                viewModel = viewModel,
                section = MainPanelMeasurementSection.INPUT_VOLTAGE,
                types = if (uiState.supplyType == SupplyType.THREE_PHASE) threePhaseInputVoltageTypes else singlePhaseInputVoltageTypes,
            )
            MeasurementSummary(uiState.measurements.filter { it.section == MainPanelMeasurementSection.INPUT_VOLTAGE }, viewModel)
        }
        InspectionFormBlock("Interruptor diferencial") {
            InspectionDropdownField("Estado", uiState.differentialPresent, YesNoUnknown.entries.toList(), YesNoUnknown::label) {
                viewModel.update { copy(differentialPresent = it) }
            }
            if (uiState.differentialPresent == YesNoUnknown.YES) {
                InspectionDropdownField("Corriente nominal", uiState.differentialRatedAmps, differentialAmpOptions, ::ampOptionLabel) {
                    viewModel.update { copy(differentialRatedAmps = it, differentialOtherRatedAmps = "") }
                }
                if (uiState.differentialRatedAmps == MAIN_PANEL_OTHER_VALUE) {
                    NumberField("Otra corriente nominal", uiState.differentialOtherRatedAmps, { viewModel.update { copy(differentialOtherRatedAmps = it.filter(Char::isDigit)) } }, uiState.ratedAmpsError)
                }
                InspectionDropdownField("Sensibilidad diferencial", uiState.differentialSensitivityMa, differentialSensitivityOptions, ::sensitivityOptionLabel) {
                    viewModel.update { copy(differentialSensitivityMa = it, differentialOtherSensitivityMa = "") }
                }
                if (uiState.differentialSensitivityMa == MAIN_PANEL_OTHER_VALUE) {
                    NumberField("Otra sensibilidad", uiState.differentialOtherSensitivityMa, { viewModel.update { copy(differentialOtherSensitivityMa = it.filter(Char::isDigit)) } }, uiState.sensitivityError)
                }
                InspectionDropdownField("Prueba manual", uiState.differentialTestResult, DifferentialTestResult.entries.toList(), DifferentialTestResult::label) {
                    viewModel.update { copy(differentialTestResult = it) }
                }
            }
        }
        InspectionFormBlock("Circuitos y protecciones") {
            NumberField("Cantidad de circuitos", uiState.circuitCount, viewModel::updateCircuitCount, uiState.circuitCountError)
            CircuitList(uiState, viewModel)
        }
        InspectionFormBlock("Cableado, borneras y riesgos visibles") {
            InspectionDropdownField("Colores de conductores", uiState.conductorColorStatus, ConductorColorStatus.entries.toList(), ConductorColorStatus::label) {
                viewModel.update { copy(conductorColorStatus = it) }
            }
            yesNoField("Bornera de neutro", uiState.neutralBarPresent) { viewModel.update { copy(neutralBarPresent = it) } }
            yesNoField("Bornera de tierra", uiState.groundBarPresent) { viewModel.update { copy(groundBarPresent = it) } }
            yesNoField("Neutro y tierra separados", uiState.neutralAndGroundSeparated) { viewModel.update { copy(neutralAndGroundSeparated = it) } }
            InspectionDropdownField("Conductores de protección presentes", uiState.protectionConductorsPresent, YesNoPartialUnknown.entries.toList(), YesNoPartialUnknown::label) {
                viewModel.update { copy(protectionConductorsPresent = it) }
            }
            yesNoField("Empalmes improvisados", uiState.improvisedConnections) { viewModel.update { copy(improvisedConnections = it) } }
            yesNoField("Signos de calentamiento o recalentamiento", uiState.overheatingSigns) { viewModel.update { copy(overheatingSigns = it) } }
            yesNoField("Partes expuestas, aislación dañada o riesgo de contacto", uiState.exposedPartsOrDamagedInsulation) {
                viewModel.update { copy(exposedPartsOrDamagedInsulation = it) }
            }
            InspectionDropdownField("Compatibilidad protección/conductor", uiState.protectionCompatibility, ProtectionCompatibility.entries.toList(), ProtectionCompatibility::label) {
                viewModel.update { copy(protectionCompatibility = it) }
            }
            InspectionTextField("Observación del bloque", uiState.wiringRisksNotes, { viewModel.update { copy(wiringRisksNotes = it) } }, minLines = 2)
        }
        InspectionFormBlock("Verificación rápida del conductor de protección") {
            Text("Verificación orientativa. No reemplaza la medición de resistencia de puesta a tierra con telurómetro ni una certificación correspondiente.")
            MeasurementForm(
                uiState = uiState,
                viewModel = viewModel,
                section = MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK,
                types = protectionConductorTypes,
                origins = listOf(MeasurementOrigin.MEASURED, MeasurementOrigin.ESTIMATED, MeasurementOrigin.NOT_VERIFIED),
            )
            InspectionDropdownField("Resultado orientativo", uiState.protectionConductorCheckResult, ProtectionConductorCheckResult.entries.toList(), ProtectionConductorCheckResult::label) {
                viewModel.update { copy(protectionConductorCheckResult = it) }
            }
            MeasurementSummary(uiState.measurements.filter { it.section == MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK }, viewModel)
        }
        InspectionFormBlock("Observación general") {
            InspectionTextField("Observación", uiState.notes, { viewModel.update { copy(notes = it) } }, minLines = 3)
        }
        SavedIndicator(uiState)
        InspectionSectionNavigation(onPreviousClick = onPreviousClick, onNextClick = onNextClick, onHomeClick = onHomeClick)
    }
}

@Composable
private fun CircuitList(uiState: MainPanelInspectionUiState, viewModel: MainPanelInspectionViewModel) {
    if (uiState.circuits.isEmpty()) {
        Text("Sin circuitos registrados")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // key por id: la lista se reemplaza entera al recargar circuitos, y sin key los
        // slots se identifican por posición y el editor abierto se recrea al reordenar.
        uiState.circuits.forEachIndexed { index, circuit ->
            key(circuit.id) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = circuit.summary(index),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.toggleCircuitExpanded(circuit.id) }) {
                        Text(
                            text = if (circuit.id in uiState.expandedCircuitIds) "Contraer" else "Editar",
                            maxLines = 1,
                        )
                    }
                }
                if (circuit.id in uiState.expandedCircuitIds) {
                    CircuitEditor(circuit, uiState.circuitInputs[circuit.id] ?: CircuitNumberInputs(), viewModel)
                }
            }
        }
    }
}

@Composable
private fun CircuitEditor(circuit: MainPanelCircuit, inputs: CircuitNumberInputs, viewModel: MainPanelInspectionViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InspectionDropdownField("Identificación o destino", circuit.destination, CircuitDestination.entries.toList(), CircuitDestination::label) {
            viewModel.updateCircuit(circuit.copy(destination = it, destinationOther = null))
        }
        if (circuit.destination in circuitDestinationsWithFreeText) {
            InspectionTextField("Qué alimenta", circuit.destinationOther.orEmpty(), { viewModel.updateCircuit(circuit.copy(destinationOther = it.ifBlank { null })) })
        }
        InspectionDropdownField("Térmica", circuit.breakerAmps?.toString() ?: circuit.breakerOtherAmps?.let { MAIN_PANEL_OTHER_VALUE }.orEmpty(), breakerOptions, ::ampOptionLabel) {
            viewModel.updateCircuit(
                circuit.copy(
                    breakerAmps = it.toIntOrNull(),
                    breakerOtherAmps = if (it == MAIN_PANEL_OTHER_VALUE) 0 else null,
                ),
            )
        }
        if (circuit.breakerOtherAmps != null) {
            NumberField("Otra térmica", inputs.breakerOther, { value -> viewModel.updateCircuitBreakerOther(circuit, value) }, null)
        }
        InspectionDropdownField("Curva", circuit.breakerCurve, BreakerCurve.entries.toList(), BreakerCurve::label) {
            viewModel.updateCircuit(circuit.copy(breakerCurve = it))
        }
        InspectionDropdownField("Sección del conductor", circuit.conductorSectionMm2?.formatNumber() ?: circuit.conductorOtherSectionMm2?.let { MAIN_PANEL_OTHER_VALUE }.orEmpty(), conductorSectionOptions, ::sectionOptionLabel) {
            viewModel.updateCircuit(
                circuit.copy(
                    conductorSectionMm2 = it.toDoubleOrNull(),
                    conductorOtherSectionMm2 = if (it == MAIN_PANEL_OTHER_VALUE) 0.0 else null,
                ),
            )
        }
        if (circuit.conductorOtherSectionMm2 != null) {
            DecimalField("Otra sección mm²", inputs.sectionOther, { value -> viewModel.updateCircuitSectionOther(circuit, value) }, null)
        }
        InspectionDropdownField("Material del conductor", circuit.conductorMaterial, ConductorMaterial.entries.toList(), ConductorMaterial::label) {
            viewModel.updateCircuit(circuit.copy(conductorMaterial = it, conductorMaterialOther = null))
        }
        if (circuit.conductorMaterial == ConductorMaterial.OTHER) {
            InspectionTextField("Otro material", circuit.conductorMaterialOther.orEmpty(), { viewModel.updateCircuit(circuit.copy(conductorMaterialOther = it.ifBlank { null })) })
        }
        InspectionDropdownField("Origen del consumo", circuit.consumptionOrigin, consumptionOrigins, MeasurementOrigin::label) {
            viewModel.updateCircuit(circuit.copy(consumptionOrigin = it, consumptionAmps = if (it == MeasurementOrigin.NOT_VERIFIED) null else circuit.consumptionAmps))
        }
        if (circuit.consumptionOrigin != MeasurementOrigin.NOT_VERIFIED) {
            DecimalField("Consumo del circuito A", inputs.consumption, { value -> viewModel.updateCircuitConsumption(circuit, value) }, null)
        }
        InspectionTextField("Observación", circuit.notes.orEmpty(), { viewModel.updateCircuit(circuit.copy(notes = it.ifBlank { null })) }, minLines = 2)
    }
}

@Composable
private fun MeasurementForm(
    uiState: MainPanelInspectionUiState,
    viewModel: MainPanelInspectionViewModel,
    section: MainPanelMeasurementSection,
    types: List<MainPanelMeasurementType>,
    origins: List<MeasurementOrigin> = measurementOrigins,
) {
    val selectedType = uiState.measurementType.takeIf { it in types } ?: types.first()
    InspectionDropdownField("Medición", selectedType, types, MainPanelMeasurementType::label) {
        viewModel.updateMeasurementDraft(section = section, type = it)
    }
    InspectionDropdownField("Origen", uiState.measurementOrigin, origins, MeasurementOrigin::label) {
        viewModel.updateMeasurementDraft(section = section, origin = it)
    }
    if (uiState.measurementOrigin != MeasurementOrigin.NOT_VERIFIED) {
        DecimalField("Valor V", uiState.measurementValue, { viewModel.updateMeasurementDraft(section = section, value = it) }, uiState.measurementError)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { viewModel.updateMeasurementDraft(section = section, type = selectedType); viewModel.saveMeasurement() }, enabled = uiState.status == InspectionStatus.DRAFT) {
            Text(if (uiState.editingMeasurementId == null) "Agregar tensión" else "Actualizar tensión")
        }
        if (uiState.editingMeasurementId != null) {
            TextButton(onClick = viewModel::cancelMeasurementEdit) { Text("Cancelar") }
        }
    }
}

@Composable
private fun MeasurementSummary(measurements: List<MainPanelMeasurement>, viewModel: MainPanelInspectionViewModel) {
    if (measurements.isEmpty()) {
        Text("Sin mediciones registradas")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        measurements.forEach { measurement ->
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${measurement.type.label()}: ${measurement.formatValue()} (${measurement.origin.label()})", modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.editMeasurement(measurement) }) { Text("Editar") }
                TextButton(onClick = { viewModel.deleteMeasurement(measurement.id) }) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun SavedIndicator(uiState: MainPanelInspectionUiState) {
    if (uiState.saved && uiState.status == InspectionStatus.DRAFT) Text("Cambios guardados automáticamente")
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
    )
}

@Composable
private fun DecimalField(label: String, value: String, onChange: (String) -> Unit, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { char -> char.isDigit() || char == '.' || char == ',' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
    )
}

@Composable
private fun yesNoField(label: String, value: YesNoUnknown, onChange: (YesNoUnknown) -> Unit) {
    InspectionDropdownField(label, value, YesNoUnknown.entries.toList(), YesNoUnknown::label, onValueChange = onChange)
}

private val singlePhaseInputVoltageTypes = listOf(MainPanelMeasurementType.INPUT_VOLTAGE_LN)
private val threePhaseInputVoltageTypes = listOf(
    MainPanelMeasurementType.INPUT_VOLTAGE_L1_N,
    MainPanelMeasurementType.INPUT_VOLTAGE_L2_N,
    MainPanelMeasurementType.INPUT_VOLTAGE_L3_N,
    MainPanelMeasurementType.INPUT_VOLTAGE_L1_L2,
    MainPanelMeasurementType.INPUT_VOLTAGE_L2_L3,
    MainPanelMeasurementType.INPUT_VOLTAGE_L3_L1,
)
private val protectionConductorTypes = listOf(
    MainPanelMeasurementType.PROTECTION_VOLTAGE_PHASE_GROUND,
    MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND,
)
private val measurementOrigins = listOf(MeasurementOrigin.MEASURED, MeasurementOrigin.ESTIMATED, MeasurementOrigin.DECLARED_BY_CLIENT, MeasurementOrigin.NOT_VERIFIED)
private val consumptionOrigins = measurementOrigins
private val differentialAmpOptions = listOf("", "25", "40", "63", MAIN_PANEL_OTHER_VALUE)
private val differentialSensitivityOptions = listOf("", "30", "100", "300", MAIN_PANEL_OTHER_VALUE)
private val breakerOptions = listOf("", "6", "10", "16", "20", "25", "32", "40", "50", "63", MAIN_PANEL_OTHER_VALUE)
private val conductorSectionOptions = listOf("", "1.5", "2.5", "4", "6", "10", "16", "25", MAIN_PANEL_OTHER_VALUE)
private val feederConductorSectionOptions = conductorSectionOptions.filterNot { it == MAIN_PANEL_OTHER_VALUE }

private fun ampOptionLabel(value: String): String = when (value) {
    "" -> "No verificada"
    MAIN_PANEL_OTHER_VALUE -> "Otro"
    else -> "$value A"
}

private fun sensitivityOptionLabel(value: String): String = when (value) {
    "" -> "No verificada"
    MAIN_PANEL_OTHER_VALUE -> "Otro"
    else -> "$value mA"
}

private fun sectionOptionLabel(value: String): String = when (value) {
    "" -> "No verificada"
    MAIN_PANEL_OTHER_VALUE -> "Otro"
    else -> "${value.replace(".", ",")} mm²"
}

private fun MainPanelCircuit.summary(index: Int): String {
    val destinationText = if (destination == CircuitDestination.UNIDENTIFIED) {
        "sin identificar"
    } else {
        destinationOther?.takeIf(String::isNotBlank) ?: destination.label()
    }
    val breaker = (breakerAmps ?: breakerOtherAmps?.takeIf { it > 0 })?.let { "$it A" }
    val section = (conductorSectionMm2 ?: conductorOtherSectionMm2?.takeIf { it > 0.0 })?.let { "${it.formatNumber()} mm²" }
    return listOf("Circuito ${index + 1}", destinationText, breaker, section).filterNotNull().joinToString(" · ")
}

private fun MainPanelMeasurement.formatValue(): String = value?.let { "${it.formatNumber()} $unit" } ?: "No verificado"

private fun Double.formatNumber(): String {
    val whole = toLong()
    return if (this == whole.toDouble()) whole.toString() else toString().replace(".", ",")
}
