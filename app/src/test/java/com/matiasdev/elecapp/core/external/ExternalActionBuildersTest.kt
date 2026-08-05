package com.matiasdev.elecapp.core.external

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExternalActionBuildersTest {
    @Test
    fun `normalizes whatsapp phone without inventing country code`() {
        assertEquals("5491122334455", normalizePhoneForWhatsApp("+54 9 (11) 2233-4455"))
        assertEquals("1122334455", normalizePhoneForWhatsApp("11 2233-4455"))
    }

    @Test
    fun `returns null when whatsapp phone has no digits`() {
        assertNull(normalizePhoneForWhatsApp("sin telefono"))
    }

    @Test
    fun `builds maps query from address and locality`() {
        assertEquals("Av Siempre Viva 742, Springfield", buildMapsQuery(" Av Siempre Viva 742 ", "Springfield"))
        assertEquals("Springfield", buildMapsQuery(null, "Springfield"))
    }
}
