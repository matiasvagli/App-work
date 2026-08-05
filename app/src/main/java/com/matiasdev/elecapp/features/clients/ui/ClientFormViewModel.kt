package com.matiasdev.elecapp.features.clients.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.domain.ClientValidator
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientFormViewModel(
    private val repository: ClientRepository,
    private val clientId: String?,
    private val initialDraft: ClientFormDraft = ClientFormDraft(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ClientFormUiState(
            id = clientId,
            fullName = initialDraft.fullName,
            phone = initialDraft.phone,
            email = initialDraft.email,
            address = initialDraft.address,
            locality = initialDraft.locality,
            notes = initialDraft.notes,
            isLoading = clientId != null,
        ),
    )
    val uiState: StateFlow<ClientFormUiState> = _uiState.asStateFlow()

    private var existingClient: Client? = null

    init {
        if (clientId != null) {
            loadClient(clientId)
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, phoneError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onLocalityChange(value: String) {
        _uiState.update { it.copy(locality = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun applyImportedContact(contact: com.matiasdev.elecapp.core.external.ImportedContact) {
        _uiState.update {
            it.copy(
                fullName = contact.fullName.ifBlank { it.fullName },
                phone = contact.phone.ifBlank { it.phone },
                email = contact.email.ifBlank { it.email },
                successMessage = "Contacto importado. Revisá los datos antes de guardar.",
            )
        }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun save() {
        val currentState = _uiState.value
        val validation = ClientValidator.validate(currentState.fullName, currentState.phone)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    fullNameError = validation.fullNameError,
                    phoneError = validation.phoneError,
                )
            }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                currentState.toClient(existingClient).also { repository.save(it) }
            }.onSuccess { client ->
                _uiState.update { it.copy(isSaving = false, saved = true, savedClientId = client.id) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "No se pudo guardar el cliente",
                    )
                }
            }
        }
    }

    private fun loadClient(id: String) {
        viewModelScope.launch(ioDispatcher) {
            runCatching { repository.findById(id) }
                .onSuccess { client ->
                    existingClient = client
                    if (client == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Cliente no encontrado")
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                fullName = client.fullName,
                                phone = client.phone,
                                email = client.email.orEmpty(),
                                address = client.address.orEmpty(),
                                locality = client.locality.orEmpty(),
                                notes = client.notes.orEmpty(),
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo cargar el cliente",
                        )
                    }
                }
        }
    }

    private fun ClientFormUiState.toClient(existing: Client?): Client {
        val now = Instant.now()
        return Client(
            id = existing?.id ?: UUID.randomUUID().toString(),
            fullName = fullName.trim(),
            phone = phone.trim(),
            email = email.trim().ifBlank { null },
            address = address.trim().ifBlank { null },
            locality = locality.trim().ifBlank { null },
            notes = notes.trim().ifBlank { null },
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}

class ClientFormViewModelFactory(
    private val repository: ClientRepository,
    private val clientId: String?,
    private val initialDraft: ClientFormDraft = ClientFormDraft(),
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClientFormViewModel(repository, clientId, initialDraft) as T
    }
}
