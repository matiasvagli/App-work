package com.matiasdev.elecapp.core.external

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.domain.Visit
import java.time.Duration

fun normalizePhoneForWhatsApp(phone: String): String? {
    val trimmed = phone.trim()
    if (trimmed.isBlank()) return null
    val hasPlus = trimmed.startsWith("+")
    val digits = trimmed.filter(Char::isDigit)
    if (digits.isBlank()) return null
    return if (hasPlus) digits else digits
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
