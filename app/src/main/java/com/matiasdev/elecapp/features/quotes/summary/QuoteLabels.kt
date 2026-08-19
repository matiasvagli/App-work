package com.matiasdev.elecapp.features.quotes.summary

import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.QuoteItemType
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.quotes.domain.QuoteUnit

fun QuoteStatus.label(): String = when (this) {
    QuoteStatus.DRAFT -> "Borrador"
    QuoteStatus.READY -> "Listo"
    QuoteStatus.SENT -> "Enviado"
    QuoteStatus.APPROVED -> "Aprobado"
    QuoteStatus.REJECTED -> "Rechazado"
    QuoteStatus.EXPIRED -> "Vencido"
    QuoteStatus.CANCELLED -> "Cancelado"
}

fun QuoteItemType.label(): String = when (this) {
    QuoteItemType.LABOR -> "Mano de obra"
    QuoteItemType.SERVICE -> "Servicio"
    QuoteItemType.MATERIAL -> "Material"
    QuoteItemType.OTHER -> "Otro"
}

fun QuoteUnit.label(custom: String? = null): String = when (this) {
    QuoteUnit.UNIT -> "unidad"
    QuoteUnit.HOUR -> "hora"
    QuoteUnit.METER -> "metro"
    QuoteUnit.DAY -> "día"
    QuoteUnit.FIXED -> "global"
    QuoteUnit.OTHER -> custom?.takeIf { it.isNotBlank() } ?: "otro"
}

fun DiscountType.label(): String = when (this) {
    DiscountType.NONE -> "Sin descuento"
    DiscountType.FIXED -> "Descuento fijo"
    DiscountType.PERCENTAGE -> "Descuento porcentual"
}
