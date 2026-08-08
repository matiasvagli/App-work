package com.matiasdev.elecapp.features.finance.data

import com.matiasdev.elecapp.features.finance.domain.FinanceMetrics
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentDraft
import com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft
import com.matiasdev.elecapp.features.finance.domain.RegisterPaymentResult
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.VisitCloseDraft
import com.matiasdev.elecapp.features.finance.domain.VisitCloseResult
import com.matiasdev.elecapp.features.finance.domain.VisitCompletion
import com.matiasdev.elecapp.features.finance.domain.VisitWorkDraft
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun observeReceipts(): Flow<List<ServiceReceipt>>

    fun observeReceiptsByClient(clientId: String): Flow<List<ServiceReceipt>>

    fun observeReceiptById(id: String): Flow<ServiceReceipt?>

    fun observeReceiptByVisitId(visitId: String): Flow<ServiceReceipt?>

    fun observeItems(receiptId: String): Flow<List<ServiceReceiptItem>>

    fun observePayments(receiptId: String): Flow<List<Payment>>

    fun observePaymentsByClient(clientId: String): Flow<List<Payment>>

    fun observeVisitCompletion(visitId: String): Flow<VisitCompletion?>

    suspend fun hasAnotherRunningVisit(): Boolean

    suspend fun startQuickVisit(draft: QuickVisitDraft, pauseRunningVisit: Boolean): String

    suspend fun closeVisit(visitId: String, draft: VisitCloseDraft): VisitCloseResult

    suspend fun saveVisitWorkDraft(visitId: String, draft: VisitWorkDraft)

    suspend fun registerPayment(
        receiptId: String?,
        clientId: String,
        visitId: String?,
        draft: PaymentDraft,
    ): RegisterPaymentResult

    suspend fun financeMetrics(start: Instant, endExclusive: Instant): FinanceMetrics
}
