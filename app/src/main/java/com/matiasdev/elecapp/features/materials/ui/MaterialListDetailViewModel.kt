package com.matiasdev.elecapp.features.materials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatusActions
import com.matiasdev.elecapp.features.materials.summary.MaterialListSummaryGenerator
import com.matiasdev.elecapp.features.materials.summary.MaterialSummaryContext
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

data class MaterialListDetailUiState(
    val list: MaterialList? = null,
    val items: List<MaterialItem> = emptyList(),
    val client: Client? = null,
    val quote: Quote? = null,
    val includePrices: Boolean = false,
    val shareText: String = "",
)

class MaterialListDetailViewModel(
    private val repository: MaterialRepository,
    private val clientRepository: ClientRepository,
    private val quoteRepository: QuoteRepository,
    private val listId: String,
) : ViewModel() {
    private val includePrices = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val _events = MutableSharedFlow<MaterialListDetailEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<MaterialListDetailUiState> = combine(
        repository.observeById(listId),
        repository.observeItems(listId),
        includePrices,
    ) { list, items, prices ->
        val client = list?.let { clientRepository.findById(it.clientId) }
        val quote = list?.quoteId?.let { quoteRepository.findById(it) }
        val text = if (list != null && client != null) {
            MaterialListSummaryGenerator.generate(
                materialList = list,
                items = items,
                context = MaterialSummaryContext(client.fullName, quote?.quoteNumber),
                includePrices = prices,
                currency = quote?.currency ?: QuoteCurrency.ARS,
            )
        } else {
            ""
        }
        MaterialListDetailUiState(list, items, client, quote, prices, text)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MaterialListDetailUiState())

    fun setIncludePrices(value: Boolean) {
        includePrices.value = value
    }

    fun applyPrimaryTransition() {
        val status = uiState.value.list?.status ?: return
        val next = MaterialListStatusActions.primaryTransition(status) ?: return
        changeStatus(next)
    }

    fun cancelList() {
        val status = uiState.value.list?.status ?: return
        if (!MaterialListStatusActions.canCancel(status)) return
        changeStatus(MaterialListStatus.CANCELLED)
    }

    private fun changeStatus(status: MaterialListStatus) {
        viewModelScope.launch {
            repository.updateStatus(listId, status, Instant.now())
            _events.emit(
                if (status == MaterialListStatus.CANCELLED) {
                    MaterialListDetailEvent.Cancelled
                } else {
                    MaterialListDetailEvent.Message(status.confirmationMessage())
                },
            )
        }
    }
}

sealed interface MaterialListDetailEvent {
    data class Message(val text: String) : MaterialListDetailEvent
    data object Cancelled : MaterialListDetailEvent
}

fun MaterialListStatus.primaryActionLabel(): String? = when (this) {
    MaterialListStatus.DRAFT -> "Marcar lista preparada"
    MaterialListStatus.READY -> "Marcar entregada"
    MaterialListStatus.DELIVERED -> "Marcar comprada"
    MaterialListStatus.PURCHASED,
    MaterialListStatus.CANCELLED,
    -> null
}

private fun MaterialListStatus.confirmationMessage(): String = when (this) {
    MaterialListStatus.READY -> "Lista preparada"
    MaterialListStatus.DELIVERED -> "Lista entregada"
    MaterialListStatus.PURCHASED -> "Lista comprada"
    MaterialListStatus.CANCELLED -> "Lista cancelada"
    MaterialListStatus.DRAFT -> "Lista actualizada"
}

class MaterialListDetailViewModelFactory(
    private val repository: MaterialRepository,
    private val clientRepository: ClientRepository,
    private val quoteRepository: QuoteRepository,
    private val listId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MaterialListDetailViewModel(repository, clientRepository, quoteRepository, listId) as T
    }
}
