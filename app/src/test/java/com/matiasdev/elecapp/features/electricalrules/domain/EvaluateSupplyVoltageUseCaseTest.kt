package com.matiasdev.elecapp.features.electricalrules.domain

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluateSupplyVoltageUseCaseTest {
    @Test
    fun `gets both rules from repository`() = runTest {
        val repository = FakeElectricalRuleConfigRepository(defaultConfigs())
        val useCase = EvaluateSupplyVoltageUseCase(repository)

        useCase(input(220.0))

        assertEquals(
            listOf(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE, ElectricalRuleCode.MAX_SUPPLY_VOLTAGE),
            repository.getCalls,
        )
    }

    @Test
    fun `returns minimum and maximum evaluations in order`() = runTest {
        val useCase = EvaluateSupplyVoltageUseCase(FakeElectricalRuleConfigRepository(defaultConfigs()))

        val results = useCase(input(220.0))

        assertEquals(
            listOf(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE, ElectricalRuleCode.MAX_SUPPLY_VOLTAGE),
            results.map { it.ruleCode },
        )
        assertEquals(listOf(ElectricalRuleEvaluationStatus.PASSED, ElectricalRuleEvaluationStatus.PASSED), results.map { it.status })
    }

    @Test
    fun `respects disabled rules`() = runTest {
        val configs = defaultConfigs().map {
            if (it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE) it.copy(enabled = false) else it
        }
        val useCase = EvaluateSupplyVoltageUseCase(FakeElectricalRuleConfigRepository(configs))

        val results = useCase(input(220.0))

        assertEquals(ElectricalRuleEvaluationStatus.DISABLED, results.first { it.ruleCode == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }.status)
        assertEquals(ElectricalRuleEvaluationStatus.PASSED, results.first { it.ruleCode == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }.status)
    }

    @Test
    fun `works if one config does not exist`() = runTest {
        val configs = defaultConfigs().filterNot { it.code == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }
        val useCase = EvaluateSupplyVoltageUseCase(FakeElectricalRuleConfigRepository(configs))

        val results = useCase(input(220.0))

        assertEquals(ElectricalRuleEvaluationStatus.PASSED, results.first { it.ruleCode == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE }.status)
        assertEquals(ElectricalRuleEvaluationStatus.NOT_EVALUATED, results.first { it.ruleCode == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE }.status)
    }

    @Test
    fun `does not save or mutate repository`() = runTest {
        val repository = FakeElectricalRuleConfigRepository(defaultConfigs())
        val useCase = EvaluateSupplyVoltageUseCase(repository)

        useCase(input(254.0))

        assertEquals(0, repository.saveCount)
        assertEquals(0, repository.restoreDefaultsCount)
        assertEquals(defaultConfigs(), repository.configs.value)
    }

    private fun input(voltage: Double): SupplyVoltageInput = SupplyVoltageInput(
        voltage = voltage,
        location = "pilar",
        origin = CalculationSource.MEASURED,
        sourceCalculationId = null,
        inspectionId = "inspection-id",
    )

    private fun defaultConfigs(): List<ElectricalRuleConfig> {
        return DefaultElectricalRuleConfigs.all.filter {
            it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE || it.code == ElectricalRuleCode.MAX_SUPPLY_VOLTAGE
        }
    }
}

private class FakeElectricalRuleConfigRepository(
    initialConfigs: List<ElectricalRuleConfig>,
) : ElectricalRuleConfigRepository {
    val configs = MutableStateFlow(initialConfigs)
    val getCalls = mutableListOf<ElectricalRuleCode>()
    var saveCount = 0
    var restoreDefaultsCount = 0

    override fun observeAll(): Flow<List<ElectricalRuleConfig>> = configs

    override fun observeByCode(code: ElectricalRuleCode): Flow<ElectricalRuleConfig?> {
        return configs.map { values -> values.firstOrNull { it.code == code } }
    }

    override suspend fun getByCode(code: ElectricalRuleCode): ElectricalRuleConfig? {
        getCalls += code
        return configs.value.firstOrNull { it.code == code }
    }

    override suspend fun save(config: ElectricalRuleConfig) {
        saveCount++
        configs.value = configs.value.filterNot { it.code == config.code } + config
    }

    override suspend fun saveAll(configs: List<ElectricalRuleConfig>) {
        saveCount++
        this.configs.value = configs
    }

    override suspend fun restoreDefaults() {
        restoreDefaultsCount++
        configs.value = DefaultElectricalRuleConfigs.all
    }
}
