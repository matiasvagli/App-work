package com.matiasdev.elecapp.features.finance.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.finance.domain.Payment
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.finance.domain.PaymentStatus
import com.matiasdev.elecapp.features.finance.domain.ReceiptItemSourceType
import com.matiasdev.elecapp.features.finance.domain.ServiceReceipt
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItem
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptItemType
import com.matiasdev.elecapp.features.finance.domain.ServiceReceiptStatus
import com.matiasdev.elecapp.features.finance.domain.MaterialSuppliedBy
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptShareTextTest {
    @Test
    fun `shared receipt includes payment methods and follow up but not internal notes`() {
        val receipt = receipt(
            notes = "Nota para cliente\n\nPróxima visita agendada:\n12/08/2026 a las 14:30\n\nMotivo:\nRevisión del patio",
            internalNotes = "No compartir",
        )
        val text = ReceiptShareText.build(
            receipt = receipt,
            client = client,
            items = listOf(item("Mano de obra", 80_000_00), item("Materiales", 20_000_00)),
            payments = listOf(payment(30_000_00, PaymentMethod.CASH), payment(70_000_00, PaymentMethod.BANK_TRANSFER)),
        )

        assertTrue(text.contains("Forma de pago:"))
        assertTrue(text.contains("Efectivo: $ 30.000"))
        assertTrue(text.contains("Transferencia: $ 70.000"))
        assertTrue(text.contains("Próxima visita agendada"))
        assertFalse(text.contains("No compartir"))
    }

    private fun receipt(notes: String?, internalNotes: String?): ServiceReceipt {
        return ServiceReceipt("receipt", 23L, "client", "visit", null, now, "Servicio", "Trabajo realizado", ServiceReceiptStatus.PAID, 80_000_00, 20_000_00, 0, 0, 100_000_00, notes, internalNotes, now, now, false)
    }

    private fun item(description: String, total: Long): ServiceReceiptItem {
        return ServiceReceiptItem("item-$description", "receipt", ServiceReceiptItemType.LABOR, description, 1_000, total, total, ReceiptItemSourceType.MANUAL, null, MaterialSuppliedBy.UNKNOWN, true, 0, null, now, now, false)
    }

    private fun payment(amount: Long, method: PaymentMethod): Payment {
        return Payment("payment-$amount", "client", "visit", "receipt", amount, method, now, null, null, PaymentStatus.CONFIRMED, now, now, false)
    }

    private companion object {
        val now: Instant = Instant.parse("2026-08-05T12:00:00Z")
        val client = Client("client", "Carlos López", "111", null, "Av. Espora 1234", "Adrogué", null, now, now, false)
    }
}
