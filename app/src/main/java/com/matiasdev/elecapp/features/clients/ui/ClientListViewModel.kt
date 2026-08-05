package com.matiasdev.elecapp.features.clients.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientListViewModel(
    private val repository: ClientRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientListUiState())
    val uiState: StateFlow<ClientListUiState> = _uiState.asStateFlow()

    private var allClients: List<Client> = emptyList()

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.observeActiveClients()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudieron cargar los clientes",
                        )
                    }
                }
                .collect { clients ->
                    allClients = clients
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            clients = clients.filterBy(it.searchQuery),
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                clients = allClients.filterBy(query),
            )
        }
    }

    fun askDelete(client: Client) {
        _uiState.update { it.copy(clientPendingDelete = client) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(clientPendingDelete = null) }
    }

    fun confirmDelete() {
        val client = _uiState.value.clientPendingDelete ?: return
        viewModelScope.launch(ioDispatcher) {
            runCatching { repository.softDelete(client.id) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo eliminar el cliente")
                    }
                }
            dismissDelete()
        }
    }

    private fun List<Client>.filterBy(query: String): List<Client> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return this
        return filter { client ->
            client.fullName.contains(normalizedQuery, ignoreCase = true) ||
                client.phone.contains(normalizedQuery, ignoreCase = true)
        }
    }
}

class ClientListViewModelFactory(
    private val repository: ClientRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClientListViewModel(repository) as T
    }
}
