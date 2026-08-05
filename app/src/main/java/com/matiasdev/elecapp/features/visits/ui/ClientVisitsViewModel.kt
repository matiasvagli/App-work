package com.matiasdev.elecapp.features.visits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ClientVisitsViewModel(
    clientRepository: ClientRepository,
    visitRepository: VisitRepository,
    clientId: String,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientVisitsUiState())
    val uiState: StateFlow<ClientVisitsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            combine(
                clientRepository.observeActiveClientById(clientId),
                visitRepository.observeActiveVisitsForClient(clientId),
            ) { client, visits ->
                ClientVisitsUiState(
                    isLoading = false,
                    client = client,
                    visits = visits.sortedBy { it.scheduledAt },
                    errorMessage = if (client == null) "Cliente no encontrado" else null,
                )
            }.collect { _uiState.value = it }
        }
    }
}

class ClientVisitsViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val clientId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClientVisitsViewModel(clientRepository, visitRepository, clientId) as T
    }
}
