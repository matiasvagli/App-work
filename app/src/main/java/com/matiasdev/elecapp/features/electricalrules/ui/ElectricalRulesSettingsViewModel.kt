package com.matiasdev.elecapp.features.electricalrules.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleSeverity
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleValueFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ElectricalRulesSettingsViewModel(
    private val repository: ElectricalRuleConfigRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ElectricalRulesSettingsUiState())
    val uiState: StateFlow<ElectricalRulesSettingsUiState> = _uiState.asStateFlow()

    private var latestRules: List<ElectricalRuleConfig> = emptyList()
    private var nextMessageId = 0L

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.observeAll()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            saving = false,
                            error = "No se pudieron cargar los criterios eléctricos.",
                            message = snackbarMessage(error.userMessage("No se pudieron cargar los criterios eléctricos.")),
                        )
                    }
                }
                .collect { rules ->
                    latestRules = rules
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = null,
                            sections = rules.toSections(),
                            editingRule = it.editingRule?.refreshFrom(rules),
                        )
                    }
                }
        }
    }

    fun startEditing(code: ElectricalRuleCode) {
        val rule = latestRules.firstOrNull { it.code == code } ?: return
        _uiState.update { it.copy(editingRule = rule.toEditState()) }
    }

    fun dismissEditor() {
        _uiState.update { it.copy(editingRule = null) }
    }

    fun onEditEnabledChange(enabled: Boolean) {
        updateEditingRule { it.copy(enabled = enabled) }
    }

    fun onEditNumericValueChange(value: String) {
        updateEditingRule { it.copy(numericValueText = value, numericValueError = null) }
    }

    fun onEditSecondaryNumericValueChange(value: String) {
        updateEditingRule { it.copy(secondaryNumericValueText = value, secondaryNumericValueError = null) }
    }

    fun onEditSeverityChange(severity: ElectricalRuleSeverity) {
        updateEditingRule { it.copy(severity = severity) }
    }

    fun onEditFindingTitleChange(value: String) {
        updateEditingRule { it.copy(findingTitle = value, findingTitleError = null) }
    }

    fun onEditFindingDescriptionChange(value: String) {
        updateEditingRule { it.copy(findingDescription = value, findingDescriptionError = null) }
    }

    fun onEditRecommendationChange(value: String) {
        updateEditingRule { it.copy(recommendation = value) }
    }

    fun saveEditingRule() {
        val editState = _uiState.value.editingRule ?: return
        val rule = latestRules.firstOrNull { it.code == editState.code } ?: return
        when (val validation = validate(editState, rule)) {
            is RuleEditValidation.Invalid -> {
                _uiState.update {
                    it.copy(
                        editingRule = editState.copy(
                            numericValueError = validation.numericValueError,
                            secondaryNumericValueError = validation.secondaryNumericValueError,
                            findingTitleError = validation.findingTitleError,
                            findingDescriptionError = validation.findingDescriptionError,
                        ),
                        message = snackbarMessage(validation.message),
                    )
                }
            }
            is RuleEditValidation.Valid -> saveRule(validation.config, validation.warningMessage)
        }
    }

    fun setRuleEnabled(code: ElectricalRuleCode, enabled: Boolean) {
        val rule = latestRules.firstOrNull { it.code == code } ?: return
        saveRule(rule.copy(enabled = enabled), warningMessage = null)
    }

    fun askRestoreDefaults() {
        _uiState.update { it.copy(restoreConfirmationVisible = true) }
    }

    fun dismissRestoreDefaults() {
        _uiState.update { it.copy(restoreConfirmationVisible = false) }
    }

    fun restoreDefaults() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(saving = true, restoreConfirmationVisible = false) }
            runCatching { repository.restoreDefaults() }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            saving = false,
                            editingRule = null,
                            message = snackbarMessage("Valores restaurados"),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            saving = false,
                            message = snackbarMessage(error.userMessage("No se pudieron restaurar los valores iniciales.")),
                        )
                    }
                }
        }
    }

    fun onMessageShown(messageId: Long) {
        _uiState.update { state ->
            if (state.message?.id == messageId) state.copy(message = null) else state
        }
    }

    private fun saveRule(config: ElectricalRuleConfig, warningMessage: String?) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(saving = true) }
            runCatching { repository.save(config) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            saving = false,
                            editingRule = null,
                            message = snackbarMessage(warningMessage ?: "Configuración guardada"),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            saving = false,
                            message = snackbarMessage(error.userMessage("No se pudo guardar la configuración.")),
                        )
                    }
                }
        }
    }

    private fun validate(
        editState: ElectricalRuleEditUiState,
        original: ElectricalRuleConfig,
    ): RuleEditValidation {
        val numericValue = parsePositiveDouble(editState.numericValueText)
        val secondaryValue = editState.secondaryNumericValueText?.let(::parsePositiveDouble)
        val findingTitle = editState.findingTitle.trim()
        val findingDescription = editState.findingDescription.trim()

        val numericError = if (numericValue == null) "Ingresá un valor mayor que 0." else null
        val secondaryError = if (editState.secondaryNumericValueText != null && secondaryValue == null) {
            "Ingresá un valor secundario mayor que 0."
        } else {
            null
        }
        val titleError = if (findingTitle.isBlank()) "Ingresá un título." else null
        val descriptionError = if (findingDescription.isBlank()) "Ingresá una descripción." else null
        if (numericError != null || secondaryError != null || titleError != null || descriptionError != null) {
            return RuleEditValidation.Invalid(
                message = numericError ?: secondaryError ?: titleError ?: descriptionError.orEmpty(),
                numericValueError = numericError,
                secondaryNumericValueError = secondaryError,
                findingTitleError = titleError,
                findingDescriptionError = descriptionError,
            )
        }

        val voltageError = validateVoltageBounds(editState.code, numericValue)
        if (voltageError != null) {
            return RuleEditValidation.Invalid(
                message = voltageError,
                numericValueError = voltageError,
            )
        }

        val warning = if (
            editState.code == ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT &&
            numericValue != null &&
            numericValue > 20.0
        ) {
            "Configuración guardada. Revisá si una caída mayor al 20 % fue ingresada por error."
        } else {
            null
        }

        return RuleEditValidation.Valid(
            config = original.copy(
                enabled = editState.enabled,
                severity = editState.severity,
                numericValue = numericValue,
                secondaryNumericValue = secondaryValue,
                findingTitle = findingTitle,
                findingDescriptionTemplate = findingDescription,
                recommendationTemplate = editState.recommendation.trim().takeIf { it.isNotBlank() },
            ),
            warningMessage = warning,
        )
    }

    private fun validateVoltageBounds(code: ElectricalRuleCode, editedValue: Double?): String? {
        if (editedValue == null) return null
        val minValue = when (code) {
            ElectricalRuleCode.MIN_SUPPLY_VOLTAGE -> editedValue
            else -> latestRules.firstOrNull { it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }?.numericValue
        }
        val maxValue = when (code) {
            ElectricalRuleCode.MAX_SUPPLY_VOLTAGE -> editedValue
            else -> latestRules.firstOrNull { it.code == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }?.numericValue
        }
        return if (
            code in supplyVoltageCodes &&
            minValue != null &&
            maxValue != null &&
            minValue >= maxValue
        ) {
            "La tensión mínima debe ser menor que la tensión máxima."
        } else {
            null
        }
    }

    private fun updateEditingRule(update: (ElectricalRuleEditUiState) -> ElectricalRuleEditUiState) {
        _uiState.update { state ->
            state.copy(editingRule = state.editingRule?.let(update))
        }
    }

    private fun snackbarMessage(text: String): ElectricalRulesSettingsMessage {
        return ElectricalRulesSettingsMessage(id = ++nextMessageId, text = text)
    }
}

