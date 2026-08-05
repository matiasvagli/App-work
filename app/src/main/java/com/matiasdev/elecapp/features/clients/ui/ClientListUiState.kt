package com.matiasdev.elecapp.features.clients.ui

import com.matiasdev.elecapp.features.clients.domain.Client

data class ClientListUiState(
    val isLoading: Boolean = true,
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val clientPendingDelete: Client? = null,
)
