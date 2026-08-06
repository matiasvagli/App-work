package com.matiasdev.elecapp.features.electricalrules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleSeverity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalRulesSettingsScreen(
    repository: ElectricalRuleConfigRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ElectricalRulesSettingsViewModel = viewModel(
        factory = ElectricalRulesSettingsViewModelFactory(repository),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message?.id) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message.text)
        viewModel.onMessageShown(message.id)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Criterios eléctricos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::askRestoreDefaults) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurar valores iniciales")
                    }
                },
            )
        },
    ) { padding ->
        ElectricalRulesSettingsContent(
            uiState = uiState,
            onRuleClick = viewModel::startEditing,
            onEnabledChange = viewModel::setRuleEnabled,
            onRestoreDefaultsClick = viewModel::askRestoreDefaults,
            modifier = Modifier.padding(padding),
        )
    }

    uiState.editingRule?.let { editState ->
        RuleEditDialog(
            editState = editState,
            saving = uiState.saving,
            onDismiss = viewModel::dismissEditor,
            onEnabledChange = viewModel::onEditEnabledChange,
            onNumericValueChange = viewModel::onEditNumericValueChange,
            onSecondaryNumericValueChange = viewModel::onEditSecondaryNumericValueChange,
            onSeverityChange = viewModel::onEditSeverityChange,
            onFindingTitleChange = viewModel::onEditFindingTitleChange,
            onFindingDescriptionChange = viewModel::onEditFindingDescriptionChange,
            onRecommendationChange = viewModel::onEditRecommendationChange,
            onSave = viewModel::saveEditingRule,
        )
    }

    if (uiState.restoreConfirmationVisible) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRestoreDefaults,
            title = { Text("Restaurar valores iniciales") },
            text = {
                Text(
                    "Se reemplazarán todos los criterios personalizados por los valores iniciales de la aplicación. " +
                        "Esta acción no modifica relevamientos ya guardados.",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::restoreDefaults, enabled = !uiState.saving) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRestoreDefaults, enabled = !uiState.saving) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun ElectricalRulesSettingsContent(
    uiState: ElectricalRulesSettingsUiState,
    onRuleClick: (ElectricalRuleCode) -> Unit,
    onEnabledChange: (ElectricalRuleCode, Boolean) -> Unit,
    onRestoreDefaultsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Cargando criterios eléctricos...")
            }
        }
        uiState.error != null && uiState.sections.all { it.rules.isEmpty() } -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("No se pudieron cargar los criterios eléctricos.", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onRestoreDefaultsClick) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restaurar valores iniciales")
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                item {
                    Text(
                        "Estos valores se utilizan como criterios configurables de trabajo. Modificarlos no cambia " +
                            "la lógica de cálculo ni representa por sí solo una certificación normativa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(uiState.sections, key = { it.type }) { section ->
                    RuleSection(
                        section = section,
                        onRuleClick = onRuleClick,
                        onEnabledChange = onEnabledChange,
                    )
                }
                item {
                    OutlinedButton(
                        onClick = onRestoreDefaultsClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.saving,
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar valores iniciales")
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleSection(
    section: ElectricalRuleSectionUi,
    onRuleClick: (ElectricalRuleCode) -> Unit,
    onEnabledChange: (ElectricalRuleCode, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        section.rules.forEach { rule ->
            RuleCard(
                rule = rule,
                compactConductorLabel = section.type == ElectricalRuleSectionType.CONDUCTOR_PROTECTION,
                onClick = { onRuleClick(rule.code) },
                onEnabledChange = { onEnabledChange(rule.code, it) },
            )
        }
        if (section.type == ElectricalRuleSectionType.CONDUCTOR_PROTECTION) {
            Text(
                "Los valores de protección por sección son criterios iniciales configurables. La capacidad real del " +
                    "conductor puede depender del método de instalación, temperatura, agrupamiento, aislación y otras condiciones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RuleCard(
    rule: ElectricalRuleItemUi,
    compactConductorLabel: Boolean,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (compactConductorLabel && rule.material != null && rule.sectionLabel != null) {
                        "${rule.material} ${rule.sectionLabel}"
                    } else {
                        rule.name
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${rule.value} ${rule.unit}".trim(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text("Severidad: ${rule.severityLabel}", style = MaterialTheme.typography.bodySmall)
                Text(
                    if (rule.enabled) "Activada" else "Desactivada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = rule.enabled, onCheckedChange = onEnabledChange)
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}

@Composable
private fun RuleEditDialog(
    editState: ElectricalRuleEditUiState,
    saving: Boolean,
    onDismiss: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onNumericValueChange: (String) -> Unit,
    onSecondaryNumericValueChange: (String) -> Unit,
    onSeverityChange: (ElectricalRuleSeverity) -> Unit,
    onFindingTitleChange: (String) -> Unit,
    onFindingDescriptionChange: (String) -> Unit,
    onRecommendationChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(editState.title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (editState.material != null && editState.sectionLabel != null) {
                    Text(
                        "${editState.material} ${editState.sectionLabel}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Activada")
                    Switch(checked = editState.enabled, onCheckedChange = onEnabledChange)
                }
                OutlinedTextField(
                    value = editState.numericValueText,
                    onValueChange = onNumericValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Valor principal (${editState.unit})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = editState.numericValueError != null,
                    supportingText = {
                        Text(editState.numericValueError ?: numericSupportingText(editState.code))
                    },
                )
                if (editState.usesSecondaryValue) {
                    OutlinedTextField(
                        value = editState.secondaryNumericValueText.orEmpty(),
                        onValueChange = onSecondaryNumericValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Valor secundario") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = editState.secondaryNumericValueError != null,
                        supportingText = editState.secondaryNumericValueError?.let { { Text(it) } },
                    )
                }
                Text("Severidad", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElectricalRuleSeverity.entries.forEach { severity ->
                        FilterChip(
                            selected = editState.severity == severity,
                            onClick = { onSeverityChange(severity) },
                            label = { Text(severity.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = editState.findingTitle,
                    onValueChange = onFindingTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Título del hallazgo") },
                    isError = editState.findingTitleError != null,
                    supportingText = editState.findingTitleError?.let { { Text(it) } },
                )
                OutlinedTextField(
                    value = editState.findingDescription,
                    onValueChange = onFindingDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción del hallazgo") },
                    minLines = 3,
                    isError = editState.findingDescriptionError != null,
                    supportingText = editState.findingDescriptionError?.let { { Text(it) } },
                )
                OutlinedTextField(
                    value = editState.recommendation,
                    onValueChange = onRecommendationChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Recomendación") },
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = !saving) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(20.dp).height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("Cancelar")
            }
        },
    )
}

private fun numericSupportingText(code: ElectricalRuleCode): String {
    return when (code) {
        ElectricalRuleCode.MIN_SUPPLY_VOLTAGE,
        ElectricalRuleCode.MAX_SUPPLY_VOLTAGE,
        -> "La tensión mínima debe ser menor que la máxima."
        ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT -> "Rango sugerido de trabajo: mayor que 0 % y hasta 20 %."
        else -> "Debe ser mayor que 0."
    }
}
