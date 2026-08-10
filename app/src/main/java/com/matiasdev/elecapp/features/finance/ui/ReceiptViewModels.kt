package com.matiasdev.elecapp.features.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.DateRangeCalculator
import com.matiasdev.elecapp.features.finance.domain.FinanceMetrics
import com.matiasdev.elecapp.features.finance.domain.FinancePeriodPreset
import com.matiasdev.elecapp.features.finance.domain.MoneyParser
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentBalanceCalculator
import com.matiasdev.elecapp.features.finance.domain.PaymentDraft
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.displayNumber
import java.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReceiptDetailUiState(
    val receipt: ServiceReceipt? = null,
    val client: Client? = null,
    val items: List<ServiceReceiptItem> = emptyList(),
    val payments: List<Payment> = emptyList(),
)

data class ReceiptListUiState(val receipts: List<ServiceReceipt> = emptyList())

data class RegisterPaymentUiState(
    val receipt: ServiceReceipt? = null,
    val payments: List<Payment> = emptyList(),
    val amount: String = "",
    val method: PaymentMethod = PaymentMethod.CASH,
    val reference: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

data class FinanceDashboardUiState(
    val preset: FinancePeriodPreset = FinancePeriodPreset.TODAY,
    val metrics: FinanceMetrics = FinanceMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
)

sealed interface ReceiptDetailEvent {
    data class Share(val text: String) : ReceiptDetailEvent
}

sealed interface RegisterPaymentEvent {
    data object Saved : RegisterPaymentEvent
}

class ReceiptDetailViewModel(
    private val financeRepository: FinanceRepository,
    private val clientRepository: ClientRepository,
    private val receiptId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<ReceiptDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                financeRepository.observeReceiptById(receiptId),
                financeRepository.observeItems(receiptId),
                financeRepository.observePayments(receiptId),
            ) { receipt, items, payments -> Triple(receipt, items, payments) }.collect { (receipt, items, payments) ->
                val client = receipt?.let { clientRepository.findById(it.clientId) }
                _uiState.update { it.copy(receipt = receipt, client = client, items = items, payments = payments) }
            }
        }
    }

    fun share() {
        val state = _uiState.value
        val receipt = state.receipt ?: return
        viewModelScope.launch { _events.emit(ReceiptDetailEvent.Share(ReceiptShareText.build(receipt, state.client, state.items, state.payments))) }
    }
}

class ReceiptListViewModel(financeRepository: FinanceRepository, clientId: String?) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceiptListUiState())
    val uiState: StateFlow<ReceiptListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val flow = clientId?.let(financeRepository::observeReceiptsByClient) ?: financeRepository.observeReceipts()
            flow.collect { receipts -> _uiState.update { it.copy(receipts = receipts) } }
        }
    }
}

class RegisterPaymentViewModel(
    private val financeRepository: FinanceRepository,
    private val receiptId: String?,
    private val clientId: String,
    private val visitId: String?,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterPaymentUiState())
    val uiState: StateFlow<RegisterPaymentUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<RegisterPaymentEvent>()
    val events = _events.asSharedFlow()

    init {
        if (receiptId != null) {
            viewModelScope.launch(ioDispatcher) {
                financeRepository.observeReceiptById(receiptId).collect { receipt ->
                    _uiState.update { it.copy(receipt = receipt) }
                }
            }
            viewModelScope.launch(ioDispatcher) {
                financeRepository.observePayments(receiptId).collect { payments ->
                    _uiState.update { it.copy(payments = payments) }
                }
            }
        }
    }

    fun update(transform: (RegisterPaymentUiState) -> RegisterPaymentUiState) {
        _uiState.update { transform(it).copy(errorMessage = null) }
    }

    fun save() {
        val cents = MoneyParser.parseCents(_uiState.value.amount)
        if (cents == null || cents <= 0L) {
            _uiState.update { it.copy(errorMessage = "Ingresá un importe válido") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                withContext(ioDispatcher) {
                    financeRepository.registerPayment(receiptId, clientId, visitId, PaymentDraft(cents, _uiState.value.method, timeProvider.now(), _uiState.value.reference, _uiState.value.notes))
                }
            }.onSuccess { _events.emit(RegisterPaymentEvent.Saved) }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo registrar el pago") } }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

class FinanceDashboardViewModel(private val financeRepository: FinanceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FinanceDashboardUiState())
    val uiState: StateFlow<FinanceDashboardUiState> = _uiState.asStateFlow()

    init {
        load(FinancePeriodPreset.TODAY)
    }

    fun load(preset: FinancePeriodPreset) {
        viewModelScope.launch(Dispatchers.IO) {
            val range = DateRangeCalculator.range(preset, Clock.systemDefaultZone())
            val metrics = financeRepository.financeMetrics(range.start, range.endExclusive)
            _uiState.update { it.copy(preset = preset, metrics = metrics) }
        }
    }
}

class ReceiptDetailViewModelFactory(
    private val financeRepository: FinanceRepository,
    private val clientRepository: ClientRepository,
    private val receiptId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ReceiptDetailViewModel(financeRepository, clientRepository, receiptId) as T
}

class ReceiptListViewModelFactory(private val financeRepository: FinanceRepository, private val clientId: String?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ReceiptListViewModel(financeRepository, clientId) as T
}

class RegisterPaymentViewModelFactory(private val financeRepository: FinanceRepository, private val receiptId: String?, private val clientId: String, private val visitId: String?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RegisterPaymentViewModel(financeRepository, receiptId, clientId, visitId) as T
}

class FinanceDashboardViewModelFactory(private val financeRepository: FinanceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FinanceDashboardViewModel(financeRepository) as T
}
