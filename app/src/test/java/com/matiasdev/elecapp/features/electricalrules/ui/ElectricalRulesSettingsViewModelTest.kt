package com.matiasdev.elecapp.features.electricalrules.ui

import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleSeverity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ElectricalRulesSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `carga todas las reglas`() = runTest {
        val viewModel = ElectricalRulesSettingsViewModel(FakeElectricalRuleConfigRepository(), testDispatcher)

        val loadedRules = viewModel.uiState.value.sections.flatMap { it.rules }

        assertFalse(viewModel.uiState.value.loading)
        assertEquals(ElectricalRuleCode.entries.toSet(), loadedRules.map { it.code }.toSet())
    }

    @Test
    fun `agrupa tension caida y protecciones`() = runTest {
        val viewModel = ElectricalRulesSettingsViewModel(FakeElectricalRuleConfigRepository(), testDispatcher)

        val sections = viewModel.uiState.value.sections
        val supplySection = sections.first { it.type == ElectricalRuleSectionType.SUPPLY_VOLTAGE }
        val voltageDropSection = sections.first { it.type == ElectricalRuleSectionType.VOLTAGE_DROP }
        val protectionSection = sections.first { it.type == ElectricalRuleSectionType.CONDUCTOR_PROTECTION }

        assertEquals(listOf(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE, ElectricalRuleCode.MAX_SUPPLY_VOLTAGE), supplySection.rules.map { it.code })
        assertEquals(listOf(ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT), voltageDropSection.rules.map { it.code })
        assertEquals(5, protectionSection.rules.size)
        assertEquals("190", supplySection.rules.first().value)
        assertEquals("Cobre", protectionSection.rules.first().material)
        assertEquals("1,5 mm²", protectionSection.rules.first().sectionLabel)
    }

    @Test
    fun `guarda un valor valido aceptando coma decimal`() = runTest {
        val repository = FakeElectricalRuleConfigRepository()
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.startEditing(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE)
        viewModel.onEditNumericValueChange("195,5")
        viewModel.saveEditingRule()

        assertEquals(195.5, repository.savedConfigs.last().numericValue ?: 0.0, 0.0)
    }

    @Test
    fun `rechaza tension minima mayor o igual a maxima`() = runTest {
        val repository = FakeElectricalRuleConfigRepository()
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.startEditing(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE)
        viewModel.onEditNumericValueChange("250")
        viewModel.saveEditingRule()

        assertTrue(repository.savedConfigs.isEmpty())
        assertEquals("La tensión mínima debe ser menor que la tensión máxima.", viewModel.uiState.value.editingRule?.numericValueError)
    }

    @Test
    fun `rechaza valores cero o negativos`() = runTest {
        val repository = FakeElectricalRuleConfigRepository()
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.startEditing(ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT)
        viewModel.onEditNumericValueChange("0")
        viewModel.saveEditingRule()

        assertTrue(repository.savedConfigs.isEmpty())
        assertEquals("Ingresá un valor mayor que 0.", viewModel.uiState.value.editingRule?.numericValueError)
    }

    @Test
    fun `activa y desactiva una regla`() = runTest {
        val repository = FakeElectricalRuleConfigRepository()
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.setRuleEnabled(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE, false)
        viewModel.setRuleEnabled(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE, true)

        assertFalse(repository.savedConfigs.first().enabled)
        assertTrue(repository.savedConfigs.last().enabled)
    }

    @Test
    fun `cambia la severidad`() = runTest {
        val repository = FakeElectricalRuleConfigRepository()
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.startEditing(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE)
        viewModel.onEditSeverityChange(ElectricalRuleSeverity.CRITICAL)
        viewModel.saveEditingRule()

        assertEquals(ElectricalRuleSeverity.CRITICAL, repository.savedConfigs.last().severity)
    }

    @Test
    fun `restaura los valores iniciales`() = runTest {
        val modifiedRules = DefaultElectricalRuleConfigs.all.map {
            if (it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE) it.copy(numericValue = 180.0) else it
        }
        val repository = FakeElectricalRuleConfigRepository(modifiedRules)
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.askRestoreDefaults()
        viewModel.restoreDefaults()

        assertEquals(1, repository.restoreDefaultsCalls)
        assertEquals(DefaultElectricalRuleConfigs.all, repository.rules.value)
        assertEquals("Valores restaurados", viewModel.uiState.value.message?.text)
    }

    @Test
    fun `informa errores del repositorio`() = runTest {
        val repository = FakeElectricalRuleConfigRepository().apply {
            saveFailure = IllegalStateException("falló Room")
        }
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)

        viewModel.startEditing(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE)
        viewModel.onEditNumericValueChange("195")
        viewModel.saveEditingRule()

        assertTrue(viewModel.uiState.value.message?.text.orEmpty().contains("No se pudo guardar la configuración."))
        assertFalse(viewModel.uiState.value.saving)
    }

    @Test
    fun `no modifica el codigo interno ni la version de una regla`() = runTest {
        val repository = FakeElectricalRuleConfigRepository()
        val viewModel = ElectricalRulesSettingsViewModel(repository, testDispatcher)
        val original = DefaultElectricalRuleConfigs.all.first { it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }

        viewModel.startEditing(original.code)
        viewModel.onEditNumericValueChange("196")
        viewModel.saveEditingRule()

        val saved = repository.savedConfigs.last()
        assertEquals(original.code, saved.code)
        assertEquals(original.configVersion, saved.configVersion)
    }
}

private class FakeElectricalRuleConfigRepository(
    initialRules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    private val observeFailure: Throwable? = null,
) : ElectricalRuleConfigRepository {
    val rules = MutableStateFlow(initialRules)
    val savedConfigs = mutableListOf<ElectricalRuleConfig>()
    var restoreDefaultsCalls = 0
    var saveFailure: Throwable? = null
    var restoreFailure: Throwable? = null

    override fun observeAll(): Flow<List<ElectricalRuleConfig>> {
        val failure = observeFailure
        return if (failure == null) rules else flow { throw failure }
    }

    override fun observeByCode(code: ElectricalRuleCode): Flow<ElectricalRuleConfig?> {
        return rules.map { values -> values.firstOrNull { it.code == code } }
    }

    override suspend fun getByCode(code: ElectricalRuleCode): ElectricalRuleConfig? {
        return rules.value.firstOrNull { it.code == code }
    }

    override suspend fun save(config: ElectricalRuleConfig) {
        saveFailure?.let { throw it }
        savedConfigs += config
        rules.value = rules.value.map { if (it.code == config.code) config else it }
    }

    override suspend fun saveAll(configs: List<ElectricalRuleConfig>) {
        configs.forEach { save(it) }
    }

    override suspend fun restoreDefaults() {
        restoreFailure?.let { throw it }
        restoreDefaultsCalls += 1
        rules.value = DefaultElectricalRuleConfigs.all
    }
}