data class ElectricalRulesSettingsUiState(
    val loading: Boolean = true,
    val sections: List<ElectricalRuleSectionUi> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null,
    val message: ElectricalRulesSettingsMessage? = null,
    val editingRule: ElectricalRuleEditUiState? = null,
    val restoreConfirmationVisible: Boolean = false,
)

data class ElectricalRulesSettingsMessage(
    val id: Long,
    val text: String,
)

data class ElectricalRuleSectionUi(
    val type: ElectricalRuleSectionType,
    val title: String,
    val rules: List<ElectricalRuleItemUi>,
)

enum class ElectricalRuleSectionType {
    SUPPLY_VOLTAGE,
    VOLTAGE_DROP,
    GROUNDING,
    CONDUCTOR_PROTECTION,
}

data class ElectricalRuleItemUi(
    val code: ElectricalRuleCode,
    val name: String,
    val value: String,
    val unit: String,
    val enabled: Boolean,
    val severity: ElectricalRuleSeverity,
    val severityLabel: String,
    val material: String? = null,
    val sectionLabel: String? = null,
)

data class ElectricalRuleEditUiState(
    val code: ElectricalRuleCode,
    val title: String,
    val unit: String,
    val enabled: Boolean,
    val severity: ElectricalRuleSeverity,
    val numericValueText: String,
    val secondaryNumericValueText: String?,
    val findingTitle: String,
    val findingDescription: String,
    val recommendation: String,
    val material: String?,
    val sectionLabel: String?,
    val numericValueError: String? = null,
    val secondaryNumericValueError: String? = null,
    val findingTitleError: String? = null,
    val findingDescriptionError: String? = null,
) {
    val usesSecondaryValue: Boolean = secondaryNumericValueText != null
}

