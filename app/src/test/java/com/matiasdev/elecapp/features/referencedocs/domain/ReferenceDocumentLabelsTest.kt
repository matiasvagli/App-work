package com.matiasdev.elecapp.features.referencedocs.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class ReferenceDocumentLabelsTest {
    private val now: Instant = Instant.parse("2026-08-18T12:00:00Z")

    @Test
    fun `formatea el tamano en unidades legibles`() {
        assertEquals("617 kB", ReferenceDocumentLabels.size(617_218))
        assertEquals("980 B", ReferenceDocumentLabels.size(980))
        assertEquals("2,5 MB".replace(',', '.'), ReferenceDocumentLabels.size(2_500_000).replace(',', '.'))
    }

    @Test
    fun `describe la antiguedad en lenguaje natural`() {
        assertEquals("Importado hoy", ReferenceDocumentLabels.age(now, now))
        assertEquals("Importado ayer", ReferenceDocumentLabels.age(now.minus(Duration.ofDays(1)), now))
        assertEquals("Importado hace 5 días", ReferenceDocumentLabels.age(now.minus(Duration.ofDays(5)), now))
        assertEquals("Importado hace 3 meses", ReferenceDocumentLabels.age(now.minus(Duration.ofDays(100)), now))
    }

    @Test
    fun `marca como desactualizado recien a los 60 dias`() {
        assertFalse(ReferenceDocumentLabels.isStale(now.minus(Duration.ofDays(59)), now))
        assertTrue(ReferenceDocumentLabels.isStale(now.minus(Duration.ofDays(60)), now))
    }

    @Test
    fun `un reloj corrido hacia atras no produce antiguedad negativa`() {
        val importedInTheFuture = now.plus(Duration.ofDays(3))

        assertEquals("Importado hoy", ReferenceDocumentLabels.age(importedInTheFuture, now))
        assertFalse(ReferenceDocumentLabels.isStale(importedInTheFuture, now))
    }
}
