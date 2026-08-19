package com.matiasdev.elecapp.features.referencedocs.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceDocumentNamingTest {
    @Test
    fun `conserva el nombre publicado sin la extension duplicada`() {
        val fileName = ReferenceDocumentNaming.fileName("AAIERIC - Costos Julio 2026.pdf")

        assertEquals("AAIERIC-Costos-Julio-2026.pdf", fileName)
    }

    @Test
    fun `descarta separadores de ruta para no escribir fuera de la carpeta`() {
        val fileName = ReferenceDocumentNaming.fileName("../../etc/passwd.pdf")

        assertEquals("passwd.pdf", fileName)
        assertTrue(!fileName.contains("/"))
        assertTrue(!fileName.contains(".."))
    }

    @Test
    fun `usa un nombre por defecto cuando el selector no informa ninguno`() {
        assertEquals("documento.pdf", ReferenceDocumentNaming.fileName(null))
        assertEquals("documento.pdf", ReferenceDocumentNaming.fileName("   "))
        assertEquals("documento.pdf", ReferenceDocumentNaming.fileName(".pdf"))
    }

    @Test
    fun `recorta nombres larguisimos pero mantiene la extension`() {
        val fileName = ReferenceDocumentNaming.fileName("a".repeat(300) + ".pdf")

        assertTrue(fileName.endsWith(".pdf"))
        assertEquals(80 + ".pdf".length, fileName.length)
    }

    @Test
    fun `el titulo queda legible sin extension`() {
        assertEquals(
            "AAIERIC - Costos Sugeridos de Mano de Obra - Julio 2026",
            ReferenceDocumentNaming.title("AAIERIC - Costos Sugeridos de Mano de Obra - Julio 2026.pdf"),
        )
        assertEquals("Documento sin nombre", ReferenceDocumentNaming.title(null))
    }
}
