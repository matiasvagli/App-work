package com.matiasdev.elecapp.features.visits.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.WorkHistoryItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkHistoryUiState(
    val isLoading: Boolean = true,
    val items: List<WorkHistoryItem> = emptyList(),
    val errorMessage: String? = null,
)

class WorkHistoryViewModel(
    private val visitRepository: VisitRepository,
    private val clientId: String?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkHistoryUiState())
    val uiState: StateFlow<WorkHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val history = clientId
                ?.takeIf(String::isNotBlank)
                ?.let(visitRepository::observeCompletedWorkHistoryForClient)
                ?: visitRepository.observeCompletedWorkHistory()
            history
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo cargar el historial",
                        )
                    }
                }
                .collect { items ->
                    _uiState.update {
                        it.copy(isLoading = false, items = items, errorMessage = null)
                    }
                }
        }
    }
}

class WorkHistoryViewModelFactory(
    private val visitRepository: VisitRepository,
    private val clientId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkHistoryViewModel(visitRepository, clientId) as T
    }
}