private sealed interface RuleEditValidation {
    data class Valid(
        val config: ElectricalRuleConfig,
        val warningMessage: String?,
    ) : RuleEditValidation

    data class Invalid(
        val message: String,
        val numericValueError: String? = null,
        val secondaryNumericValueError: String? = null,
        val findingTitleError: String? = null,
        val findingDescriptionError: String? = null,
    ) : RuleEditValidation
}

private val supplyVoltageCodes = setOf(
    ElectricalRuleCode.MIN_SUPPLY_VOLTAGE,
    ElectricalRuleCode.MAX_SUPPLY_VOLTAGE,
)

private val conductorProtectionCodes = listOf(
    ElectricalRuleCode.MAX_CURRENT_COPPER_1_5_MM2,
    ElectricalRuleCode.MAX_CURRENT_COPPER_2_5_MM2,
    ElectricalRuleCode.MAX_CURRENT_COPPER_4_MM2,
    ElectricalRuleCode.MAX_CURRENT_COPPER_6_MM2,
    ElectricalRuleCode.MAX_CURRENT_COPPER_10_MM2,
)

private fun List<ElectricalRuleConfig>.toSections(): List<ElectricalRuleSectionUi> {
    fun item(code: ElectricalRuleCode): ElectricalRuleItemUi? = firstOrNull { it.code == code }?.toItem()
    return listOf(
        ElectricalRuleSectionUi(
            type = ElectricalRuleSectionType.SUPPLY_VOLTAGE,
            title = "Tensión de suministro",
            rules = listOfNotNull(
                item(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE),
                item(ElectricalRuleCode.MAX_SUPPLY_VOLTAGE),
            ),
        ),
        ElectricalRuleSectionUi(
            type = ElectricalRuleSectionType.VOLTAGE_DROP,
            title = "Caída de tensión",
            rules = listOfNotNull(item(ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT)),
        ),
        ElectricalRuleSectionUi(
            type = ElectricalRuleSectionType.GROUNDING,
            title = "Puesta a tierra",
            rules = listOfNotNull(item(ElectricalRuleCode.MAX_GROUND_RESISTANCE_OHMS)),
        ),
        ElectricalRuleSectionUi(
            type = ElectricalRuleSectionType.CONDUCTOR_PROTECTION,
            title = "Protección por sección de conductor",
            rules = conductorProtectionCodes.mapNotNull(::item),
        ),
    )
}

