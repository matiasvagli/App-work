package com.matiasdev.elecapp.features.referencedocs.domain

/**
 * Origen sugerido desde donde descargar un documento.
 *
 * La app no baja el archivo: abre el sitio en el navegador y el técnico importa el PDF a mano.
 * No hay red en ElecApp y la lista publicada cambia de formato seguido, así que scrapearla
 * sería frágil y quedaría desactualizada en silencio.
 */
data class ReferenceDocumentSource(
    val title: String,
    val description: String,
    val url: String,
)

val AaiericLaborCostsSource = ReferenceDocumentSource(
    title = "Costos de mano de obra (AAIERIC)",
    description = "Valores sugeridos por encuesta a instaladores. Se actualizan mes a mes.",
    url = "https://aaieric.org.ar/costos-mano-de-obra",
)
