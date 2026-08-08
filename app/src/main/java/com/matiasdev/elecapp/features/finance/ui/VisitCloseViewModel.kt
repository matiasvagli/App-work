package com.matiasdev.elecapp.features.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.MoneyParser
import com.matiasdev.elecapp.features.finance.domain.PaymentDraft
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.finance.domain.ReceiptItemDraft
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItemType
import com.matiasdev.elecapp.features.finance.domain.VisitCloseDraft
import com.matiasdev.elecapp.features.finance.domain.VisitCloseResult
import com.matiasdev.elecapp.features.finance.domain.VisitTechnicalResult
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSummary
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ClosePaymentMethod { CASH, BANK_TRANSFER, MERCADO_PAGO, MIXED }

data class FollowUpDraft(
    val date: String = "",
    val time: String = "",
    val durationMinutes: String = "",
    val reason: String = "",
    val notes: String = "",
)

data class VisitCloseUiState(
    val visit: Visit? = null,
    val client: Client? = null,
    val workSummary: VisitWorkSummary? = null,
    val now: Instant = Instant.EPOCH,
    val diagnosis: String = "",
    val workType: VisitWorkType = VisitWorkType.OTHER,
    val workPerformed: String = "",
    val workSectors: String = "",
    val workItems: String = "",
    val workTests: String = "",
    val workObservations: String = "",
    val technicalResult: VisitTechnicalResult? = null,
    val pendingWork: String = "",
    val customerNotes: String = "",
    val internalNotes: String = "",
    val laborInput: String = "",
    val laborCents: Long = 0L,
    val materialsInput: String = "",
    val materialsCents: Long = 0L,
    val selectedPaymentMethod: ClosePaymentMethod = ClosePaymentMethod.CASH,
    val mixedCashInput: String = "",
    val mixedCashCents: Long = 0L,
    val mixedTransferInput: String = "",
    val mixedTransferCents: Long = 0L,
    val mixedMercadoPagoInput: String = "",
    val mixedMercadoPagoCents: Long = 0L,
    val transferReference: String = "",
    val mercadoPagoReference: String = "",
    val generateReceipt: Boolean = true,
    val followUpDraft: FollowUpDraft = FollowUpDraft(),
    val scheduledFollowUpVisit: Visit? = null,
    val showFollowUpForm: Boolean = false,
    val showRemoveFollowUpDialog: Boolean = false,
    val showNoChargeDialog: Boolean = false,
    val closeWithoutCharge: Boolean = false,
    val isSaving: Boolean = false,
    val validationErrors: List<String> = emptyList(),
) {
    val totalCents: Long get() = laborCents + materialsCents
    val mixedDistributedCents: Long get() = mixedCashCents + mixedTransferCents + mixedMercadoPagoCents
}

sealed interface VisitCloseEvent {
    data class Saved(val result: VisitCloseResult) : VisitCloseEvent
    data class Message(val text: String) : VisitCloseEvent
}

