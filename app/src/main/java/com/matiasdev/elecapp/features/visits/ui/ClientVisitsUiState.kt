package com.matiasdev.elecapp.features.visits.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.domain.Visit

data class ClientVisitsUiState(
    val isLoading: Boolean = true,
    val client: Client? = null,
    val visits: List<Visit> = emptyList(),
    val errorMessage: String? = null,
)
