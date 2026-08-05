package com.matiasdev.elecapp.features.clients.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientDetailViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val clientId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            combine(
                clientRepository.observeActiveClientById(clientId),
                visitRepository.observeActiveVisitsForClient(clientId),
            ) { client, visits ->
                ClientDetailUiState(
                    isLoading = false,
                    client = client,
                    upcomingVisits = visits
                        .filter { it.scheduledAt >= Instant.now() }
                        .sortedBy { it.scheduledAt }
                        .take(3),
                    errorMessage = if (client == null) "Cliente no encontrado" else null,
                )
            }.catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar el cliente",
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun askDelete(client: Client) {
        _uiState.update { it.copy(clientPendingDelete = client) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(clientPendingDelete = null) }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        val client = _uiState.value.clientPendingDelete ?: return
        viewModelScope.launch {
            runCatching { withContext(ioDispatcher) { clientRepository.softDelete(client.id) } }
                .onSuccess { onDeleted() }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo eliminar el cliente")
                    }
                }
        }
    }
}

class ClientDetailViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val clientId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClientDetailViewModel(clientRepository, visitRepository, clientId) as T
    }
}
