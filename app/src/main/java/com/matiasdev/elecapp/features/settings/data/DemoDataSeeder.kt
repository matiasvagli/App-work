package com.matiasdev.elecapp.features.settings.data

import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.data.ClientRepository
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.data.FinanceRepository
import com.matiasdev.elecapp.features.finance.domain.PaymentDraft
import com.matiasdev.elecapp.features.finance.domain.QuickVisitClientMode
import com.matiasdev.elecapp.features.finance.domain.QuickVisitDraft
import com.matiasdev.elecapp.features.finance.domain.ReceiptItemDraft
import com.matiasdev.elecapp.features.finance.domain.VisitAttentionType
import com.matiasdev.elecapp.features.finance.domain.VisitCloseDraft
import com.matiasdev.elecapp.features.finance.domain.VisitTechnicalResult
import com.matiasdev.elecapp.features.finance.domain.VisitWorkType
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItemType
import com.matiasdev.elecapp.features.quotes.data.QuoteRepository
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit
import com.matiasdev.elecapp.features.visits.data.VisitRepository
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Duration
import java.time.Instant
import java.util.UUID

// Valores en centavos, del orden de la lista de referencia de mano de obra.
private const val HOUR_RATE_CENTS = 5_431_000L
private const val OUTLET_CENTS = 10_843_100L
private const val MAIN_PANEL_CENTS = 32_031_000L
private const val BREAKER_CENTS = 2_500_000L

/**
 * Genera un juego de datos de ejemplo coherente para probar la app.
 *
 * No inserta filas a mano: usa las mismas operaciones que la UI (`startQuickVisit`, `closeVisit`),
 * así los comprobantes, la numeración, los saldos y las sesiones de trabajo quedan consistentes
 * sin duplicar reglas de negocio.
 *
 * Todas las fechas son relativas a `now`, para que el ejemplo no envejezca.
 */
