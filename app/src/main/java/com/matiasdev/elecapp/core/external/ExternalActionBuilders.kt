package com.matiasdev.elecapp.core.external

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.Duration

fun normalizePhoneForWhatsApp(phone: String): String? {
    val digits = phone.trim().filter(Char::isDigit)
    return digits.ifBlank { null }
}

fun buildMapsQuery(address: String?, locality: String?): String? {
    return listOfNotNull(address?.trim()?.takeIf(String::isNotBlank), locality?.trim()?.takeIf(String::isNotBlank))
        .joinToString(", ")
        .takeIf(String::isNotBlank)
}

fun whatsappIntent(phone: String): Intent? {
    val normalizedPhone = normalizePhoneForWhatsApp(phone) ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalizedPhone"))
        .setPackage("com.whatsapp")
}

fun whatsappBusinessIntent(phone: String): Intent? {
    val normalizedPhone = normalizePhoneForWhatsApp(phone) ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalizedPhone"))
        .setPackage("com.whatsapp.w4b")
}

fun browserWhatsappIntent(phone: String): Intent? {
    val normalizedPhone = normalizePhoneForWhatsApp(phone) ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalizedPhone"))
}

fun dialIntent(phone: String): Intent {
    return Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))
}

fun emailIntent(email: String): Intent {
    return Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${Uri.encode(email)}"))
}

fun mapsIntent(address: String?, locality: String?): Intent? {
    val query = buildMapsQuery(address, locality) ?: return null
    return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
}

fun browserMapsIntent(address: String?, locality: String?): Intent? {
    val query = buildMapsQuery(address, locality) ?: return null
    return Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"),
    )
}

fun browserIntent(url: String): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse(url))
}

/** Sharesheet de texto plano, el único canal de salida de la app. */
fun shareTextIntent(text: String, chooserTitle: String): Intent {
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, text)
    return Intent.createChooser(send, chooserTitle)
}

/**
 * Abre un PDF propio con el visor que tenga el teléfono.
 *
 * La URI llega de un FileProvider, así que hay que ceder permiso de lectura explícito: la app
 * que recibe el intent no puede leer el almacenamiento interno de ElecApp por su cuenta.
 */
fun viewPdfIntent(uri: Uri): Intent {
    return Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/pdf")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun calendarInsertIntent(client: Client, visit: Visit): Intent {
    val duration = visit.estimatedDurationMinutes?.let { Duration.ofMinutes(it.toLong()) }
    val description = listOfNotNull(
        visit.reason,
        visit.notes?.takeIf(String::isNotBlank),
    ).joinToString("\n\n")

    return Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, "Visita eléctrica - ${client.fullName}")
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, visit.scheduledAt.toEpochMilli())
        .putExtra(
            CalendarContract.EXTRA_EVENT_END_TIME,
            visit.scheduledAt.plus(duration ?: Duration.ofMinutes(60)).toEpochMilli(),
        )
        .putExtra(CalendarContract.Events.EVENT_LOCATION, buildMapsQuery(client.address, client.locality).orEmpty())
        .putExtra(CalendarContract.Events.DESCRIPTION, description)
}
