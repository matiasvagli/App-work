package com.matiasdev.elecapp.features.materials.summary

import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class MaterialListSummaryGeneratorTest {
    @Test
    fun `does not include prices by default`() {
        val text = MaterialListSummaryGenerator.generate(
            materialList(),
            listOf(item()),
            MaterialSummaryContext("Carlos López", null),
        )

        assertTrue(text.contains("Compra a cargo del cliente."))
        assertTrue(text.contains("interruptor diferencial"))
        assertFalse(text.contains("Precio estimado"))
    }

    @Test
    fun `includes optional prices only when enabled`() {
        val text = MaterialListSummaryGenerator.generate(
            materialList(),
            listOf(item()),
            MaterialSummaryContext("Carlos López", "PRES-2026-0001"),
            includePrices = true,
        )

        assertTrue(text.contains("Presupuesto relacionado: PRES-2026-0001"))
        assertTrue(text.contains("Precio estimado"))
    }

    private fun materialList(): MaterialList {
        val now = Instant.parse("2026-08-04T12:00:00Z")
        return MaterialList(
            id = "list",
            clientId = "client",
            visitId = null,
            inspectionId = null,
            quoteId = null,
            title = "Adecuación de tablero",
            status = MaterialListStatus.READY,
            purchaseResponsibility = PurchaseResponsibility.CLIENT,
            introduction = null,
            notes = null,
            createdAt = now,
            updatedAt = now,
            deliveredAt = null,
            isDeleted = false,
        )
    }

    private fun item(): MaterialItem {
        val now = Instant.parse("2026-08-04T12:00:00Z")
        return MaterialItem(
            id = "item",
            materialListId = "list",
            description = "interruptor diferencial bipolar 40 A / 30 mA",
            quantity = 1.0,
            unit = MaterialUnit.UNIT,
            customUnitLabel = null,
            specifications = null,
            preferredBrand = "Marca sugerida",
            alternativeAllowed = true,
            estimatedUnitPriceAmount = 45_000_00L,
            actualUnitPriceAmount = null,
            sortOrder = 0,
            notes = null,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }
}
