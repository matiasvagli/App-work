package com.matiasdev.elecapp.features.materials.summary

import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility

fun MaterialListStatus.label(): String = when (this) {
    MaterialListStatus.DRAFT -> "Borrador"
    MaterialListStatus.READY -> "Lista preparada"
    MaterialListStatus.DELIVERED -> "Entregada"
    MaterialListStatus.PURCHASED -> "Comprada"
    MaterialListStatus.CANCELLED -> "Cancelada"
}

fun PurchaseResponsibility.label(): String = when (this) {
    PurchaseResponsibility.CLIENT -> "Compra a cargo del cliente"
    PurchaseResponsibility.TECHNICIAN -> "Compra a cargo del electricista"
    PurchaseResponsibility.TO_BE_DEFINED -> "Responsable de compra a definir"
}

fun MaterialUnit.label(custom: String? = null): String = when (this) {
    MaterialUnit.UNIT -> "unidad"
    MaterialUnit.METER -> "metro"
    MaterialUnit.ROLL -> "rollo"
    MaterialUnit.BOX -> "caja"
    MaterialUnit.PACK -> "paquete"
    MaterialUnit.OTHER -> custom?.takeIf { it.isNotBlank() } ?: "otro"
}
