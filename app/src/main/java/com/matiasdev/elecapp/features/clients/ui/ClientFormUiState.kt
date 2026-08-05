package com.matiasdev.elecapp.features.clients.ui

data class ClientFormUiState(
    val id: String? = null,
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val locality: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val saved: Boolean = false,
    val savedClientId: String? = null,
)

data class ClientFormDraft(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val locality: String = "",
    val notes: String = "",
)
