package com.matiasdev.elecapp.features.clients.domain

data class ClientValidationResult(
    val fullNameError: String? = null,
    val phoneError: String? = null,
) {
    val isValid: Boolean = fullNameError == null && phoneError == null
}

object ClientValidator {
    fun validate(fullName: String, phone: String): ClientValidationResult {
        val trimmedName = fullName.trim()
        val trimmedPhone = phone.trim()

        return ClientValidationResult(
            fullNameError = when {
                trimmedName.isBlank() -> "El nombre es obligatorio"
                trimmedName.length < 2 -> "Ingresá un nombre más claro"
                else -> null
            },
            phoneError = when {
                trimmedPhone.isBlank() -> "El teléfono es obligatorio"
                trimmedPhone.length < 6 -> "Ingresá un teléfono válido"
                else -> null
            },
        )
    }
}
