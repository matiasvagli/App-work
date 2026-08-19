package com.matiasdev.elecapp.features.referencedocs.domain

private const val PDF_EXTENSION = ".pdf"
private const val FALLBACK_NAME = "documento"
private const val MAX_BASE_LENGTH = 80

/**
 * Deriva el nombre de archivo y el título a partir de lo que informa el selector del sistema.
 *
 * El nombre que devuelve el selector es texto arbitrario: puede traer separadores de ruta,
 * venir vacío o ser larguísimo. Como después se usa para armar un `File` y una URI de
 * FileProvider, se sanitiza antes de tocar el disco.
 */
object ReferenceDocumentNaming {
    /** Nombre seguro para guardar en disco. Siempre termina en `.pdf`. */
    fun fileName(displayName: String?): String {
        val base = displayName
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.removeSuffixIgnoringCase(PDF_EXTENSION)
            .orEmpty()
            .map { if (it.isAllowedInFileName()) it else '-' }
            .joinToString(separator = "")
            .collapseDashes()
            .trim('-', '.', ' ')
            .take(MAX_BASE_LENGTH)
            .ifBlank { FALLBACK_NAME }
        return base + PDF_EXTENSION
    }

    /** Título legible para la lista: el nombre original sin extensión ni separadores duros. */
    fun title(displayName: String?): String {
        val base = displayName
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.removeSuffixIgnoringCase(PDF_EXTENSION)
            ?.replace('_', ' ')
            ?.trim()
            .orEmpty()
        return base.ifBlank { "Documento sin nombre" }
    }

    private fun Char.isAllowedInFileName(): Boolean = isLetterOrDigit() || this == '.' || this == '-' || this == '_'

    private fun String.collapseDashes(): String = replace(Regex("-{2,}"), "-")

    private fun String.removeSuffixIgnoringCase(suffix: String): String {
        return if (endsWith(suffix, ignoreCase = true)) dropLast(suffix.length) else this
    }
}
