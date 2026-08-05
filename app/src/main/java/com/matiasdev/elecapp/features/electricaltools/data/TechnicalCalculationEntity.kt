package com.matiasdev.elecapp.features.electricaltools.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "technical_calculations",
    indices = [
        Index("type"),
        Index("client_id"),
        Index("visit_id"),
        Index("inspection_id"),
        Index("created_at"),
        Index("classification"),
        Index("is_deleted"),
    ],
)
data class TechnicalCalculationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val source: String,
    @ColumnInfo(name = "client_id") val clientId: String?,
    @ColumnInfo(name = "visit_id") val visitId: String?,
    @ColumnInfo(name = "inspection_id") val inspectionId: String?,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "input_data_json") val inputDataJson: String,
    @ColumnInfo(name = "result_data_json") val resultDataJson: String,
    @ColumnInfo(name = "primary_result_value") val primaryResultValue: Double?,
    @ColumnInfo(name = "primary_result_unit") val primaryResultUnit: String?,
    val classification: String,
    @ColumnInfo(name = "technician_conclusion") val technicianConclusion: String,
    @ColumnInfo(name = "technician_notes") val technicianNotes: String?,
    @ColumnInfo(name = "formula_version") val formulaVersion: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
