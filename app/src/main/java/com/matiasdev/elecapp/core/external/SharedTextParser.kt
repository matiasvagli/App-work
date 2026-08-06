package com.matiasdev.elecapp.core.external

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

data class SharedClientDraft(
    val fullName: String = "",
    val phones: List<String> = emptyList(),
    val email: String = "",
    val notes: String = "",
)

fun extractPlainSharedText(intent: Intent?): String? {
    return extractPlainSharedText(
        action = intent?.action,
        type = intent?.type,
        text = intent?.getStringExtra(Intent.EXTRA_TEXT),
    )
}

fun extractPlainSharedText(action: String?, type: String?, text: String?): String? {
    if (action != Intent.ACTION_SEND) return null
    if (type != "text/plain") return null
    return text
        ?.takeIf { it.isNotBlank() }
}

fun readSharedClientDraft(contentResolver: ContentResolver, intent: Intent?): SharedClientDraft? {
    if (intent == null) return null
    val action = intent.action
    val type = intent.type
    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
    val streamUri = intent.streamUri()
    val dataUri = intent.data

    if (action == Intent.ACTION_SEND && (isVCardType(type) || streamUri?.looksLikeVCardFile() == true)) {
        val contact = when {
            streamUri != null -> readImportedVCard(contentResolver, streamUri).getOrNull()
            !text.isNullOrBlank() -> parseImportedVCard(text).getOrNull()
            else -> null
        } ?: return null
        return contact.toSharedClientDraft()
    }

    if (action == Intent.ACTION_VIEW && dataUri != null && (isVCardType(type) || dataUri.looksLikeVCardFile())) {
        val contact = readImportedVCard(contentResolver, dataUri).getOrNull() ?: return null
        return contact.toSharedClientDraft()
    }

    val plainText = extractPlainSharedText(action, type, text) ?: return null
    return SharedClientDraft(
        phones = suggestPhoneFromText(plainText)?.let(::normalizeImportedPhone)?.takeIf(String::isNotBlank)?.let(::listOf).orEmpty(),
        notes = plainText,
    )
}

fun suggestPhoneFromText(text: String): String? {
    val match = Regex("""\+?[\d][\d\s().-]{5,}\d""").find(text) ?: return null
    return match.value.trim()
}

private fun ImportedContact.toSharedClientDraft(): SharedClientDraft {
    return SharedClientDraft(
        fullName = fullName,
        phones = phones,
        email = email,
    )
}

private fun isVCardType(type: String?): Boolean {
    return type in setOf("text/x-vcard", "text/vcard", "text/directory")
}

private fun Uri.looksLikeVCardFile(): Boolean {
    val lastSegment = lastPathSegment.orEmpty().lowercase()
    return lastSegment.endsWith(".vcf") || toString().lowercase().contains(".vcf")
}

@Suppress("DEPRECATION")
private fun Intent.streamUri(): Uri? {
    return getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
}
