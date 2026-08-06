package com.matiasdev.elecapp.features.electricalrules.data

import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleSeverity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomElectricalRuleConfigRepositoryTest {
    @Test
    fun `restoreDefaults restores initial values`() = runTest {
        val dao = FakeElectricalRuleConfigDao()
        val repository = RoomElectricalRuleConfigRepository(dao)
        val modified = DefaultElectricalRuleConfigs.all.first {
            it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE
        }.copy(
            enabled = false,
            severity = ElectricalRuleSeverity.RECOMMENDED,
            numericValue = 180.0,
            findingTitle = "Título modificado",
        )
        repository.save(modified)

        repository.restoreDefaults()

        assertEquals(
            DefaultElectricalRuleConfigs.all.first { it.code == ElectricalRuleCode.MIN_SUPPLY_VOLTAGE },
            repository.getByCode(ElectricalRuleCode.MIN_SUPPLY_VOLTAGE),
        )
        assertEquals(DefaultElectricalRuleConfigs.all.size, dao.rows.value.size)
    }
}

private class FakeElectricalRuleConfigDao : ElectricalRuleConfigDao {
    val rows = MutableStateFlow<List<ElectricalRuleConfigEntity>>(emptyList())

    override fun observeAll(): Flow<List<ElectricalRuleConfigEntity>> = rows

    override fun observeByCode(code: String): Flow<ElectricalRuleConfigEntity?> {
        return rows.map { values -> values.firstOrNull { it.code == code } }
    }

    override suspend fun getByCode(code: String): ElectricalRuleConfigEntity? {
        return rows.value.firstOrNull { it.code == code }
    }

    override suspend fun insertAllIgnoringExisting(entities: List<ElectricalRuleConfigEntity>) {
        val existingCodes = rows.value.map { it.code }.toSet()
        rows.value = rows.value + entities.filterNot { it.code in existingCodes }
    }

    override suspend fun upsert(entity: ElectricalRuleConfigEntity) {
        rows.value = rows.value.filterNot { it.code == entity.code } + entity
    }

    override suspend fun upsertAll(entities: List<ElectricalRuleConfigEntity>) {
        entities.forEach { upsert(it) }
    }

    override suspend fun updateEnabled(code: String, enabled: Boolean) {
        rows.value = rows.value.map { if (it.code == code) it.copy(enabled = enabled) else it }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }
}
