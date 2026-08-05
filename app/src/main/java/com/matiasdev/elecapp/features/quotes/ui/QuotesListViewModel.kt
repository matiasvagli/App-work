package com.matiasdev.elecapp.features.quotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.QuoteListItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class QuotesListUiState(
    val selectedStatus: QuoteStatus? = QuoteStatus.DRAFT,
    val query: String = "",
    val orderByDue: Boolean = false,
    val quotes: List<QuoteListItem> = emptyList(),
)

class QuotesListViewModel(
    private val repository: QuoteRepository,
) : ViewModel() {
    private val filters = MutableStateFlow(QuotesListUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<QuotesListUiState> = filters.flatMapLatest { filter ->
        repository.observeList(filter.selectedStatus, filter.query, filter.orderByDue).map { quotes ->
            filter.copy(quotes = quotes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuotesListUiState())

    fun selectStatus(status: QuoteStatus?) {
        filters.update { it.copy(selectedStatus = status) }
    }

    fun updateQuery(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun toggleOrderByDue() {
        filters.update { it.copy(orderByDue = !it.orderByDue) }
    }
}

class QuotesListViewModelFactory(
    private val repository: QuoteRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = QuotesListViewModel(repository) as T
}
