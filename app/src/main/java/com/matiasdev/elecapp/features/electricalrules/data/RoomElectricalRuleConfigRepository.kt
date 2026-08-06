package com.matiasdev.elecapp.features.electricalrules.data

import androidx.sqlite.db.SupportSQLiteDatabase
import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomElectricalRuleConfigRepository(
    private val dao: ElectricalRuleConfigDao,
) : ElectricalRuleConfigRepository {
    override fun observeAll(): Flow<List<ElectricalRuleConfig>> = dao.observeAll().map { rows ->
        rows.map(ElectricalRuleConfigEntity::toDomain)
    }

    override fun observeByCode(code: ElectricalRuleCode): Flow<ElectricalRuleConfig?> {
        return dao.observeByCode(code.name).map { it?.toDomain() }
    }

    override suspend fun getByCode(code: ElectricalRuleCode): ElectricalRuleConfig? {
        return dao.getByCode(code.name)?.toDomain()
    }

    override suspend fun save(config: ElectricalRuleConfig) {
        dao.upsert(config.toEntity())
    }

    override suspend fun saveAll(configs: List<ElectricalRuleConfig>) {
        dao.upsertAll(configs.map(ElectricalRuleConfig::toEntity))
    }

    override suspend fun restoreDefaults() {
        dao.upsertAll(DefaultElectricalRuleConfigs.all.map(ElectricalRuleConfig::toEntity))
    }
}

fun SupportSQLiteDatabase.createElectricalRuleConfigsTable() {
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS electrical_rule_configs (
            code TEXT NOT NULL,
            name TEXT NOT NULL,
            enabled INTEGER NOT NULL,
            severity TEXT NOT NULL,
            numeric_value REAL,
            secondary_numeric_value REAL,
            unit TEXT,
            finding_title TEXT NOT NULL,
            finding_description_template TEXT NOT NULL,
            recommendation_template TEXT,
            config_version INTEGER NOT NULL,
            PRIMARY KEY(code)
        )
        """.trimIndent(),
    )
}

fun SupportSQLiteDatabase.insertDefaultElectricalRuleConfigsIgnoringExisting() {
    DefaultElectricalRuleConfigs.all.map(ElectricalRuleConfig::toEntity).forEach { entity ->
        execSQL(
            """
            INSERT OR IGNORE INTO electrical_rule_configs (
                code,
                name,
                enabled,
                severity,
                numeric_value,
                secondary_numeric_value,
                unit,
                finding_title,
                finding_description_template,
                recommendation_template,
                config_version
            ) VALUES (
                ${entity.code.sql()},
                ${entity.name.sql()},
                ${entity.enabled.sql()},
                ${entity.severity.sql()},
                ${entity.numericValue.sql()},
                ${entity.secondaryNumericValue.sql()},
                ${entity.unit.sql()},
                ${entity.findingTitle.sql()},
                ${entity.findingDescriptionTemplate.sql()},
                ${entity.recommendationTemplate.sql()},
                ${entity.configVersion}
            )
            """.trimIndent(),
        )
    }
}

private fun Boolean.sql(): String = if (this) "1" else "0"

private fun Double?.sql(): String = this?.toString() ?: "NULL"

private fun String?.sql(): String = this?.let { "'${it.replace("'", "''")}'" } ?: "NULL"
