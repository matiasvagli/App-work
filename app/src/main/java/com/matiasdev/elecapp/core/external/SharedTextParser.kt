package com.matiasdev.elecapp.core.external

import android.content.Intent

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

fun suggestPhoneFromText(text: String): String? {
    val match = Regex("""\+?[\d][\d\s().-]{5,}\d""").find(text) ?: return null
    return match.value.trim()
}
