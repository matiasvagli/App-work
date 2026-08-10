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

    /**
     * Las instrucciones vienen envueltas a mano, así que una frase puede quedar partida en
     * dos líneas. Se compara contra el prompt con los saltos y la sangría colapsados.
     */
    private fun promptText(report: String = technicalReport): String =
        ClientReportPromptGenerator.generate(report).replace(Regex("\\s+"), " ")

    @Test
    fun `incluye el informe tecnico como fuente de datos`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertTrue(prompt.contains("Tensión fase-neutro: 201 V (medido)"))
        assertTrue(prompt.contains("=== INFORME TÉCNICO (fuente de datos) ==="))
    }

    @Test
    fun `prohibe inventar datos y afirmar cumplimiento normativo`() {
        val prompt = promptText()
        assertTrue(prompt.contains("No inventes"))
        assertTrue(prompt.contains("No afirmes que la instalación cumple"))
    }

    @Test
    fun `prohibe interpretar que significa una medicion`() {
        assertTrue(promptText().contains("No interpretes qué significa una medición"))
    }

    @Test
    fun `pide texto plano sin markdown`() {
        assertTrue(promptText().contains("NADA de Markdown"))
    }

    @Test
    fun `traduce las etiquetas de severidad del informe tecnico a niveles de accion`() {
        val prompt = promptText()
        assertTrue(prompt.contains("[URGENTE]"))
        assertTrue(prompt.contains("[PRIORITARIO]"))
        assertTrue(prompt.contains("[RECOMENDADO]"))
        assertTrue(prompt.contains("[INFORMATIVO]"))
        assertTrue(prompt.contains("HAY QUE RESOLVERLO AHORA (SEGURIDAD)"))
        assertTrue(prompt.contains("HAY QUE RESOLVERLO PRONTO"))
        assertTrue(prompt.contains("CONVIENE MEJORARLO"))
    }

    @Test
    fun `prohibe que la ia cambie el nivel que definio el tecnico`() {
        assertTrue(promptText().contains("No la cambies, no subas ni bajes nada de nivel"))
    }

    @Test
    fun `pide accion concreta ante una proteccion fallida o ausente`() {
        val prompt = promptText()
        assertTrue(prompt.contains("no se revisa: se reemplaza o se instala"))
        assertTrue(prompt.contains("la recomendación es reemplazarlo"))
    }

    @Test
    fun `pide el informe listo para copiar y pegar en un solo bloque`() {
        val prompt = promptText()
        assertTrue(prompt.contains("dentro de un bloque de código"))
        assertTrue(prompt.contains("lo copie de un toque"))
    }

    @Test
    fun `pide el bloque de control fuera del informe y solo con valores que sostienen un hallazgo`() {
        val prompt = promptText()
        assertTrue(prompt.contains("CONTROL PARA EL TÉCNICO (no forma parte del informe)"))
        assertTrue(prompt.contains("Los valores que no derivaron en ningún hallazgo no van acá"))
    }

    @Test
    fun `permite acotar el alcance de la medicion fase-tierra sin concluir que hay tierra`() {
        val prompt = promptText()
        assertTrue(prompt.contains("no confirma que la puesta a tierra sea efectiva"))
        assertTrue(prompt.contains("No afirmes que hay puesta a tierra ni que no la hay"))
    }

    @Test
    fun `pide el encabezado con los datos del cliente antes de la primera seccion`() {
        val prompt = promptText()
        assertTrue(prompt.contains("INFORME DE VISITA TÉCNICA"))
        assertTrue(prompt.contains("Cliente, Domicilio, Fecha y Motivo de la visita"))
    }

    @Test
    fun `pide redactar de nuevo el estado general en vez de copiar el comentario del tecnico`() {
        val prompt = promptText()
        assertTrue(prompt.contains("Esta sección se escribe de nuevo, no se copia"))
        assertTrue(prompt.contains("no lo pegues tal cual"))
    }

    @Test
    fun `obliga a reportar los calculos automaticos sin inventarles nivel`() {
        val prompt = promptText()
        assertTrue(prompt.contains("CÁLCULOS AUTOMÁTICOS [AUTO]"))
        assertTrue(prompt.contains("No lo omitas por no ser un hallazgo"))
        assertTrue(prompt.contains("HAY QUE VERIFICARLO ANTES DE DEFINIR LA CORRECCIÓN"))
        assertTrue(prompt.contains("Si un [AUTO] dio normal o correcto, no lo listes"))
    }

    @Test
    fun `el propio prompt no usa markdown que la IA pueda imitar`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertFalse("el prompt no debe contener negritas markdown", prompt.contains("**"))
        assertFalse("el prompt no debe contener encabezados markdown", prompt.contains("\n#"))
        assertFalse("el prompt no debe abrir bloques de código", prompt.contains("```"))
    }

    @Test
    fun `no deja sangria heredada del raw string en el texto que lee la IA`() {
        val prompt = ClientReportPromptGenerator.generate(technicalReport)
        assertFalse(prompt.contains("\n        Sos un asistente"))
        assertTrue(prompt.startsWith("Sos un asistente"))
    }

    @Test
    fun `no rompe si el informe tecnico llega vacio`() {
        val prompt = ClientReportPromptGenerator.generate("")
        assertTrue(prompt.contains("=== INFORME TÉCNICO (fuente de datos) ==="))
    }
}