class DemoDataSeeder(
    private val clientRepository: ClientRepository,
    private val visitRepository: VisitRepository,
    private val financeRepository: FinanceRepository,
    private val quoteRepository: QuoteRepository,
    private val timeProvider: TimeProvider,
) {
    suspend fun seed() {
        val now = timeProvider.now()

        val maria = newClient(
            fullName = "María Fernández",
            phone = "1145678901",
            address = "Av. Rivadavia 4820",
            locality = "Caballito",
            notes = "Prefiere que la avisen media hora antes.",
            now = now,
        )
        val deposito = newClient(
            fullName = "Depósito Lomas SRL",
            phone = "1156783412",
            address = "Colón 1145",
            locality = "Lomas de Zamora",
            notes = "Entrada por el portón lateral. Preguntar por Rubén.",
            now = now,
        )
        val carlos = newClient(
            fullName = "Carlos Gutiérrez",
            phone = "1134567788",
            address = "Belgrano 372",
            locality = "Adrogué",
            notes = null,
            now = now,
        )
        listOf(maria, deposito, carlos).forEach { clientRepository.save(it) }

        seedPaidVisit(carlos, now)
        seedPartiallyPaidVisit(maria, now)
        seedScheduledVisit(carlos, now)
        seedQuote(carlos, now)

        // Se deja para el final: la app admite una sola visita en curso a la vez.
        seedVisitInProgress(deposito)
    }

    /** Trabajo cerrado y cobrado por completo: alimenta "cobrado" en Economía. */
    private suspend fun seedPaidVisit(client: Client, now: Instant) {
        val visitId = startVisit(client, VisitAttentionType.REPAIR, "Cambio de tomas en cocina")
        financeRepository.closeVisit(
            visitId = visitId,
            draft = closeDraft(
                workPerformed = "Reemplazo de tres tomacorrientes y revisión de la línea de cocina.",
                technicalResult = VisitTechnicalResult.RESOLVED,
                receiptTitle = "Cambio de tomas en cocina",
                items = listOf(
                    receiptItem(ServiceReceiptItemType.LABOR, "Mano de obra", hours = 2.0, unitPriceCents = HOUR_RATE_CENTS),
                    receiptItem(ServiceReceiptItemType.MATERIAL, "Tomacorrientes", units = 3.0, unitPriceCents = 1_200_000L),
                ),
                payments = listOf(
                    PaymentDraft(
                        amountCents = 2 * HOUR_RATE_CENTS + 3 * 1_200_000L,
                        method = PaymentMethod.CASH,
                        paidAt = now.minus(Duration.ofDays(6)),
                    ),
                ),
            ),
        )
    }

    /** Trabajo cerrado con seña: deja saldo pendiente, que es el caso que más conviene mirar. */
    private suspend fun seedPartiallyPaidVisit(client: Client, now: Instant) {
        val visitId = startVisit(client, VisitAttentionType.INSTALLATION, "Tablero principal nuevo")
        financeRepository.closeVisit(
            visitId = visitId,
            draft = closeDraft(
                workPerformed = "Instalación de tablero principal con disyuntor, térmica y puesta a tierra.",
                technicalResult = VisitTechnicalResult.RESOLVED,
                receiptTitle = "Tablero principal con PAT",
                items = listOf(
                    receiptItem(ServiceReceiptItemType.LABOR, "Tablero principal con 1 ID y 1 TM + PAT", units = 1.0, unitPriceCents = MAIN_PANEL_CENTS),
                    receiptItem(ServiceReceiptItemType.MATERIAL, "Térmica bipolar", units = 2.0, unitPriceCents = BREAKER_CENTS),
                ),
                payments = listOf(
                    PaymentDraft(
                        amountCents = 15_000_000L,
                        method = PaymentMethod.BANK_TRANSFER,
                        paidAt = now.minus(Duration.ofDays(2)),
                        notes = "Seña acordada al iniciar.",
                    ),
                ),
            ),
        )
    }

    /** Visita en curso, con sesión de trabajo corriendo: alimenta la tarjeta principal de Inicio. */
    private suspend fun seedVisitInProgress(client: Client) {
        startVisit(client, VisitAttentionType.DIAGNOSTIC, "Se dispara el disyuntor por la mañana")
    }

    /** Visita futura confirmada: alimenta Agenda y el próximo turno. */
    private suspend fun seedScheduledVisit(client: Client, now: Instant) {
        visitRepository.save(
            Visit(
                id = UUID.randomUUID().toString(),
                clientId = client.id,
                scheduledAt = now.plus(Duration.ofDays(2)),
                estimatedDurationMinutes = 90,
                reason = "Agregar circuito para aire acondicionado",
                notes = "Confirmar si ya compró el equipo.",
                status = VisitStatus.CONFIRMED,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        )
    }

    private suspend fun seedQuote(client: Client, now: Instant) {
        val quoteId = UUID.randomUUID().toString()
        val items = listOf(
            demoQuoteItem(quoteId, 0, QuoteItemType.LABOR, "Boca completa", 6.0, QuoteUnit.UNIT, OUTLET_CENTS),
            demoQuoteItem(quoteId, 1, QuoteItemType.LABOR, "Mano de obra adicional", 4.0, QuoteUnit.HOUR, HOUR_RATE_CENTS),
            demoQuoteItem(quoteId, 2, QuoteItemType.MATERIAL, "Cable 2,5 mm²", 40.0, QuoteUnit.METER, 320_000L),
        )
        val subtotal = items.sumOf { it.lineTotalAmount }
        quoteRepository.saveQuoteWithItems(
            quote = Quote(
                id = quoteId,
                clientId = client.id,
                visitId = null,
                inspectionId = null,
                quoteNumber = quoteRepository.nextQuoteNumber(now),
                title = "Circuito nuevo para aire acondicionado",
                description = "Tendido de circuito independiente desde el tablero.",
                status = QuoteStatus.SENT,
                currency = QuoteCurrency.ARS,
                subtotalAmount = subtotal,
                discountType = DiscountType.NONE,
                discountValue = 0L,
                totalAmount = subtotal,
                validUntil = now.plus(Duration.ofDays(15)),
                paymentTerms = "50% al iniciar, 50% al finalizar.",
                generalNotes = null,
                clientMessage = null,
                sentAt = now.minus(Duration.ofDays(1)),
                approvedAt = null,
                rejectedAt = null,
                createdAt = now.minus(Duration.ofDays(1)),
                updatedAt = now.minus(Duration.ofDays(1)),
                isDeleted = false,
            ),
            items = items,
        )
    }

    private suspend fun startVisit(client: Client, type: VisitAttentionType, detail: String): String {
        return financeRepository.startQuickVisit(
            draft = QuickVisitDraft(
                clientMode = QuickVisitClientMode.EXISTING,
                selectedClientId = client.id,
                attentionType = type,
                briefDetail = detail,
            ),
            pauseRunningVisit = true,
        )
    }

    private fun newClient(
        fullName: String,
        phone: String,
        address: String,
        locality: String,
        notes: String?,
        now: Instant,
    ) = Client(
        id = UUID.randomUUID().toString(),
        fullName = fullName,
        phone = phone,
        email = null,
        address = address,
        locality = locality,
        notes = notes,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )

    private fun closeDraft(
        workPerformed: String,
        technicalResult: VisitTechnicalResult,
        receiptTitle: String,
        items: List<ReceiptItemDraft>,
        payments: List<PaymentDraft>,
    ) = VisitCloseDraft(
        diagnosis = null,
        workType = VisitWorkType.REPAIR,
        workPerformed = workPerformed,
        workSectors = null,
        workItems = null,
        workTests = null,
        workObservations = null,
        technicalResult = technicalResult,
        pendingWork = null,
        requiresFollowUp = false,
        followUpSuggestedAt = null,
        internalNotes = null,
        customerNotes = null,
        generateReceipt = true,
        quoteId = null,
        receiptTitle = receiptTitle,
        receiptDescription = null,
        items = items,
        discountCents = 0L,
        initialPayments = payments,
    )

    private fun receiptItem(
        type: ServiceReceiptItemType,
        description: String,
        hours: Double? = null,
        units: Double? = null,
        unitPriceCents: Long,
    ) = ReceiptItemDraft(
        type = type,
        description = description,
        quantityMillis = ((hours ?: units ?: 1.0) * 1_000).toLong(),
        unitPriceCents = unitPriceCents,
    )

    private fun demoQuoteItem(
        quoteId: String,
        sortOrder: Int,
        type: QuoteItemType,
        description: String,
        quantity: Double,
        unit: QuoteUnit,
        unitPriceCents: Long,
    ): QuoteItem {
        val now = timeProvider.now()
        return QuoteItem(
            id = UUID.randomUUID().toString(),
            quoteId = quoteId,
            type = type,
            description = description,
            quantity = quantity,
            unit = unit,
            customUnitLabel = null,
            unitPriceAmount = unitPriceCents,
            lineTotalAmount = (quantity * unitPriceCents).toLong(),
            sortOrder = sortOrder,
            notes = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}
