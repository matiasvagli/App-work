package com.matiasdev.elecapp.features.visits.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.matiasdev.elecapp.core.ui.theme.ElecAppTheme
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.inspections.domain.ElectricalInspection
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionType
import com.matiasdev.elecapp.features.inspections.domain.PropertyType
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.materials.domain.MaterialList
import com.matiasdev.elecapp.features.materials.domain.MaterialListStatus
import com.matiasdev.elecapp.features.materials.domain.PurchaseResponsibility
import com.matiasdev.elecapp.features.quotes.domain.DiscountType
import com.matiasdev.elecapp.features.quotes.domain.Quote
import com.matiasdev.elecapp.features.quotes.domain.QuoteCurrency
import com.matiasdev.elecapp.features.quotes.domain.QuoteStatus
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionDurations
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant

@Preview(name = "Pendiente", showBackground = true)
@Composable
private fun VisitPendingPreview() {
    PreviewContent(VisitStatus.PENDING)
}

@Preview(name = "Confirmada", showBackground = true)
@Composable
private fun VisitConfirmedPreview() {
    PreviewContent(VisitStatus.CONFIRMED, withDocuments = true)
}

@Preview(name = "En curso", showBackground = true)
@Composable
private fun VisitRunningPreview() {
    PreviewContent(VisitStatus.IN_PROGRESS, running = true, withDocuments = true)
}

@Preview(name = "Pausada 320dp", widthDp = 320, showBackground = true)
@Composable
private fun VisitPausedSmallPreview() {
    PreviewContent(VisitStatus.IN_PROGRESS, running = false, withDocuments = true)
}

@Preview(name = "Finalizada", showBackground = true)
@Composable
private fun VisitCompletedPreview() {
    PreviewContent(VisitStatus.COMPLETED, withDocuments = true)
}

@Preview(name = "Cancelada oscura", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun VisitCancelledDarkPreview() {
    PreviewContent(VisitStatus.CANCELLED)
}

@Composable
private fun PreviewContent(status: VisitStatus, running: Boolean = false, withDocuments: Boolean = false) {
    val visit = fakeVisit(status)
    val sessions = fakeSessions(running)
    ElecAppTheme {
        VisitDetailContent(
            uiState = VisitDetailUiState(
                isLoading = false,
                visit = visit,
                client = fakeClient,
                inspection = if (withDocuments) fakeInspection else null,
                quote = if (withDocuments) fakeQuote else null,
                materialList = if (withDocuments) fakeMaterialList else null,
                sessions = sessions,
                now = now,
                workSummary = VisitWorkSessionDurations.summarize(visit, sessions, now),
            ),
            onInspectionClick = {},
            onQuoteClick = {},
            onMaterialClick = {},
            onWorkClick = {},
            onStartVisitClick = {},
            onPauseWorkClick = {},
            onResumeWorkClick = {},
            onCompleteVisitClick = {},
            onReceiptClick = {},
            onRegisterPaymentClick = { _, _, _ -> },
            onEditSessionNotesClick = {},
        )
    }
}

private fun fakeVisit(status: VisitStatus): Visit {
    return Visit(
        id = "visit",
        clientId = "client",
        scheduledAt = now.minusSeconds(3600),
        estimatedDurationMinutes = 120,
        reason = "Revisión de tablero principal",
        notes = "Cliente indica cortes intermitentes.",
        status = status,
        createdAt = now.minusSeconds(7200),
        updatedAt = now,
        isDeleted = false,
        startedAt = if (status in listOf(VisitStatus.IN_PROGRESS, VisitStatus.COMPLETED)) now.minusSeconds(5400) else null,
        completedAt = if (status == VisitStatus.COMPLETED) now else null,
        completionNotes = if (status == VisitStatus.COMPLETED) "Se revisó tablero y conexiones principales." else null,
        pendingWorkNotes = if (status == VisitStatus.COMPLETED) "Queda presupuestar reemplazo de térmica." else null,
    )
}

private fun fakeSessions(running: Boolean): List<VisitWorkSession> {
    val first = session("s1", now.minusSeconds(5400), now.minusSeconds(3000), VisitWorkSessionStatus.PAUSED)
    val second = session("s2", now.minusSeconds(1800), if (running) null else now.minusSeconds(600), if (running) VisitWorkSessionStatus.RUNNING else VisitWorkSessionStatus.PAUSED)
    return listOf(first, second)
}

private fun session(id: String, startedAt: Instant, endedAt: Instant?, status: VisitWorkSessionStatus): VisitWorkSession {
    return VisitWorkSession(id, "visit", startedAt, endedAt, status, "Nota de prueba", now, now, false)
}

private val now = Instant.parse("2026-08-05T15:30:00Z")
private val fakeClient = Client("client", "Carlos Lopez", "1122334455", "carlos@mail.com", "Av. Siempre Viva 123", "Lanus", null, now, now, false)
private val fakeInspection = ElectricalInspection(
    "inspection",
    "visit",
    InspectionStatus.DRAFT,
    InspectionScope.GENERAL_ASSESSMENT,
    InspectionType.VISUAL,
    GeneralCondition.FAIR,
    SupplyType.SINGLE_PHASE,
    PropertyType.HOUSE,
    "Revisión",
    null,
    null,
    "Revisión",
    "Carlos Lopez",
    "Av. Siempre Viva 123",
    "Lanus",
    null,
    null,
    null,
    null,
    now,
    null,
    now,
    now,
    false,
)
private val fakeQuote = Quote("quote", "client", "visit", null, "P-0001", "Reparación tablero", null, QuoteStatus.READY, QuoteCurrency.ARS, 1000, DiscountType.NONE, 0, 1000, null, null, null, null, null, null, null, now, now, false)
private val fakeMaterialList = MaterialList("materials", "client", "visit", null, null, "Materiales tablero", MaterialListStatus.READY, PurchaseResponsibility.TECHNICIAN, null, null, now, now, null, false)
