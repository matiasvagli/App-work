package com.matiasdev.elecapp.features.inspections.domain

object InspectionValidation {
    fun validatePositiveInt(value: Int?, fieldLabel: String): String? {
        return if (value != null && value <= 0) "$fieldLabel debe ser mayor a cero" else null
    }

    fun validatePositiveDouble(value: Double?, fieldLabel: String): String? {
        return if (value != null && value <= 0.0) "$fieldLabel debe ser mayor a cero" else null
    }

    fun validateRequiredText(value: String, fieldLabel: String): String? {
        return if (value.isBlank()) "$fieldLabel es obligatorio" else null
    }
}
