package com.matiasdev.elecapp.core.external

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract

data class ImportedContact(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
)

fun readImportedContact(contentResolver: ContentResolver, contactUri: Uri): Result<ImportedContact> {
    return runCatching {
        val contact = contentResolver.query(
            contactUri,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use ImportedContact()
            val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)).orEmpty()
            val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
            ImportedContact(
                fullName = name,
                phone = if (hasPhone) readFirstPhone(contentResolver, id).orEmpty() else "",
                email = readFirstEmail(contentResolver, id).orEmpty(),
            )
        }
        contact ?: ImportedContact()
    }
}

private fun readFirstPhone(contentResolver: ContentResolver, contactId: String): String? {
    return contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
        arrayOf(contactId),
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
        } else {
            null
        }
    }
}

private fun readFirstEmail(contentResolver: ContentResolver, contactId: String): String? {
    return contentResolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
        arrayOf(contactId),
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
        } else {
            null
        }
    }
}
