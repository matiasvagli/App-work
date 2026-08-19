package com.matiasdev.elecapp.features.referencedocs.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.matiasdev.elecapp.features.referencedocs.domain.ReferenceDocument
import java.io.File

private const val ROOT_DIRECTORY = "reference_docs"
private const val PDF_HEADER = "%PDF"

/**
 * Guarda los PDF importados en almacenamiento interno, uno por carpeta.
 *
 * Cada documento vive en `files/reference_docs/<id>/<nombre>.pdf`. La carpeta por id da
 * unicidad sin ensuciar el nombre del archivo, que es lo que después muestra el visor externo
 * como título.
 */
class ReferenceDocumentStorage(context: Context) {
    private val appContext = context.applicationContext
    private val authority = "${appContext.packageName}.fileprovider"

    fun displayName(uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        return appContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }

    /**
     * Copia el contenido de [uri] adentro de la app y devuelve el tamaño en bytes.
     *
     * Si algo falla a mitad de camino borra la carpeta: un PDF truncado abre roto en el visor
     * y es peor que no tenerlo.
     */
    fun copyIn(uri: Uri, documentId: String, fileName: String): Long {
        val target = fileFor(documentId, fileName)
        target.parentFile?.mkdirs()
        return runCatching {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: error("No se pudo leer el archivo seleccionado")
            target.length()
        }.onFailure { directoryFor(documentId).deleteRecursively() }.getOrThrow()
    }

    /** Chequea la firma del archivo; el selector filtra por MIME, pero el MIME lo declara el origen. */
    fun looksLikePdf(file: File): Boolean {
        if (!file.isFile) return false
        val header = ByteArray(PDF_HEADER.length)
        val read = file.inputStream().use { it.read(header) }
        return read == header.size && String(header) == PDF_HEADER
    }

    fun fileFor(document: ReferenceDocument): File = fileFor(document.id, document.fileName)

    fun fileFor(documentId: String, fileName: String): File = File(directoryFor(documentId), fileName)

    fun contentUriFor(document: ReferenceDocument): Uri {
        return FileProvider.getUriForFile(appContext, authority, fileFor(document))
    }

    fun delete(documentId: String) {
        directoryFor(documentId).deleteRecursively()
    }

    /** Usado por el borrado total: las filas se van con la base, los archivos hay que barrerlos aparte. */
    fun deleteAll() {
        File(appContext.filesDir, ROOT_DIRECTORY).deleteRecursively()
    }

    private fun directoryFor(documentId: String): File {
        return File(File(appContext.filesDir, ROOT_DIRECTORY), documentId)
    }
}
