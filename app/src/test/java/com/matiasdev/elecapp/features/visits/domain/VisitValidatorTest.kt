package com.matiasdev.elecapp.features.visits.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisitValidatorTest {
    private val now = Instant.parse("2026-01-01T10:00:00Z")

    @Test
    fun `requires reason`() {
        val result = VisitValidator.validate("", now.plusSeconds(3600), null, now)

        assertEquals("El motivo es obligatorio", result.reasonError)
    }

    @Test
    fun `rejects past scheduled date`() {
        val result = VisitValidator.validate("Revisión", now.minusSeconds(60), null, now)

        assertEquals("La visita no puede quedar en el pasado", result.dateTimeError)
    }

    @Test
    fun `accepts future visit`() {
        val result = VisitValidator.validate("Revisión", now.plusSeconds(3600), 45, now)

        assertTrue(result.isValid)
    }
}
