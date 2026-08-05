package com.matiasdev.elecapp.features.clients.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.domain.Visit

data class ClientDetailUiState(
    val isLoading: Boolean = true,
    val client: Client? = null,
    val upcomingVisits: List<Visit> = emptyList(),
    val errorMessage: String? = null,
    val clientPendingDelete: Client? = null,
)
