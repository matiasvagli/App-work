package com.matiasdev.elecapp.features.referencedocs.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentRepository
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentStorage
import com.matiasdev.elecapp.features.referencedocs.domain.ReferenceDocument
import com.matiasdev.elecapp.features.referencedocs.domain.ReferenceDocumentLabels
import com.matiasdev.elecapp.features.referencedocs.domain.ReferenceDocumentNaming
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ReferenceDocsViewModel(
    private val repository: ReferenceDocumentRepository,
    private val storage: ReferenceDocumentStorage,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReferenceDocsUiState())
    val uiState: StateFlow<ReferenceDocsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReferenceDocsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ReferenceDocsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { documents ->
                val now = timeProvider.now()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        documents = documents.map { it.toRow(now) },
                    )
                }
            }
        }
    }

    fun onSourceClick(url: String) {
        _events.tryEmit(ReferenceDocsEvent.OpenUrl(url))
    }

    fun onImport(uri: Uri, sourceUrl: String?) {
        if (_uiState.value.isImporting) return
        _uiState.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            val result = runCatching { copyDocument(uri, sourceUrl) }
            _uiState.update { it.copy(isImporting = false) }
            result
                .onSuccess { _events.tryEmit(ReferenceDocsEvent.Message("Documento importado")) }
                .onFailure { _events.tryEmit(ReferenceDocsEvent.Message(it.importErrorMessage())) }
        }
    }

    fun onOpen(documentId: String) {
        viewModelScope.launch {
            val document = repository.findById(documentId)
            if (document == null) {
                _events.tryEmit(ReferenceDocsEvent.Message("No se encontró el documento"))
                return@launch
            }
            val exists = withContext(ioDispatcher) { storage.fileFor(document).isFile }
            if (!exists) {
                _events.tryEmit(ReferenceDocsEvent.Message("El archivo ya no está guardado. Importalo de nuevo."))
                return@launch
            }
            _events.tryEmit(ReferenceDocsEvent.OpenPdf(storage.contentUriFor(document)))
        }
    }

    fun onDelete(documentId: String) {
        viewModelScope.launch {
            repository.softDelete(documentId)
            withContext(ioDispatcher) { storage.delete(documentId) }
            _events.tryEmit(ReferenceDocsEvent.Message("Documento eliminado"))
        }
    }

    private suspend fun copyDocument(uri: Uri, sourceUrl: String?): ReferenceDocument {
        val document = withContext(ioDispatcher) {
            val id = UUID.randomUUID().toString()
            val displayName = storage.displayName(uri)
            val fileName = ReferenceDocumentNaming.fileName(displayName)
            val sizeBytes = storage.copyIn(uri = uri, documentId = id, fileName = fileName)
            if (!storage.looksLikePdf(storage.fileFor(id, fileName))) {
                storage.delete(id)
                error("El archivo elegido no es un PDF")
            }
            ReferenceDocument(
                id = id,
                title = ReferenceDocumentNaming.title(displayName),
                fileName = fileName,
                sourceUrl = sourceUrl,
                sizeBytes = sizeBytes,
                importedAt = timeProvider.now(),
            )
        }
        repository.save(document)
        return document
    }

    private fun ReferenceDocument.toRow(now: java.time.Instant) = ReferenceDocumentRow(
        id = id,
        title = title,
        sizeLabel = ReferenceDocumentLabels.size(sizeBytes),
        ageLabel = ReferenceDocumentLabels.age(importedAt, now),
        isStale = ReferenceDocumentLabels.isStale(importedAt, now),
        sourceUrl = sourceUrl,
    )

    private fun Throwable.importErrorMessage(): String {
        return message?.takeIf(String::isNotBlank) ?: "No se pudo importar el documento"
    }
}

class ReferenceDocsViewModelFactory(
    private val repository: ReferenceDocumentRepository,
    private val storage: ReferenceDocumentStorage,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReferenceDocsViewModel(repository, storage) as T
    }
}
