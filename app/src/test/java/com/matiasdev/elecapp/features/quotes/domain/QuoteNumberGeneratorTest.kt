package com.matiasdev.elecapp.features.quotes.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class QuoteNumberGeneratorTest {
    @Test
    fun `generates readable local quote number`() {
        val number = QuoteNumberGenerator.next(
            now = Instant.parse("2026-08-04T12:00:00Z"),
            existingForYear = 0,
            zoneId = ZoneId.of("America/Argentina/Buenos_Aires"),
        )

        assertEquals("PRES-2026-0001", number)
    }
}
