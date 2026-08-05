package com.matiasdev.elecapp.features.materials.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.materials.domain.MaterialItem
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.MaterialUnit
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import java.time.Instant

@Preview(showBackground = true)
@Composable
private fun MaterialDetailDraftPreview() {
    MaterialPreview(MaterialListStatus.DRAFT)
}

@Preview(showBackground = true)
@Composable
private fun MaterialDetailReadyPreview() {
    MaterialPreview(MaterialListStatus.READY)
}

@Preview(showBackground = true)
@Composable
private fun MaterialDetailDeliveredPreview() {
    MaterialPreview(MaterialListStatus.DELIVERED)
}

@Preview(showBackground = true)
@Composable
private fun MaterialDetailPurchasedPreview() {
    MaterialPreview(MaterialListStatus.PURCHASED)
}

@Preview(showBackground = true)
@Composable
private fun MaterialDetailCancelledPreview() {
    MaterialPreview(MaterialListStatus.CANCELLED)
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun MaterialDetailSmallWidthPreview() {
    MaterialPreview(MaterialListStatus.READY, longList = true)
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MaterialDetailDarkPreview() {
    MaterialPreview(MaterialListStatus.DELIVERED)
}

@Composable
private fun MaterialPreview(status: MaterialListStatus, longList: Boolean = false) {
    ElecAppTheme {
        MaterialListDetailContent(
            uiState = MaterialListDetailUiState(
                list = sampleList(status),
                items = if (longList) List(10) { sampleItem("$it") } else listOf(sampleItem("1"), sampleItem("2")),
                client = sampleClient(),
                includePrices = false,
                shareText = "Lista de materiales",
            ),
            onIncludePricesChange = {},
            onEditClick = {},
            onPrimaryTransition = {},
            onCancel = {},
            onCopy = {},
            onShare = {},
            onMaterialsListClick = {},
        )
    }
}

private fun sampleList(status: MaterialListStatus) = MaterialList(
    id = "list",
    clientId = "client",
    visitId = "visit",
    inspectionId = null,
    quoteId = null,
    title = "Materiales tablero principal",
    status = status,
    purchaseResponsibility = PurchaseResponsibility.TECHNICIAN,
    introduction = null,
    notes = null,
    createdAt = Instant.parse("2026-08-05T12:00:00Z"),
    updatedAt = Instant.parse("2026-08-05T12:00:00Z"),
    deliveredAt = null,
    isDeleted = false,
)

private fun sampleItem(suffix: String) = MaterialItem(
    id = "item-$suffix",
    materialListId = "list",
    description = "Interruptor termomagnético bipolar $suffix",
    quantity = 2.0,
    unit = MaterialUnit.UNIT,
    customUnitLabel = null,
    specifications = "Curva C, capacidad según verificación en obra",
    preferredBrand = "Marca de referencia",
    alternativeAllowed = true,
    estimatedUnitPriceAmount = 120000,
    actualUnitPriceAmount = null,
    sortOrder = suffix.toIntOrNull() ?: 0,
    notes = null,
    createdAt = Instant.parse("2026-08-05T12:00:00Z"),
    updatedAt = Instant.parse("2026-08-05T12:00:00Z"),
    isDeleted = false,
)

private fun sampleClient() = Client(
    id = "client",
    fullName = "Carlos López",
    phone = "1111",
    email = null,
    address = "Av. Siempre Viva 123",
    locality = "Córdoba",
    notes = null,
    createdAt = Instant.parse("2026-08-05T12:00:00Z"),
    updatedAt = Instant.parse("2026-08-05T12:00:00Z"),
    isDeleted = false,
)
