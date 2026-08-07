package com.matiasdev.elecapp.core.external

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import java.io.BufferedReader

data class ImportedContact(
    val fullName: String = "",
    val phones: List<String> = emptyList(),
    val email: String = "",
) {
    val phone: String = phones.firstOrNull().orEmpty()
}

fun readImportedContact(contentResolver: ContentResolver, contactUri: Uri): Result<ImportedContact> {
    return runCatching {
        val resolvedUri = ContactsContract.Contacts.lookupContact(contentResolver, contactUri)
            ?: error("No se encontró el contacto seleccionado.")
        val summary = contentResolver.query(
            resolvedUri,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) error("No se pudo leer el contacto seleccionado.")
            ContactSummary(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)).orEmpty(),
            )
        } ?: error("No se pudo leer el contacto seleccionado.")
        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(summary.id.toString()),
            null,
        )?.use { cursor ->
            buildList {
                val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    normalizeImportedPhone(cursor.getString(numberIndex).orEmpty())
                        .takeIf(String::isNotBlank)
                        ?.let(::add)
                }
            }
        }.orEmpty().distinct()
        val email = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(summary.id.toString()),
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) ""
            else cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)).orEmpty()
        }.orEmpty()
        ImportedContact(
            fullName = summary.fullName,
            phones = phones,
            email = email,
        )
    }
}

fun readImportedVCard(contentResolver: ContentResolver, vcardUri: Uri): Result<ImportedContact> {
    return runCatching {
        val text = contentResolver.openInputStream(vcardUri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
        }.orEmpty()
        parseImportedVCard(text).getOrThrow()
    }
}

fun parseImportedVCard(text: String): Result<ImportedContact> {
    return runCatching {
        val lines = unfoldVCardLines(text)
        val fullName = lines.firstValue("FN")
            ?: lines.firstValue("N")?.split(';')?.filter(String::isNotBlank)?.joinToString(" ")
            ?: ""
        val phones = lines.values("TEL").map(::normalizeImportedPhone).filter(String::isNotBlank).distinct()
        if (fullName.isBlank() && phones.isEmpty()) error("La vCard no contiene nombre ni teléfono.")
        ImportedContact(
            fullName = fullName.trim(),
            phones = phones,
            email = lines.firstValue("EMAIL").orEmpty().trim(),
        )
    }
}

fun normalizeImportedPhone(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return ""
    val firstDigitIndex = trimmed.indexOfFirst(Char::isDigit)
    val hasInternationalPrefix = firstDigitIndex >= 0 && '+' in trimmed.take(firstDigitIndex)
    val digits = trimmed.filter(Char::isDigit)
    return when {
        digits.isBlank() -> ""
        hasInternationalPrefix -> "+$digits"
        else -> digits
    }
}

private data class ContactSummary(val id: Long, val fullName: String)

private fun unfoldVCardLines(text: String): List<String> {
    val result = mutableListOf<String>()
    text.replace("\r\n", "\n").replace('\r', '\n').lineSequence().forEach { line ->
        if ((line.startsWith(" ") || line.startsWith("\t")) && result.isNotEmpty()) {
            result[result.lastIndex] = result.last() + line.drop(1)
        } else {
            result += line
        }
    }
    return result
}

private fun List<String>.firstValue(property: String): String? {
    return values(property).firstOrNull()
}

private fun List<String>.values(property: String): List<String> {
    return mapNotNull { line ->
        val separatorIndex = line.indexOf(':').takeIf { it >= 0 } ?: return@mapNotNull null
        val name = line.take(separatorIndex).substringBefore(';')
        if (!name.equals(property, ignoreCase = true)) return@mapNotNull null
        decodeVCardValue(line.drop(separatorIndex + 1))
    }
}

private fun decodeVCardValue(value: String): String {
    return value
        .replace("\\n", " ", ignoreCase = true)
        .replace("\\,", ",")
        .replace("\\;", ";")
        .trim()
}
