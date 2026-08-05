package com.matiasdev.elecapp.features.inspections.summary

import com.matiasdev.elecapp.features.inspections.domain.completeAggregate
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InspectionSummaryGeneratorTest {
    @Test
    fun `generates deterministic structured summary in Spanish`() {
        val summary = InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC"))

        assertTrue(summary.contains("VISITA TÉCNICA"))
        assertTrue(summary.contains("Cliente: Carlos López"))
        assertTrue(summary.contains("Fecha: 04/08/2026"))
        assertTrue(summary.contains("[URGENTE] Conductores deteriorados"))
        assertTrue(summary.contains("Estos elementos no fueron verificados durante la visita."))
        assertEquals(summary, InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC")))
    }

    @Test
    fun `omits empty fields and does not invent values`() {
        val summary = InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC"))

        assertFalse(summary.contains("Corriente diferencial nominal"))
        assertFalse(summary.contains("Cantidad de circuitos"))
        assertTrue(summary.contains("No constituye una certificación integral"))
    }

    @Test
    fun `preserves original technical comment exactly`() {
        val summary = InspectionSummaryGenerator.generate(completeAggregate(), testVisit(), ZoneId.of("UTC"))

        assertTrue(summary.contains("COMENTARIO ORIGINAL DEL ELECTRICISTA\nComentario técnico exacto."))
    }
}
