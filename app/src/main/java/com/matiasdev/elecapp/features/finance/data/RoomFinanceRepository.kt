package com.matiasdev.elecapp.features.finance.data

import androidx.room.withTransaction
import com.matiasdev.elecapp.core.time.SystemTimeProvider
import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.data.AppDatabase
import com.matiasdev.elecapp.features.clients.data.ClientDao
import com.matiasdev.elecapp.features.clients.data.ClientEntity
import com.matiasdev.elecapp.features.finance.domain.FinanceMetrics
import com.matiasdev.elecapp.features.finance.domain.FinanceMetricsCalculator
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentBalanceCalculator
import com.matiasdev.elecapp.features.finance.domain.PaymentDraft
import com.matiasdev.elecapp.features.finance.domain.PaymentStatus
import com.matiasdev.elecapp.features.finance.domain.QuickVisitClientMode
import com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft
import com.matiasdev.elecapp.features.finance.domain.ReceiptCalculator
import com.matiasdev.elecapp.features.finance.domain.ReceiptStatusResolver
import com.matiasdev.elecapp.features.finance.domain.RegisterPaymentResult
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptStatus
import com.matiasdev.elecapp.features.finance.domain.VisitCloseDraft
import com.matiasdev.elecapp.features.finance.domain.VisitCloseResult
import com.matiasdev.elecapp.features.finance.domain.VisitCompletion
import com.matiasdev.elecapp.features.visits.data.VisitDao
import com.matiasdev.elecapp.features.visits.data.VisitEntity
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionDao
import com.matiasdev.elecapp.features.visits.data.VisitWorkSessionEntity
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFinanceRepository(
    private val database: AppDatabase,
    private val clientDao: ClientDao,
    private val visitDao: VisitDao,
    private val sessionDao: VisitWorkSessionDao,
    private val completionDao: VisitCompletionDao,
    private val receiptDao: ServiceReceiptDao,
    private val itemDao: ServiceReceiptItemDao,
    private val paymentDao: PaymentDao,
    private val sequenceDao: ReceiptSequenceDao,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : FinanceRepository {
    override fun observeReceipts(): Flow<List<ServiceReceipt>> = receiptDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeReceiptsByClient(clientId: String): Flow<List<ServiceReceipt>> {
        return receiptDao.observeByClientId(clientId).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeReceiptById(id: String): Flow<ServiceReceipt?> = receiptDao.observeById(id).map { it?.toDomain() }

    override fun observeReceiptByVisitId(visitId: String): Flow<ServiceReceipt?> = receiptDao.observeByVisitId(visitId).map { it?.toDomain() }

    override fun observeItems(receiptId: String): Flow<List<ServiceReceiptItem>> {
        return itemDao.observeByReceiptId(receiptId).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observePayments(receiptId: String): Flow<List<Payment>> {
        return paymentDao.observeByReceiptId(receiptId).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observePaymentsByClient(clientId: String): Flow<List<Payment>> {
        return paymentDao.observeByClientId(clientId).map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeVisitCompletion(visitId: String): Flow<VisitCompletion?> {
        return completionDao.observeByVisitId(visitId).map { it?.toDomain() }
    }

    override suspend fun hasAnotherRunningVisit(): Boolean = sessionDao.getAllActive().isNotEmpty()

    override suspend fun startQuickVisit(draft: QuickVisitDraft, pauseRunningVisit: Boolean): String = database.withTransaction {
        val now = timeProvider.now()
        val activeSessions = sessionDao.getAllActive()
        if (activeSessions.isNotEmpty() && !pauseRunningVisit) error("Ya hay una visita en curso")
        activeSessions.forEach { sessionDao.closeActiveSession(it.visitId, now.toEpochMilli(), VisitWorkSessionStatus.PAUSED.name, now.toEpochMilli()) }
        val clientId = if (draft.clientMode == QuickVisitClientMode.QUICK_CREATE) {
            val id = UUID.randomUUID().toString()
            clientDao.upsert(
                ClientEntity(
                    id = id,
                    fullName = draft.quickClientName.trim(),
                    phone = draft.phone.trim(),
                    email = null,
                    address = draft.address.trim().ifBlank { null },
                    locality = draft.locality.trim().ifBlank { null },
                    notes = null,
                    createdAt = now.toEpochMilli(),
                    updatedAt = now.toEpochMilli(),
                    isDeleted = false,
                ),
            )
            id
        } else {
            draft.selectedClientId ?: error("Seleccioná un cliente")
        }
        val visitId = UUID.randomUUID().toString()
        visitDao.upsert(
            VisitEntity(
                id = visitId,
                clientId = clientId,
                scheduledAt = now.toEpochMilli(),
                estimatedDurationMinutes = draft.estimatedDurationMinutes.toIntOrNull(),
                reason = draft.attentionType.label,
                notes = draft.briefDetail.trim().ifBlank { null },
                status = VisitStatus.IN_PROGRESS.name,
                startedAt = now.toEpochMilli(),
                completedAt = null,
                completionNotes = null,
                pendingWorkNotes = null,
                attentionType = draft.attentionType.name,
                parentVisitId = null,
                createdAt = now.toEpochMilli(),
                updatedAt = now.toEpochMilli(),
                isDeleted = false,
            ),
        )
        sessionDao.insert(newSession(visitId, now))
        visitId
    }

    override suspend fun closeVisit(visitId: String, draft: VisitCloseDraft): VisitCloseResult = database.withTransaction {
        val visit = visitDao.findActiveById(visitId) ?: error("Visita no encontrada")
        if (VisitStatus.valueOf(visit.status) != VisitStatus.IN_PROGRESS) error("La visita no está en curso")
        val now = timeProvider.now()
        sessionDao.getActiveByVisitId(visitId)?.let {
            sessionDao.closeActiveSession(visitId, now.toEpochMilli(), VisitWorkSessionStatus.COMPLETED.name, now.toEpochMilli())
        }
        visitDao.completeVisit(visitId, now.toEpochMilli(), draft.workPerformed.trim(), draft.pendingWork?.trim()?.ifBlank { null }, now.toEpochMilli())
        completionDao.upsert(newCompletion(visitId, draft, now))
        val receiptId = if (draft.generateReceipt) createIssuedReceipt(visit.clientId, visitId, draft, now) else null
        VisitCloseResult(visitId = visitId, receiptId = receiptId)
    }

    override suspend fun registerPayment(
        receiptId: String?,
        clientId: String,
        visitId: String?,
        draft: PaymentDraft,
    ): RegisterPaymentResult = database.withTransaction {
        require(draft.amountCents > 0L) { "El monto debe ser mayor a cero" }
        val receipt = receiptId?.let { receiptDao.findById(it)?.toDomain() ?: error("Comprobante no encontrado") }
        if (receipt != null) {
            val existing = paymentDao.findByReceiptId(receipt.id).map { it.toDomain() }
            val balance = PaymentBalanceCalculator.balance(receipt.totalCents, existing)
            if (draft.amountCents > balance.pendingCents) error("El pago supera el saldo pendiente")
        }
        val paymentId = UUID.randomUUID().toString()
        paymentDao.insert(newPayment(paymentId, clientId, visitId, receiptId, draft, timeProvider.now()))
        val status = receipt?.let { updateReceiptStatus(it.id) }
        RegisterPaymentResult(paymentId, status)
    }

    override suspend fun financeMetrics(start: Instant, endExclusive: Instant): FinanceMetrics {
        val receipts = receiptDao.findValidInRange(start.toEpochMilli(), endExclusive.toEpochMilli()).map { it.toDomain() }
        val payments = paymentDao.findByDateRange(start.toEpochMilli(), endExclusive.toEpochMilli()).map { it.toDomain() }
        val visits = visitDao.findCompletedInRange(start.toEpochMilli(), endExclusive.toEpochMilli())
        val minutes = visits.sumOf { visit ->
            val startMillis = visit.startedAt ?: visit.scheduledAt
            val endMillis = visit.completedAt ?: startMillis
            ((endMillis - startMillis) / 60_000L).coerceAtLeast(0L)
        }
        return FinanceMetricsCalculator.calculate(receipts, payments, visits.size, minutes, visits.map { it.clientId }.distinct().size)
    }

    private suspend fun createIssuedReceipt(clientId: String, visitId: String, draft: VisitCloseDraft, now: Instant): String {
        val receiptId = UUID.randomUUID().toString()
        val items = draft.items.mapIndexed { index, item ->
            val total = if (item.isChargeable) ReceiptCalculator.lineTotal(item.quantityMillis, item.unitPriceCents) else 0L
            ServiceReceiptItem(receiptId = receiptId, id = UUID.randomUUID().toString(), type = item.type, description = item.description.trim(), quantityMillis = item.quantityMillis, unitPriceCents = item.unitPriceCents, totalCents = total, sourceType = item.sourceType, sourceId = item.sourceId, suppliedBy = item.suppliedBy, isChargeable = item.isChargeable, sortOrder = index, notes = item.notes?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now, isDeleted = false)
        }
        val totals = ReceiptCalculator.totals(items, draft.discountCents)
        val receipt = ServiceReceipt(receiptId, nextReceiptNumber(now), clientId, visitId, draft.quoteId, now, draft.receiptTitle.trim().ifBlank { "Comprobante de servicio" }, draft.receiptDescription?.trim()?.ifBlank { null }, ServiceReceiptStatus.ISSUED, totals.subtotalLaborCents, totals.subtotalMaterialsCents, totals.subtotalOtherCents, totals.discountCents, totals.totalCents, draft.customerNotes?.trim()?.ifBlank { null }, draft.internalNotes?.trim()?.ifBlank { null }, now, now, false)
        receiptDao.insert(receipt.toEntity())
        itemDao.insertAll(items.map { it.toEntity() })
        draft.initialPayments.forEach { payment ->
            registerPaymentInside(receipt, clientId, visitId, payment, now)
        }
        updateReceiptStatus(receiptId)
        return receiptId
    }

    private suspend fun registerPaymentInside(receipt: ServiceReceipt, clientId: String, visitId: String?, draft: PaymentDraft, now: Instant) {
        require(draft.amountCents > 0L) { "El monto debe ser mayor a cero" }
        val existing = paymentDao.findByReceiptId(receipt.id).map { it.toDomain() }
        if (draft.amountCents > PaymentBalanceCalculator.balance(receipt.totalCents, existing).pendingCents) error("El pago supera el saldo pendiente")
        paymentDao.insert(newPayment(UUID.randomUUID().toString(), clientId, visitId, receipt.id, draft, now))
    }

    private suspend fun nextReceiptNumber(now: Instant): Long {
        sequenceDao.insert(ReceiptSequenceEntity("service_receipt", 1L, now.toEpochMilli()))
        val next = sequenceDao.find()?.nextNumber ?: 1L
        sequenceDao.updateNext(next + 1, now.toEpochMilli())
        return next
    }

    private suspend fun updateReceiptStatus(receiptId: String): ServiceReceiptStatus {
        val receipt = receiptDao.findById(receiptId)?.toDomain() ?: error("Comprobante no encontrado")
        val status = ReceiptStatusResolver.resolve(receipt, paymentDao.findByReceiptId(receiptId).map { it.toDomain() })
        receiptDao.update(receipt.copy(status = status, updatedAt = timeProvider.now()).toEntity())
        return status
    }

    private fun newCompletion(visitId: String, draft: VisitCloseDraft, now: Instant): VisitCompletionEntity {
        return VisitCompletion(
            id = UUID.randomUUID().toString(),
            visitId = visitId,
            diagnosis = draft.diagnosis?.trim()?.ifBlank { null },
            workType = draft.workType,
            workPerformed = draft.workPerformed.trim(),
            workSectors = draft.workSectors?.trim()?.ifBlank { null },
            workItems = draft.workItems?.trim()?.ifBlank { null },
            workTests = draft.workTests?.trim()?.ifBlank { null },
            workObservations = draft.workObservations?.trim()?.ifBlank { null },
            technicalResult = draft.technicalResult,
            pendingWork = draft.pendingWork?.trim()?.ifBlank { null },
            requiresFollowUp = draft.requiresFollowUp,
            followUpSuggestedAt = draft.followUpSuggestedAt,
            internalNotes = draft.internalNotes?.trim()?.ifBlank { null },
            customerNotes = draft.customerNotes?.trim()?.ifBlank { null },
            completedAt = now,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        ).toEntity()
    }

    private fun newSession(visitId: String, now: Instant): VisitWorkSessionEntity {
        return VisitWorkSessionEntity(UUID.randomUUID().toString(), visitId, now.toEpochMilli(), null, VisitWorkSessionStatus.RUNNING.name, null, now.toEpochMilli(), now.toEpochMilli(), false)
    }

    private fun newPayment(id: String, clientId: String, visitId: String?, receiptId: String?, draft: PaymentDraft, now: Instant): PaymentEntity {
        return Payment(id, clientId, visitId, receiptId, draft.amountCents, draft.method, draft.paidAt, draft.reference?.trim()?.ifBlank { null }, draft.notes?.trim()?.ifBlank { null }, PaymentStatus.CONFIRMED, now, now, false).toEntity()
    }
}
