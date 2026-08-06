package com.matiasdev.elecapp.features.finance.data

import com.matiasdev.elecapp.features.finance.domain.FinanceMetrics
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentDraft
import com.matiasdev.elecapp.features.finance.domain.RegisterPaymentResult
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptStatus
import com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft
import com.matiasdev.elecapp.features.finance.domain.VisitCloseDraft
import com.matiasdev.elecapp.features.finance.domain.VisitCloseResult
import com.matiasdev.elecapp.features.finance.domain.VisitCompletion
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFinanceRepository : FinanceRepository {
    var closeCallCount: Int = 0
        private set
    var lastCloseDraft: VisitCloseDraft? = null
        private set
    var lastQuickVisitDraft: QuickVisitDraft? = null
        private set
    override fun observeReceipts(): Flow<List<ServiceReceipt>> = flowOf(emptyList())
    override fun observeReceiptsByClient(clientId: String): Flow<List<ServiceReceipt>> = flowOf(emptyList())
    override fun observeReceiptById(id: String): Flow<ServiceReceipt?> = flowOf(null)
    override fun observeReceiptByVisitId(visitId: String): Flow<ServiceReceipt?> = flowOf(null)
    override fun observeItems(receiptId: String): Flow<List<ServiceReceiptItem>> = flowOf(emptyList())
    override fun observePayments(receiptId: String): Flow<List<Payment>> = flowOf(emptyList())
    override fun observePaymentsByClient(clientId: String): Flow<List<Payment>> = flowOf(emptyList())
    override fun observeVisitCompletion(visitId: String): Flow<VisitCompletion?> = flowOf(null)
    override suspend fun hasAnotherRunningVisit(): Boolean = false
    override suspend fun startQuickVisit(draft: QuickVisitDraft, pauseRunningVisit: Boolean): String {
        lastQuickVisitDraft = draft
        return "visit"
    }
    override suspend fun closeVisit(visitId: String, draft: VisitCloseDraft): VisitCloseResult {
        closeCallCount += 1
        lastCloseDraft = draft
        return VisitCloseResult(visitId, "receipt")
    }
    override suspend fun registerPayment(receiptId: String?, clientId: String, visitId: String?, draft: PaymentDraft): RegisterPaymentResult {
        return RegisterPaymentResult("payment", ServiceReceiptStatus.ISSUED)
    }
    override suspend fun financeMetrics(start: Instant, endExclusive: Instant): FinanceMetrics {
        return FinanceMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }
}
