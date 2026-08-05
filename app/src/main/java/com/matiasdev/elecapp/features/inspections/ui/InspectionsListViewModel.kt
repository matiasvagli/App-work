package com.matiasdev.elecapp.features.inspections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionListItem
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InspectionListFilter(val label: String, val status: InspectionStatus?) {
    DRAFT("En borrador", InspectionStatus.DRAFT),
    COMPLETED("Finalizados", InspectionStatus.COMPLETED),
    ALL("Todos", null),
}

data class InspectionsListUiState(
    val isLoading: Boolean = true,
    val filter: InspectionListFilter = InspectionListFilter.DRAFT,
    val query: String = "",
    val inspections: List<InspectionListItem> = emptyList(),
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionsListViewModel(
    private val repository: InspectionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionsListUiState())
    val uiState: StateFlow<InspectionsListUiState> = _uiState.asStateFlow()
    private val filter = MutableStateFlow(InspectionListFilter.DRAFT)
    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch(ioDispatcher) {
            combine(filter, query) { currentFilter, currentQuery -> currentFilter to currentQuery }
                .flatMapLatest { (currentFilter, currentQuery) ->
                    repository.observeInspectionList(currentFilter.status, currentQuery)
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "No se pudieron cargar los relevamientos")
                    }
                }
                .collect { inspections ->
                    _uiState.update {
                        it.copy(isLoading = false, inspections = inspections, filter = filter.value, query = query.value, errorMessage = null)
                    }
                }
        }
    }

    fun onFilterChange(value: InspectionListFilter) {
        filter.value = value
        _uiState.update { it.copy(filter = value, isLoading = true) }
    }

    fun onQueryChange(value: String) {
        query.value = value
        _uiState.update { it.copy(query = value, isLoading = true) }
    }
}

class InspectionsListViewModelFactory(
    private val repository: InspectionRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InspectionsListViewModel(repository) as T
    }
}
