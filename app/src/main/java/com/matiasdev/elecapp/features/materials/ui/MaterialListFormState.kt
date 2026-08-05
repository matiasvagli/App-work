package com.matiasdev.elecapp.features.materials.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility

data class MaterialListFormUiState(
    val listId: String? = null,
    val clientId: String? = null,
    val visitId: String? = null,
    val inspectionId: String? = null,
    val quoteId: String? = null,
    val clientQuery: String = "",
    val clients: List<Client> = emptyList(),
    val title: String = "",
    val purchaseResponsibility: PurchaseResponsibility = PurchaseResponsibility.TO_BE_DEFINED,
    val introduction: String = "",
    val items: List<MaterialItemFormState> = emptyList(),
    val notes: String = "",
    val savedListId: String? = null,
    val errorMessage: String? = null,
)

data class MaterialItemFormState(
    val id: String,
    val description: String,
    val quantity: String,
    val unit: MaterialUnit,
    val customUnitLabel: String,
    val specifications: String,
    val preferredBrand: String,
    val alternativeAllowed: Boolean,
    val includePrices: Boolean,
    val estimatedUnitPriceInput: String,
    val actualUnitPriceInput: String,
    val notes: String,
)

enum class MaterialSaveMode {
    DRAFT,
    READY,
}

data class MaterialTemplate(
    val title: String,
    val unit: MaterialUnit,
)

val MaterialTemplates = listOf(
    MaterialTemplate("Interruptor diferencial", MaterialUnit.UNIT),
    MaterialTemplate("Térmica bipolar", MaterialUnit.UNIT),
    MaterialTemplate("Cable por metro", MaterialUnit.METER),
    MaterialTemplate("Caja estanca", MaterialUnit.UNIT),
    MaterialTemplate("Tomacorriente", MaterialUnit.UNIT),
    MaterialTemplate("Llave de luz", MaterialUnit.UNIT),
    MaterialTemplate("Caño corrugado", MaterialUnit.METER),
    MaterialTemplate("Canaleta", MaterialUnit.METER),
    MaterialTemplate("Prensaestopa", MaterialUnit.UNIT),
    MaterialTemplate("Bornera", MaterialUnit.UNIT),
    MaterialTemplate("Terminales", MaterialUnit.PACK),
    MaterialTemplate("Elementos de fijación", MaterialUnit.PACK),
)
