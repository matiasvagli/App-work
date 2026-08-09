package com.matiasdev.elecapp.features.inspections.summary

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientReportPromptGeneratorTest {

    private val technicalReport = """
        VISITA TÉCNICA

        Cliente: Pérez
        Fecha: 08/08/2026

        PILAR Y ACOMETIDA
        - Tensión fase-neutro: 201 V (medido)
    """.trimIndent()

    @Test
    fun `incluye el informe tecnico como fuente de datos`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertTrue(prompt.contains("Tensión fase-neutro: 201 V (medido)"))
        assertTrue(prompt.contains("=== INFORME TÉCNICO (fuente de datos) ==="))
    }

    @Test
    fun `prohibe inventar datos y afirmar cumplimiento normativo`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertTrue(prompt.contains("No inventes"))
        assertTrue(prompt.contains("No afirmes que la instalación cumple"))
    }

    @Test
    fun `prohibe interpretar que significa una medicion`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertTrue(prompt.contains("No interpretes qué significa una medición"))
    }

    @Test
    fun `pide texto plano sin markdown`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertTrue(prompt.contains("NADA de Markdown"))
    }

    @Test
    fun `pide el bloque de control con los valores utilizados`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertTrue(prompt.contains("VALORES UTILIZADOS"))
    }

    @Test
    fun `el propio prompt no usa markdown que la IA pueda imitar`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertFalse("el prompt no debe contener negritas markdown", prompt.contains("**"))
        assertFalse("el prompt no debe contener encabezados markdown", prompt.contains("\n#"))
    }

    @Test
    fun `no rompe si el informe tecnico llega vacio`() {
        val prompt = ClientReportPromptGenerator.generate("")
        assertTrue(prompt.contains("=== INFORME TÉCNICO (fuente de datos) ==="))
    }
}
