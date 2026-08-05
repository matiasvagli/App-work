package com.matiasdev.elecapp.core.external

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedTextParserTest {
    @Test
    fun `extracts text plain action send`() {
        assertEquals("Cliente desde WhatsApp", extractPlainSharedText("android.intent.action.SEND", "text/plain", "Cliente desde WhatsApp"))
    }

    @Test
    fun `ignores non text content`() {
        assertNull(extractPlainSharedText("android.intent.action.SEND", "image/png", "texto"))
    }

    @Test
    fun `suggests phone from shared text`() {
        assertEquals("+54 9 11 2233-4455", suggestPhoneFromText("Tel +54 9 11 2233-4455 gracias"))
    }
}
