package com.matiasdev.elecapp.features.electricalrules.domain

import kotlinx.coroutines.flow.Flow

interface ElectricalRuleConfigRepository {
    fun observeAll(): Flow<List<ElectricalRuleConfig>>

    fun observeByCode(code: ElectricalRuleCode): Flow<ElectricalRuleConfig?>

    suspend fun getByCode(code: ElectricalRuleCode): ElectricalRuleConfig?

    suspend fun save(config: ElectricalRuleConfig)

    suspend fun saveAll(configs: List<ElectricalRuleConfig>)

    suspend fun restoreDefaults()
}
