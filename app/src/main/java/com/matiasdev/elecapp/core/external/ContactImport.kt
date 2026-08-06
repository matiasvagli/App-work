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
        val summary = contentResolver.query(
            contactUri,
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            ContactSummary(
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)).orEmpty(),
            )
        }
        val detail = readContactEntity(contentResolver, contactUri)
        ImportedContact(
            fullName = detail.fullName.ifBlank { summary?.fullName.orEmpty() },
            phones = detail.phones,
            email = detail.email,
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
    val hasInternationalPrefix = trimmed.firstOrNull { !it.isWhitespace() } == '+'
    val digits = trimmed.filter(Char::isDigit)
    return when {
        digits.isBlank() -> ""
        hasInternationalPrefix -> "+$digits"
        else -> digits
    }
}

private fun readContactEntity(contentResolver: ContentResolver, contactUri: Uri): ImportedContact {
    val entityUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Entity.CONTENT_DIRECTORY)
    return contentResolver.query(
        entityUri,
        arrayOf(
            ContactsContract.Contacts.Entity.DISPLAY_NAME,
            ContactsContract.Contacts.Entity.MIMETYPE,
            ContactsContract.Contacts.Entity.DATA1,
        ),
        null,
        null,
        null,
    )?.use { cursor ->
        var fullName = ""
        val phones = mutableListOf<String>()
        var email = ""
        while (cursor.moveToNext()) {
            if (fullName.isBlank()) {
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.Entity.DISPLAY_NAME)).orEmpty()
            }
            when (cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.Entity.MIMETYPE))) {
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                    val phone = normalizeImportedPhone(
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.Entity.DATA1)).orEmpty(),
                    )
                    if (phone.isNotBlank()) phones += phone
                }
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                    if (email.isBlank()) {
                        email = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.Entity.DATA1)).orEmpty()
                    }
                }
            }
        }
        ImportedContact(fullName = fullName, phones = phones.distinct(), email = email)
    } ?: ImportedContact()
}

private data class ContactSummary(val fullName: String)

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
