package com.matiasdev.elecapp.features.quotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.summary.QuoteSummaryContext
import com.matiasdev.elecapp.features.quotes.summary.QuoteSummaryGenerator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

data class QuoteDetailUiState(
    val quote: Quote? = null,
    val items: List<QuoteItem> = emptyList(),
    val client: Client? = null,
    val materialList: MaterialList? = null,
    val shareText: String = "",
)

class QuoteDetailViewModel(
    private val quoteRepository: QuoteRepository,
    private val clientRepository: ClientRepository,
    materialRepository: MaterialRepository,
    private val quoteId: String,
) : ViewModel() {
    val uiState: StateFlow<QuoteDetailUiState> = combine(
        quoteRepository.observeById(quoteId),
        quoteRepository.observeItems(quoteId),
        materialRepository.observeLatestForQuote(quoteId),
    ) { quote, items, material ->
        val client = quote?.let { clientRepository.findById(it.clientId) }
        val text = if (quote != null && client != null) {
            QuoteSummaryGenerator.generate(
                quote,
                items,
                QuoteSummaryContext(
                    clientName = client.fullName,
                    address = client.address,
                    locality = client.locality,
                    linkedMaterialList = material != null,
                    purchaseResponsibility = material?.purchaseResponsibility,
                ),
            )
        } else {
            ""
        }
        QuoteDetailUiState(quote, items, client, material, text)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuoteDetailUiState())

    fun changeStatus(status: QuoteStatus) {
        viewModelScope.launch { quoteRepository.updateStatus(quoteId, status, Instant.now()) }
    }
}

class QuoteDetailViewModelFactory(
    private val quoteRepository: QuoteRepository,
    private val clientRepository: ClientRepository,
    private val materialRepository: MaterialRepository,
    private val quoteId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuoteDetailViewModel(quoteRepository, clientRepository, materialRepository, quoteId) as T
    }
}
