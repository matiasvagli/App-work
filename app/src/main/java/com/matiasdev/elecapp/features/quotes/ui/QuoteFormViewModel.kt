package com.matiasdev.elecapp.features.quotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.MoneyFormatter
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCalculator
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class QuoteFormViewModel(
    private val quoteRepository: QuoteRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteId: String?,
    initialClientId: String?,
    initialVisitId: String?,
    initialInspectionId: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        QuoteFormUiState(clientId = initialClientId, visitId = initialVisitId, inspectionId = initialInspectionId),
    )
    val uiState: StateFlow<QuoteFormUiState> = _uiState.asStateFlow()
    private var clientSearchJob: Job? = null

    init {
        viewModelScope.launch { loadInitialContext(initialClientId, initialVisitId, initialInspectionId) }
        observeClients("")
    }

    fun updateClientQuery(query: String) {
        _uiState.update { it.copy(clientQuery = query) }
        observeClients(query)
    }

    fun selectClient(clientId: String) = _uiState.update { it.copy(clientId = clientId) }
    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updateCurrency(value: QuoteCurrency) = _uiState.update { it.copy(currency = value) }
    fun updateDiscountType(value: DiscountType) = _uiState.update { it.copy(discountType = value) }
    fun updateDiscountInput(value: String) = _uiState.update { it.copy(discountInput = value) }
    fun updateValidUntil(value: String) = _uiState.update { it.copy(validUntilInput = value) }
    fun updatePaymentTerms(value: String) = _uiState.update { it.copy(paymentTerms = value) }
    fun updateGeneralNotes(value: String) = _uiState.update { it.copy(generalNotes = value) }
    fun updateClientMessage(value: String) = _uiState.update { it.copy(clientMessage = value) }

    fun addItem(type: QuoteItemType) {
        _uiState.update { state ->
            state.copy(items = state.items + newItem(type))
        }
    }

    fun updateItem(id: String, transform: (QuoteItemFormState) -> QuoteItemFormState) {
        _uiState.update { state -> state.copy(items = state.items.map { if (it.id == id) transform(it) else it }) }
    }

    fun duplicateItem(id: String) {
        _uiState.update { state ->
            val item = state.items.firstOrNull { it.id == id } ?: return@update state
            state.copy(items = state.items + item.copy(id = UUID.randomUUID().toString()))
        }
    }

    fun removeItem(id: String) = _uiState.update { state -> state.copy(items = state.items.filterNot { it.id == id }) }

    fun moveItem(id: String, offset: Int) {
        _uiState.update { state ->
            val index = state.items.indexOfFirst { it.id == id }
            if (index < 0) return@update state
            val target = (index + offset).coerceIn(state.items.indices)
            if (index == target) state else state.copy(items = state.items.toMutableList().apply {
                add(target, removeAt(index))
            })
        }
    }

    fun save(mode: QuoteSaveMode) {
        viewModelScope.launch {
            runCatching {
                val now = Instant.now()
                val state = uiState.value
                val clientId = requireNotNull(state.clientId) { "Seleccioná un cliente" }
                require(state.title.isNotBlank()) { "Ingresá un título" }
                val items = buildItems(state, now)
                if (mode == QuoteSaveMode.READY) require(items.isNotEmpty()) { "Agregá al menos un ítem válido" }
                val totals = QuoteCalculator.totals(items, state.discountType, discountValue(state))
                val id = state.quoteId ?: UUID.randomUUID().toString()
                val quote = Quote(
                    id = id,
                    clientId = clientId,
                    visitId = state.visitId,
                    inspectionId = state.inspectionId,
                    quoteNumber = state.quoteNumber.ifBlank { quoteRepository.nextQuoteNumber(now) },
                    title = state.title.trim(),
                    description = state.description.trim().ifBlank { null },
                    status = if (mode == QuoteSaveMode.READY) QuoteStatus.READY else QuoteStatus.DRAFT,
                    currency = state.currency,
                    subtotalAmount = totals.subtotalAmount,
                    discountType = state.discountType,
                    discountValue = discountValue(state),
                    totalAmount = totals.totalAmount,
                    validUntil = parseDate(state.validUntilInput),
                    paymentTerms = state.paymentTerms.trim().ifBlank { null },
                    generalNotes = state.generalNotes.trim().ifBlank { null },
                    clientMessage = state.clientMessage.trim().ifBlank { null },
                    sentAt = null,
                    approvedAt = null,
                    rejectedAt = null,
                    createdAt = now,
                    updatedAt = now,
                    isDeleted = false,
                )
                quoteRepository.saveQuoteWithItems(quote, items.map { it.copy(quoteId = id) })
                id
            }.onSuccess { id ->
                _uiState.update { it.copy(isSaving = false, savedQuoteId = id, errorMessage = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "No se pudo guardar") }
            }
        }
    }

    fun clearSavedEvent() {
        _uiState.update { it.copy(savedQuoteId = null) }
    }

    private suspend fun loadInitialContext(clientId: String?, visitId: String?, inspectionId: String?) {
        if (quoteId != null) {
            loadQuote(quoteId)
            return
        }
        val resolvedVisitId = visitId ?: inspectionId?.let {
            inspectionRepository.findAggregate(it)?.inspection?.visitId
        }
        val resolvedClientId = clientId ?: resolvedVisitId?.let {
            visitRepository.findActiveById(it)?.clientId
        }
        _uiState.update { it.copy(clientId = resolvedClientId, visitId = resolvedVisitId) }
    }

    private fun observeClients(query: String) {
        clientSearchJob?.cancel()
        clientSearchJob = viewModelScope.launch {
            clientRepository.observeActiveClientsMatching(query).collect { clients ->
                _uiState.update { it.copy(clients = clients) }
            }
        }
    }

    private suspend fun loadQuote(id: String) {
        val quote = quoteRepository.findById(id) ?: return
        val items = quoteRepository.findItems(id)
        _uiState.update {
            it.copy(
                quoteId = quote.id,
                clientId = quote.clientId,
                visitId = quote.visitId,
                inspectionId = quote.inspectionId,
                quoteNumber = quote.quoteNumber,
                title = quote.title,
                description = quote.description.orEmpty(),
                currency = quote.currency,
                items = items.map { item -> item.toForm() },
                discountType = quote.discountType,
                discountInput = discountInput(quote.discountType, quote.discountValue),
                validUntilInput = quote.validUntil?.atZone(ZoneId.systemDefault())?.toLocalDate()?.toString().orEmpty(),
                paymentTerms = quote.paymentTerms.orEmpty(),
                generalNotes = quote.generalNotes.orEmpty(),
                clientMessage = quote.clientMessage.orEmpty(),
            )
        }
    }

    private fun buildItems(state: QuoteFormUiState, now: Instant): List<QuoteItem> {
        return state.items.mapIndexedNotNull { index, form ->
            if (form.description.isBlank()) return@mapIndexedNotNull null
            val quantity = form.quantity.replace(",", ".").toDoubleOrNull() ?: 0.0
            val price = MoneyFormatter.parseMajorAmount(form.unitPriceInput)
            QuoteItem(
                id = form.id,
                quoteId = state.quoteId.orEmpty(),
                type = form.type,
                description = form.description.trim(),
                quantity = quantity,
                unit = form.unit,
                customUnitLabel = form.customUnitLabel.trim().ifBlank { null },
                unitPriceAmount = price,
                lineTotalAmount = QuoteCalculator.lineTotal(quantity, price),
                sortOrder = index,
                notes = form.notes.trim().ifBlank { null },
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            )
        }
    }

    private fun discountValue(state: QuoteFormUiState): Long {
        return when (state.discountType) {
            DiscountType.NONE -> 0L
            DiscountType.FIXED -> MoneyFormatter.parseMajorAmount(state.discountInput)
            DiscountType.PERCENTAGE -> ((state.discountInput.replace(",", ".").toDoubleOrNull() ?: 0.0) * 100).toLong()
        }
    }

    private fun parseDate(input: String): Instant? {
        return input.takeIf { it.isNotBlank() }?.let {
            LocalDate.parse(it).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }
    }

    private fun newItem(type: QuoteItemType): QuoteItemFormState = QuoteItemFormState(
        id = UUID.randomUUID().toString(),
        type = type,
        description = "",
        quantity = "1",
        unit = QuoteUnit.FIXED,
        customUnitLabel = "",
        unitPriceInput = "",
        notes = "",
    )
}

private fun QuoteItem.toForm(): QuoteItemFormState = QuoteItemFormState(
    id = id,
    type = type,
    description = description,
    quantity = quantity.toString(),
    unit = unit,
    customUnitLabel = customUnitLabel.orEmpty(),
    unitPriceInput = (unitPriceAmount / 100).toString(),
    notes = notes.orEmpty(),
)

private fun discountInput(type: DiscountType, value: Long): String = when (type) {
    DiscountType.NONE -> ""
    DiscountType.FIXED -> (value / 100).toString()
    DiscountType.PERCENTAGE -> (value / 100.0).toString()
}

class QuoteFormViewModelFactory(
    private val quoteRepository: QuoteRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteId: String?,
    private val clientId: String?,
    private val visitId: String?,
    private val inspectionId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuoteFormViewModel(
            quoteRepository,
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteId,
            clientId,
            visitId,
            inspectionId,
        ) as T
    }
}
