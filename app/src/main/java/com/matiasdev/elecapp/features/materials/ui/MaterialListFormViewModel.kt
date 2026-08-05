package com.matiasdev.elecapp.features.materials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.inspections.data.InspectionRepository
import com.matiasdev.elecapp.features.materials.data.MaterialRepository
import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.MoneyFormatter
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class MaterialListFormViewModel(
    private val repository: MaterialRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val listId: String?,
    initialClientId: String?,
    initialVisitId: String?,
    initialInspectionId: String?,
    initialQuoteId: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MaterialListFormUiState(
            clientId = initialClientId,
            visitId = initialVisitId,
            inspectionId = initialInspectionId,
            quoteId = initialQuoteId,
        ),
    )
    val uiState: StateFlow<MaterialListFormUiState> = _uiState.asStateFlow()
    private var clientSearchJob: Job? = null

    init {
        viewModelScope.launch { loadInitialContext(initialClientId, initialVisitId, initialInspectionId, initialQuoteId) }
        observeClients("")
    }

    fun updateClientQuery(query: String) {
        _uiState.update { it.copy(clientQuery = query) }
        observeClients(query)
    }

    fun selectClient(clientId: String) = _uiState.update { it.copy(clientId = clientId) }
    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateResponsibility(value: PurchaseResponsibility) = _uiState.update { it.copy(purchaseResponsibility = value) }
    fun updateIntroduction(value: String) = _uiState.update { it.copy(introduction = value) }
    fun updateNotes(value: String) = _uiState.update { it.copy(notes = value) }

    fun addItem(template: MaterialTemplate? = null) {
        _uiState.update { state -> state.copy(items = state.items + newItem(template)) }
    }

    fun updateItem(id: String, transform: (MaterialItemFormState) -> MaterialItemFormState) {
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

    fun save(mode: MaterialSaveMode) {
        viewModelScope.launch {
            runCatching {
                val now = Instant.now()
                val state = uiState.value
                val clientId = requireNotNull(state.clientId) { "Seleccioná un cliente" }
                require(state.title.isNotBlank()) { "Ingresá un título" }
                val items = buildItems(state, now)
                if (mode == MaterialSaveMode.READY) require(items.isNotEmpty()) { "Agregá al menos un material" }
                val id = state.listId ?: UUID.randomUUID().toString()
                repository.saveListWithItems(
                    MaterialList(
                        id = id,
                        clientId = clientId,
                        visitId = state.visitId,
                        inspectionId = state.inspectionId,
                        quoteId = state.quoteId,
                        title = state.title.trim(),
                        status = if (mode == MaterialSaveMode.READY) MaterialListStatus.READY else MaterialListStatus.DRAFT,
                        purchaseResponsibility = state.purchaseResponsibility,
                        introduction = state.introduction.trim().ifBlank { null },
                        notes = state.notes.trim().ifBlank { null },
                        createdAt = now,
                        updatedAt = now,
                        deliveredAt = null,
                        isDeleted = false,
                    ),
                    items.map { it.copy(materialListId = id) },
                )
                id
            }.onSuccess { id ->
                _uiState.update { it.copy(savedListId = id, errorMessage = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo guardar") }
            }
        }
    }

    fun clearSavedEvent() {
        _uiState.update { it.copy(savedListId = null) }
    }

    private suspend fun loadInitialContext(clientId: String?, visitId: String?, inspectionId: String?, quoteId: String?) {
        if (listId != null) {
            loadList(listId)
            return
        }
        val quote = quoteId?.let { quoteRepository.findById(it) }
        val resolvedInspectionId = inspectionId ?: quote?.inspectionId
        val resolvedVisitId = visitId ?: quote?.visitId ?: resolvedInspectionId?.let {
            inspectionRepository.findAggregate(it)?.inspection?.visitId
        }
        val resolvedClientId = clientId ?: quote?.clientId ?: resolvedVisitId?.let {
            visitRepository.findActiveById(it)?.clientId
        }
        _uiState.update {
            it.copy(clientId = resolvedClientId, visitId = resolvedVisitId, inspectionId = resolvedInspectionId)
        }
    }

    private suspend fun loadList(id: String) {
        val list = repository.findById(id) ?: return
        val items = repository.findItems(id)
        _uiState.update {
            it.copy(
                listId = list.id,
                clientId = list.clientId,
                visitId = list.visitId,
                inspectionId = list.inspectionId,
                quoteId = list.quoteId,
                title = list.title,
                purchaseResponsibility = list.purchaseResponsibility,
                introduction = list.introduction.orEmpty(),
                notes = list.notes.orEmpty(),
                items = items.map { item -> item.toForm() },
            )
        }
    }

    private fun buildItems(state: MaterialListFormUiState, now: Instant): List<MaterialItem> {
        return state.items.mapIndexedNotNull { index, form ->
            if (form.description.isBlank()) return@mapIndexedNotNull null
            val quantity = form.quantity.replace(",", ".").toDoubleOrNull() ?: 0.0
            require(quantity > 0.0) { "La cantidad debe ser mayor que cero" }
            MaterialItem(
                id = form.id,
                materialListId = state.listId.orEmpty(),
                description = form.description.trim(),
                quantity = quantity,
                unit = form.unit,
                customUnitLabel = form.customUnitLabel.trim().ifBlank { null },
                specifications = form.specifications.trim().ifBlank { null },
                preferredBrand = form.preferredBrand.trim().ifBlank { null },
                alternativeAllowed = form.alternativeAllowed,
                estimatedUnitPriceAmount = form.estimatedUnitPriceInput.takeIf { form.includePrices }?.let(MoneyFormatter::parseMajorAmount),
                actualUnitPriceAmount = form.actualUnitPriceInput.takeIf { form.includePrices }?.let(MoneyFormatter::parseMajorAmount),
                sortOrder = index,
                notes = form.notes.trim().ifBlank { null },
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            )
        }
    }

    private fun observeClients(query: String) {
        clientSearchJob?.cancel()
        clientSearchJob = viewModelScope.launch {
            clientRepository.observeActiveClientsMatching(query).collect { clients ->
                _uiState.update { it.copy(clients = clients) }
            }
        }
    }

    private fun newItem(template: MaterialTemplate?): MaterialItemFormState = MaterialItemFormState(
        id = UUID.randomUUID().toString(),
        description = template?.title.orEmpty(),
        quantity = "1",
        unit = template?.unit ?: MaterialUnit.UNIT,
        customUnitLabel = "",
        specifications = "",
        preferredBrand = "",
        alternativeAllowed = true,
        includePrices = false,
        estimatedUnitPriceInput = "",
        actualUnitPriceInput = "",
        notes = "",
    )
}

private fun MaterialItem.toForm(): MaterialItemFormState = MaterialItemFormState(
    id = id,
    description = description,
    quantity = quantity.toString(),
    unit = unit,
    customUnitLabel = customUnitLabel.orEmpty(),
    specifications = specifications.orEmpty(),
    preferredBrand = preferredBrand.orEmpty(),
    alternativeAllowed = alternativeAllowed,
    includePrices = estimatedUnitPriceAmount != null || actualUnitPriceAmount != null,
    estimatedUnitPriceInput = estimatedUnitPriceAmount?.let { (it / 100).toString() }.orEmpty(),
    actualUnitPriceInput = actualUnitPriceAmount?.let { (it / 100).toString() }.orEmpty(),
    notes = notes.orEmpty(),
)

class MaterialListFormViewModelFactory(
    private val repository: MaterialRepository,
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val inspectionRepository: InspectionRepository,
    private val quoteRepository: QuoteRepository,
    private val listId: String?,
    private val clientId: String?,
    private val visitId: String?,
    private val inspectionId: String?,
    private val quoteId: String?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MaterialListFormViewModel(
            repository,
            clientRepository,
            visitRepository,
            inspectionRepository,
            quoteRepository,
            listId,
            clientId,
            visitId,
            inspectionId,
            quoteId,
        ) as T
    }
}
