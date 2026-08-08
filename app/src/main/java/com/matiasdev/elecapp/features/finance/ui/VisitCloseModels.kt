package com.matiasdev.elecapp.features.finance.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.domain.VisitCloseResult
import com.matiasdev.elecapp.features.finance.domain.VisitTechnicalResult
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    data object WorkSaved : VisitCloseEvent
    data class Message(val text: String) : VisitCloseEvent
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
