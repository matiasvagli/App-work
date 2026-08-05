package com.matiasdev.elecapp.features.agenda.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalDayBoundsTest {
    @Test
    fun `calculates local day bounds`() {
        val bounds = localDayBounds(LocalDate.of(2026, 8, 4), ZoneId.of("America/Argentina/Buenos_Aires"))

        assertEquals("2026-08-04T03:00:00Z", bounds.startInclusive.toString())
        assertEquals("2026-08-05T03:00:00Z", bounds.endExclusive.toString())
    }
}
