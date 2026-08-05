package com.matiasdev.elecapp.features.materials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.domain.MaterialListItem
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class MaterialListsUiState(
    val selectedStatus: MaterialListStatus? = MaterialListStatus.DRAFT,
    val query: String = "",
    val lists: List<MaterialListItem> = emptyList(),
)

class MaterialListsViewModel(
    private val repository: MaterialRepository,
) : ViewModel() {
    private val filters = MutableStateFlow(MaterialListsUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MaterialListsUiState> = filters.flatMapLatest { filter ->
        repository.observeList(filter.selectedStatus, filter.query).map { lists ->
            filter.copy(lists = lists)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MaterialListsUiState())

    fun selectStatus(status: MaterialListStatus?) {
        filters.update { it.copy(selectedStatus = status) }
    }

    fun updateQuery(query: String) {
        filters.update { it.copy(query = query) }
    }
}

class MaterialListsViewModelFactory(
    private val repository: MaterialRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MaterialListsViewModel(repository) as T
}
