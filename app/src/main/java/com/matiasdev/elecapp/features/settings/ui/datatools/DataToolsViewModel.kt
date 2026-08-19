package com.matiasdev.elecapp.features.settings.ui.datatools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.settings.data.AppDataReset
import com.matiasdev.elecapp.features.settings.data.DemoDataSeeder
import com.matiasdev.elecapp.features.settings.domain.FeedbackContext
import com.matiasdev.elecapp.features.settings.domain.FeedbackMessage
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

class DataToolsViewModel(
    private val seeder: DemoDataSeeder,
    private val reset: AppDataReset,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DataToolsUiState())
    val uiState: StateFlow<DataToolsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataToolsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DataToolsEvent> = _events.asSharedFlow()

    fun onCommentChange(value: String) {
        _uiState.update { it.copy(comment = value) }
    }

    fun onSendFeedback(context: FeedbackContext) {
        val state = _uiState.value
        if (!state.canSendFeedback) return
        val text = FeedbackMessage.build(
            comment = state.comment,
            context = context,
            now = timeProvider.now(),
        )
        _events.tryEmit(DataToolsEvent.ShareText(text))
    }

    fun onSeedDemoData() {
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isSeeding = true) }
        viewModelScope.launch {
            val result = runCatching { withContext(ioDispatcher) { seeder.seed() } }
            _uiState.update { it.copy(isSeeding = false) }
            val message = result.fold(
                onSuccess = { "Datos de ejemplo cargados" },
                onFailure = { "No se pudieron cargar los datos de ejemplo" },
            )
            _events.tryEmit(DataToolsEvent.Message(message))
        }
    }

    fun onWipeAll() {
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isWiping = true) }
        viewModelScope.launch {
            val result = runCatching { withContext(ioDispatcher) { reset.wipeAll() } }
            _uiState.update { it.copy(isWiping = false) }
            val message = result.fold(
                onSuccess = { "Se borraron todos los datos" },
                onFailure = { "No se pudieron borrar los datos" },
            )
            _events.tryEmit(DataToolsEvent.Message(message))
        }
    }
}

class DataToolsViewModelFactory(
    private val seeder: DemoDataSeeder,
    private val reset: AppDataReset,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataToolsViewModel(seeder, reset) as T
    }
}
