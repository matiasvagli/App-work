package com.matiasdev.elecapp.features.electricalrules.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "electrical_rule_configs")
data class ElectricalRuleConfigEntity(
    @PrimaryKey val code: String,
    val name: String,
    val enabled: Boolean,
    val severity: String,
    @ColumnInfo(name = "numeric_value") val numericValue: Double?,
    @ColumnInfo(name = "secondary_numeric_value") val secondaryNumericValue: Double?,
    val unit: String?,
    @ColumnInfo(name = "finding_title") val findingTitle: String,
    @ColumnInfo(name = "finding_description_template") val findingDescriptionTemplate: String,
    @ColumnInfo(name = "recommendation_template") val recommendationTemplate: String?,
    @ColumnInfo(name = "config_version") val configVersion: Int,
)