class VisitCloseViewModel(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val financeRepository: FinanceRepository,
    private val visitId: String,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VisitCloseUiState(now = timeProvider.now()))
    val uiState: StateFlow<VisitCloseUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<VisitCloseEvent>()
    val events = _events.asSharedFlow()
    private var saved = false

    init {
        viewModelScope.launch(ioDispatcher) {
            val visit = visitRepository.findActiveById(visitId)
            val client = visit?.let { clientRepository.findById(it.clientId) }
            val sessions = workSessionRepository.getSessionsForVisit(visitId)
            _uiState.update {
                it.copy(
                    visit = visit,
                    client = client,
                    now = timeProvider.now(),
                    workPerformed = visit?.completionNotes.orEmpty(),
                    pendingWork = visit?.pendingWorkNotes.orEmpty(),
                    workSummary = visit?.let { current -> VisitWorkSessionDurations.summarize(current, sessions, timeProvider.now()) },
                    followUpDraft = FollowUpDraft(reason = visit?.pendingWorkNotes.orEmpty(), notes = client?.addressLine().orEmpty()),
                )
            }
        }
    }

    fun updateText(field: VisitCloseTextField, value: String) {
        _uiState.update { state ->
            when (field) {
                VisitCloseTextField.DIAGNOSIS -> state.copy(diagnosis = value)
                VisitCloseTextField.WORK -> state.copy(workPerformed = value)
                VisitCloseTextField.WORK_SECTORS -> state.copy(workSectors = value)
                VisitCloseTextField.WORK_ITEMS -> state.copy(workItems = value)
                VisitCloseTextField.WORK_TESTS -> state.copy(workTests = value)
                VisitCloseTextField.WORK_OBSERVATIONS -> state.copy(workObservations = value)
                VisitCloseTextField.PENDING -> state.copy(pendingWork = value)
                VisitCloseTextField.CUSTOMER_NOTES -> state.copy(customerNotes = value)
                VisitCloseTextField.INTERNAL_NOTES -> state.copy(internalNotes = value)
                VisitCloseTextField.TRANSFER_REFERENCE -> state.copy(transferReference = value)
                VisitCloseTextField.MERCADO_PAGO_REFERENCE -> state.copy(mercadoPagoReference = value)
            }.copy(validationErrors = emptyList())
        }
    }

    fun selectWorkType(type: VisitWorkType) {
        _uiState.update { it.copy(workType = type, validationErrors = emptyList()) }
    }

    fun selectTechnicalResult(result: VisitTechnicalResult) {
        _uiState.update { it.copy(technicalResult = result, validationErrors = emptyList()) }
    }

    fun updateMoney(field: VisitCloseMoneyField, value: String) {
        val cents = MoneyParser.parseCents(value) ?: 0L
        _uiState.update { state ->
            when (field) {
                VisitCloseMoneyField.LABOR -> state.copy(laborInput = value, laborCents = cents, closeWithoutCharge = false)
                VisitCloseMoneyField.MATERIALS -> state.copy(materialsInput = value, materialsCents = cents)
                VisitCloseMoneyField.MIXED_CASH -> state.copy(mixedCashInput = value, mixedCashCents = cents)
                VisitCloseMoneyField.MIXED_TRANSFER -> state.copy(mixedTransferInput = value, mixedTransferCents = cents)
                VisitCloseMoneyField.MIXED_MERCADO_PAGO -> state.copy(mixedMercadoPagoInput = value, mixedMercadoPagoCents = cents)
            }.copy(validationErrors = emptyList())
        }
    }

    fun selectPaymentMethod(method: ClosePaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method, validationErrors = emptyList()) }
    }

    fun selectGenerateReceipt(generateReceipt: Boolean) {
        _uiState.update { it.copy(generateReceipt = generateReceipt, validationErrors = emptyList()) }
    }

    fun showFollowUpForm() {
        _uiState.update { it.copy(showFollowUpForm = true, validationErrors = emptyList()) }
    }

    fun dismissFollowUpForm() {
        _uiState.update { it.copy(showFollowUpForm = false) }
    }

    fun updateFollowUp(transform: (FollowUpDraft) -> FollowUpDraft) {
        _uiState.update { it.copy(followUpDraft = transform(it.followUpDraft), validationErrors = emptyList()) }
    }

    fun saveFollowUp() {
        val state = _uiState.value
        val visit = state.visit ?: return
        val scheduledAt = parseFollowUpInstant(state.followUpDraft)
        if (scheduledAt == null || state.followUpDraft.reason.isBlank()) {
            _uiState.update { it.copy(validationErrors = listOf("Completá fecha, hora y motivo de la próxima visita")) }
            return
        }
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) {
                    val now = timeProvider.now()
                    val existing = state.scheduledFollowUpVisit
                    val followUp = Visit(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        clientId = visit.clientId,
                        scheduledAt = scheduledAt,
                        estimatedDurationMinutes = state.followUpDraft.durationMinutes.toIntOrNull(),
                        reason = state.followUpDraft.reason.trim(),
                        notes = state.followUpDraft.notes.trim().ifBlank { null },
                        status = existing?.status ?: VisitStatus.PENDING,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                        isDeleted = false,
                        parentVisitId = visit.id,
                    )
                    visitRepository.save(followUp)
                    followUp
                }
            }.onSuccess { followUp ->
                _uiState.update { it.copy(scheduledFollowUpVisit = followUp, showFollowUpForm = false, validationErrors = emptyList()) }
                _events.emit(VisitCloseEvent.Message("Próxima visita agendada"))
            }.onFailure { error ->
                _uiState.update { it.copy(validationErrors = listOf(error.message ?: "No se pudo agendar la próxima visita")) }
            }
        }
    }

    fun requestRemoveFollowUp() {
        _uiState.update { it.copy(showRemoveFollowUpDialog = true) }
    }

    fun detachFollowUp() {
        _uiState.update { it.copy(scheduledFollowUpVisit = null, showRemoveFollowUpDialog = false) }
    }

    fun deleteFollowUp() {
        val followUp = _uiState.value.scheduledFollowUpVisit ?: return
        viewModelScope.launch(ioDispatcher) {
            visitRepository.softDelete(followUp.id)
            _uiState.update { it.copy(scheduledFollowUpVisit = null, showRemoveFollowUpDialog = false) }
        }
    }

    fun requestNoCharge() {
        _uiState.update { it.copy(showNoChargeDialog = true) }
    }

    fun confirmNoCharge() {
        _uiState.update {
            it.copy(
                showNoChargeDialog = false,
                closeWithoutCharge = true,
                laborInput = "",
                laborCents = 0L,
                materialsInput = "",
                materialsCents = 0L,
                validationErrors = emptyList(),
            )
        }
    }

    fun dismissNoCharge() {
        _uiState.update { it.copy(showNoChargeDialog = false) }
    }

    fun save() {
        val state = _uiState.value
        if (saved) return
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationErrors = emptyList()) }
            runCatching {
                withContext(ioDispatcher) { financeRepository.closeVisit(visitId, state.toDraft(timeProvider.now())) }
            }.onSuccess { result ->
                saved = true
                _events.emit(VisitCloseEvent.Message("Visita finalizada"))
                _events.emit(VisitCloseEvent.Saved(result))
            }.onFailure { error ->
                _uiState.update { it.copy(validationErrors = listOf(error.message ?: "No se pudo finalizar la visita")) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun validate(state: VisitCloseUiState): List<String> = buildList {
        if (state.workPerformed.isBlank()) add("Ingresá el trabajo realizado")
        if (state.technicalResult == null) add("Seleccioná el resultado de la visita")
        if (state.generateReceipt && !state.closeWithoutCharge && state.laborCents <= 0L) add("La mano de obra debe ser mayor a cero")
        if (state.generateReceipt && state.materialsCents < 0L) add("Materiales no puede ser negativo")
        if (state.generateReceipt && state.selectedPaymentMethod == ClosePaymentMethod.MIXED && !state.closeWithoutCharge) {
            if (state.mixedDistributedCents != state.totalCents) add("La suma del pago mixto debe coincidir con el total")
            if (state.mixedDistributedCents <= 0L) add("Distribuí el total entre los métodos de pago")
        }
    }

    private fun VisitCloseUiState.toDraft(now: Instant): VisitCloseDraft {
        val items = buildList {
            if (generateReceipt && laborCents > 0L) add(ReceiptItemDraft(ServiceReceiptItemType.LABOR, "Mano de obra", 1_000, laborCents))
            if (generateReceipt && materialsCents > 0L) add(ReceiptItemDraft(ServiceReceiptItemType.MATERIAL, "Materiales", 1_000, materialsCents))
        }
        val paymentNotes = scheduledFollowUpVisit?.let { followUp -> followUpText(followUp) }
        return VisitCloseDraft(
            diagnosis = diagnosis,
            workType = workType,
            workPerformed = workPerformed,
            workSectors = workSectors,
            workItems = workItems,
            workTests = workTests,
            workObservations = workObservations,
            technicalResult = technicalResult,
            pendingWork = pendingWork,
            requiresFollowUp = scheduledFollowUpVisit != null,
            followUpSuggestedAt = scheduledFollowUpVisit?.scheduledAt,
            internalNotes = internalNotes,
            customerNotes = listOf(customerNotes.trim().ifBlank { null }, paymentNotes).filterNotNull().joinToString("\n\n").ifBlank { null },
            generateReceipt = generateReceipt,
            quoteId = null,
            receiptTitle = "Comprobante de servicio",
            receiptDescription = workPerformed,
            items = items,
            discountCents = 0L,
            initialPayments = if (!generateReceipt || closeWithoutCharge) emptyList() else paymentDrafts(now),
        )
    }

    private fun VisitCloseUiState.paymentDrafts(now: Instant): List<PaymentDraft> {
        return when (selectedPaymentMethod) {
            ClosePaymentMethod.CASH -> listOf(PaymentDraft(totalCents, PaymentMethod.CASH, now))
            ClosePaymentMethod.BANK_TRANSFER -> listOf(PaymentDraft(totalCents, PaymentMethod.BANK_TRANSFER, now, transferReference))
            ClosePaymentMethod.MERCADO_PAGO -> listOf(PaymentDraft(totalCents, PaymentMethod.MERCADO_PAGO, now, mercadoPagoReference))
            ClosePaymentMethod.MIXED -> buildList {
                if (mixedCashCents > 0L) add(PaymentDraft(mixedCashCents, PaymentMethod.CASH, now))
                if (mixedTransferCents > 0L) add(PaymentDraft(mixedTransferCents, PaymentMethod.BANK_TRANSFER, now, transferReference))
                if (mixedMercadoPagoCents > 0L) add(PaymentDraft(mixedMercadoPagoCents, PaymentMethod.MERCADO_PAGO, now, mercadoPagoReference))
            }
        }
    }

    private fun parseFollowUpInstant(draft: FollowUpDraft): Instant? {
        return runCatching {
            LocalDateTime.parse("${draft.date.trim()} ${draft.time.trim()}", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                .atZone(ZoneId.systemDefault())
                .toInstant()
        }.getOrNull()
    }
}

enum class VisitCloseTextField {
    DIAGNOSIS,
    WORK,
    WORK_SECTORS,
    WORK_ITEMS,
    WORK_TESTS,
    WORK_OBSERVATIONS,
    PENDING,
    CUSTOMER_NOTES,
    INTERNAL_NOTES,
    TRANSFER_REFERENCE,
    MERCADO_PAGO_REFERENCE,
}

enum class VisitCloseMoneyField { LABOR, MATERIALS, MIXED_CASH, MIXED_TRANSFER, MIXED_MERCADO_PAGO }

fun Client.addressLine(): String = listOfNotNull(address?.takeIf(String::isNotBlank), locality?.takeIf(String::isNotBlank)).joinToString(", ")

fun followUpText(visit: Visit): String {
    val date = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm").format(visit.scheduledAt.atZone(ZoneId.systemDefault()))
    return "Próxima visita agendada:\n$date\n\nMotivo:\n${visit.reason}"
}

class VisitCloseViewModelFactory(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val workSessionRepository: VisitWorkSessionRepository,
    private val financeRepository: FinanceRepository,
    private val visitId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VisitCloseViewModel(clientRepository, visitRepository, workSessionRepository, financeRepository, visitId) as T
    }
}
