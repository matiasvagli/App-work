package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.inspections.domain.FindingCategory
import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.InspectionFinding
import com.matiasdev.elecapp.features.inspections.domain.InspectionUnverifiedItem
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
import com.matiasdev.elecapp.features.inspections.domain.testClient
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FakeInspectionRepositoryTest {
    @Test
    fun `start creates inspection with visit and client snapshots`() = kotlinx.coroutines.test.runTest {
        val repository = FakeInspectionRepository()

        val inspection = repository.startOrGetInspection(testVisit(), testClient())

        assertEquals("visit-1", inspection.visitId)
        assertEquals("Carlos López", inspection.clientNameSnapshot)
        assertEquals("Av. X 1234", inspection.addressSnapshot)
        assertEquals("Temperley", inspection.localitySnapshot)
        assertEquals("cortes frecuentes", inspection.visitReasonSnapshot)
    }

    @Test
    fun `start reuses existing active inspection for visit`() = kotlinx.coroutines.test.runTest {
        val repository = FakeInspectionRepository()

        val first = repository.startOrGetInspection(testVisit(), testClient())
        val second = repository.startOrGetInspection(testVisit(), testClient().copy(fullName = "Nombre editado"))

        assertEquals(first.id, second.id)
        assertNotEquals("Nombre editado", second.clientNameSnapshot)
    }

    @Test
    fun `findings are ordered and soft deleted`() = kotlinx.coroutines.test.runTest {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val now = Instant.parse("2026-08-04T12:00:00Z")
        val first = finding("1", inspection.id, 1, now)
        val second = finding("2", inspection.id, 0, now)

        repository.saveFinding(first)
        repository.saveFinding(second)
        assertEquals(listOf("2", "1"), repository.findAggregate(inspection.id)?.findings?.map { it.id })

        repository.softDeleteFinding("2")
        assertEquals(listOf("1"), repository.findAggregate(inspection.id)?.findings?.map { it.id })
    }

    @Test
    fun `unverified items replace active selection without physical delete contract`() = kotlinx.coroutines.test.runTest {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val now = Instant.parse("2026-08-04T12:00:00Z")

        repository.saveUnverifiedItems(
            inspection.id,
            listOf(unverified("1", inspection.id, UnverifiedItemType.HIDDEN_WIRING, now)),
        )
        repository.saveUnverifiedItems(
            inspection.id,
            listOf(unverified("2", inspection.id, UnverifiedItemType.INSULATION_RESISTANCE, now)),
        )

        val items = repository.findAggregate(inspection.id)?.unverifiedItems.orEmpty()
        assertEquals(listOf(UnverifiedItemType.INSULATION_RESISTANCE), items.map { it.type })
    }

    private fun finding(id: String, inspectionId: String, sortOrder: Int, now: Instant): InspectionFinding {
        return InspectionFinding(
            id = id,
            inspectionId = inspectionId,
            category = FindingCategory.GENERAL,
            severity = FindingSeverity.RECOMMENDED,
            title = "Hallazgo $id",
            description = "Descripción",
            recommendation = null,
            sortOrder = sortOrder,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
    }

    private fun unverified(
        id: String,
        inspectionId: String,
        type: UnverifiedItemType,
        now: Instant,
    ): InspectionUnverifiedItem {
        return InspectionUnverifiedItem(id, inspectionId, type, null, now, now, false)
    }
}
