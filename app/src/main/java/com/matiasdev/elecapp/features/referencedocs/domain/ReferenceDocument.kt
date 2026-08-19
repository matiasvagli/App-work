package com.matiasdev.elecapp.features.referencedocs.domain

import java.time.Instant

/**
 * Un PDF de consulta que el técnico importó a la app.
 *
 * El archivo se copia a almacenamiento interno en vez de guardar la URI del selector: si
 * quedara apuntando a Descargas, limpiar el teléfono lo dejaría roto y recuperarlo exigiría
 * internet, justo lo que esta app no asume.
 */
data class ReferenceDocument(
    val id: String,
    val title: String,
    val fileName: String,
    val sourceUrl: String?,
    val sizeBytes: Long,
    val importedAt: Instant,
    val isDeleted: Boolean = false,
)
