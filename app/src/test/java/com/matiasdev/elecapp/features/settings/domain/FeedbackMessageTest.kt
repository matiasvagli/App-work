package com.matiasdev.elecapp.features.settings.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class FeedbackMessageTest {
    private val context = FeedbackContext(
        appVersion = "0.1.0",
        androidRelease = "16",
        androidSdk = 36,
        deviceModel = "Google Pixel 7",
    )
    private val now: Instant = Instant.parse("2026-08-18T22:30:00Z")
    private val zone: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

    @Test
    fun `adjunta los datos necesarios para reproducir el problema`() {
        val text = FeedbackMessage.build("No me guarda el monto", context, now, zone)

        assertTrue(text.contains("No me guarda el monto"))
        assertTrue(text.contains("App: 0.1.0"))
        assertTrue(text.contains("Android: 16 (API 36)"))
        assertTrue(text.contains("Google Pixel 7"))
        assertTrue(text.contains("Fecha: 18/08/2026 19:30"))
    }

    @Test
    fun `un comentario en blanco no se puede enviar`() {
        assertFalse(FeedbackMessage.isSendable(""))
        assertFalse(FeedbackMessage.isSendable("   "))
        assertTrue(FeedbackMessage.isSendable("algo"))
    }

    @Test
    fun `el texto sobrevive a un comentario vacio sin romperse`() {
        val text = FeedbackMessage.build("   ", context, now, zone)

        assertTrue(text.contains("(sin comentario)"))
        assertTrue(text.contains("App: 0.1.0"))
    }
}
