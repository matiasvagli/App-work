package com.matiasdev.elecapp.features.referencedocs.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reference_documents",
    indices = [
        Index("imported_at"),
        Index("is_deleted"),
    ],
)
data class ReferenceDocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "source_url") val sourceUrl: String?,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "imported_at") val importedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean,
)
