package com.matiasdev.elecapp.features.electricalrules.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectricalRuleConfigDao {
    @Query("SELECT * FROM electrical_rule_configs ORDER BY code ASC")
    fun observeAll(): Flow<List<ElectricalRuleConfigEntity>>

    @Query("SELECT * FROM electrical_rule_configs WHERE code = :code LIMIT 1")
    fun observeByCode(code: String): Flow<ElectricalRuleConfigEntity?>

    @Query("SELECT * FROM electrical_rule_configs WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): ElectricalRuleConfigEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringExisting(entities: List<ElectricalRuleConfigEntity>)

    @Upsert
    suspend fun upsert(entity: ElectricalRuleConfigEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ElectricalRuleConfigEntity>)

    @Query("UPDATE electrical_rule_configs SET enabled = :enabled WHERE code = :code")
    suspend fun updateEnabled(code: String, enabled: Boolean)

    @Query("DELETE FROM electrical_rule_configs")
    suspend fun deleteAll()
}
