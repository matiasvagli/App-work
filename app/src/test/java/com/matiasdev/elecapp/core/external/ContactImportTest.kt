package com.matiasdev.elecapp.core.external

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactImportTest {
    @Test
    fun `parses vCard with name and phone`() {
        val result = parseImportedVCard(
            """
            BEGIN:VCARD
            VERSION:3.0
            FN:Ana Garcia
            TEL;TYPE=CELL:+54 9 11 2233-4455
            END:VCARD
            """.trimIndent(),
        ).getOrThrow()

        assertEquals("Ana Garcia", result.fullName)
        assertEquals(listOf("+5491122334455"), result.phones)
    }

    @Test
    fun `parses vCard with multiple phones`() {
        val result = parseImportedVCard(
            """
            BEGIN:VCARD
            VERSION:3.0
            FN:Carlos Lopez
            TEL;TYPE=CELL:+54 9 11 2233-4455
            TEL;TYPE=WORK:(011) 5555-6666
            END:VCARD
            """.trimIndent(),
        ).getOrThrow()

        assertEquals("Carlos Lopez", result.fullName)
        assertEquals(listOf("+5491122334455", "01155556666"), result.phones)
    }

    @Test
    fun `returns failure for malformed vCard without usable contact data`() {
        val result = parseImportedVCard("BEGIN:VCARD\nVERSION:3.0\nEND:VCARD")

        assertTrue(result.isFailure)
    }

    @Test
    fun `normalizes phone keeping international prefix`() {
        assertEquals("+5491122334455", normalizeImportedPhone("+54 9 11 2233-4455"))
        assertEquals("01155556666", normalizeImportedPhone("(011) 5555-6666"))
    }
}
