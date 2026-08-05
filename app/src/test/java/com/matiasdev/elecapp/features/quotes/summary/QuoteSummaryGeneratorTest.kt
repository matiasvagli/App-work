package com.matiasdev.elecapp.features.quotes.summary

import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteItem
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class QuoteSummaryGeneratorTest {
    @Test
    fun `generates deterministic quote text and omits empty fields`() {
        val text = QuoteSummaryGenerator.generate(
            quote = quote(description = null, generalNotes = null),
            items = listOf(item("Adecuación de tablero", 120_000_00L)),
            context = QuoteSummaryContext("Carlos López", "Av. X 1234", "Temperley", false, null),
            zoneId = ZoneId.of("America/Argentina/Buenos_Aires"),
        )

        assertTrue(text.contains("PRESUPUESTO PRES-2026-0001"))
        assertTrue(text.contains("Cliente: Carlos López"))
        assertTrue(text.contains("Los materiales no están incluidos"))
        assertFalse(text.contains("Observaciones:"))
    }

    @Test
    fun `mentions linked material list without material prices`() {
        val text = QuoteSummaryGenerator.generate(
            quote = quote(description = "Trabajo completo", generalNotes = "Revisar antes de enviar."),
            items = listOf(item("Identificación de circuitos", 90_000_00L)),
            context = QuoteSummaryContext("Ana", null, null, true, null),
        )

        assertTrue(text.contains("Se entrega una lista de materiales separada."))
        assertFalse(text.contains("Precio estimado"))
    }

    private fun quote(description: String?, generalNotes: String?): Quote {
        val now = Instant.parse("2026-08-04T12:00:00Z")
        return Quote(
            id = "quote",
            clientId = "client",
            visitId = null,
            inspectionId = null,
            quoteNumber = "PRES-2026-0001",
            title = "Adecuación eléctrica",
            description = description,
            status = QuoteStatus.READY,
            currency = QuoteCurrency.ARS,
            subtotalAmount = 120_000_00L,
            discountType = DiscountType.NONE,
            discountValue = 0L,
            totalAmount = 120_000_00L,
            validUntil = null,
            paymentTerms = null,
            generalNotes = generalNotes,
            clientMessage = null,
            sentAt = null,
            approvedAt = null,
            rejectedAt = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }

    private fun item(description: String, total: Long): QuoteItem {
        val now = Instant.parse("2026-08-04T12:00:00Z")
        return QuoteItem(
            id = description,
            quoteId = "quote",
            type = QuoteItemType.SERVICE,
            description = description,
            quantity = 1.0,
            unit = QuoteUnit.FIXED,
            customUnitLabel = null,
            unitPriceAmount = total,
            lineTotalAmount = total,
            sortOrder = 0,
            notes = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}