private fun ElectricalRuleConfig.toItem(): ElectricalRuleItemUi {
    val conductor = conductorInfo(code)
    return ElectricalRuleItemUi(
        code = code,
        name = name,
        value = numericValue?.let(ElectricalRuleValueFormatter::decimal).orEmpty(),
        unit = unit.orEmpty(),
        enabled = enabled,
        severity = severity,
        severityLabel = severity.label,
        material = conductor?.material,
        sectionLabel = conductor?.sectionLabel,
    )
}

private fun ElectricalRuleConfig.toEditState(): ElectricalRuleEditUiState {
    val conductor = conductorInfo(code)
    return ElectricalRuleEditUiState(
        code = code,
        title = name,
        unit = unit.orEmpty(),
        enabled = enabled,
        severity = severity,
        numericValueText = numericValue?.let(ElectricalRuleValueFormatter::decimal).orEmpty(),
        secondaryNumericValueText = secondaryNumericValue?.let(ElectricalRuleValueFormatter::decimal),
        findingTitle = findingTitle,
        findingDescription = findingDescriptionTemplate,
        recommendation = recommendationTemplate.orEmpty(),
        material = conductor?.material,
        sectionLabel = conductor?.sectionLabel,
    )
}

private fun ElectricalRuleEditUiState.refreshFrom(rules: List<ElectricalRuleConfig>): ElectricalRuleEditUiState? {
    val latest = rules.firstOrNull { it.code == code } ?: return this
    return copy(
        title = latest.name,
        unit = latest.unit.orEmpty(),
        material = conductorInfo(latest.code)?.material,
        sectionLabel = conductorInfo(latest.code)?.sectionLabel,
    )
}

private data class ConductorInfo(
    val material: String,
    val sectionLabel: String,
)

private fun conductorInfo(code: ElectricalRuleCode): ConductorInfo? = when (code) {
    ElectricalRuleCode.MAX_CURRENT_COPPER_1_5_MM2 -> ConductorInfo("Cobre", "1,5 mm²")
    ElectricalRuleCode.MAX_CURRENT_COPPER_2_5_MM2 -> ConductorInfo("Cobre", "2,5 mm²")
    ElectricalRuleCode.MAX_CURRENT_COPPER_4_MM2 -> ConductorInfo("Cobre", "4 mm²")
    ElectricalRuleCode.MAX_CURRENT_COPPER_6_MM2 -> ConductorInfo("Cobre", "6 mm²")
    ElectricalRuleCode.MAX_CURRENT_COPPER_10_MM2 -> ConductorInfo("Cobre", "10 mm²")
    else -> null
}

private fun parsePositiveDouble(value: String): Double? {
    val parsed = value.trim().replace(',', '.').toDoubleOrNull() ?: return null
    return parsed.takeIf { !it.isNaN() && !it.isInfinite() && it > 0.0 }
}

val ElectricalRuleSeverity.label: String
    get() = when (this) {
        ElectricalRuleSeverity.CRITICAL -> "Crítico"
        ElectricalRuleSeverity.IMPORTANT -> "Importante"
        ElectricalRuleSeverity.RECOMMENDED -> "Recomendado"
    }

private fun Throwable.userMessage(fallback: String): String {
    val details = message?.takeIf { it.isNotBlank() }
    return if (details == null) fallback else "$fallback $details"
}

class ElectricalRulesSettingsViewModelFactory(
    private val repository: ElectricalRuleConfigRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ElectricalRulesSettingsViewModel(repository) as T
    }
}
