package com.matiasdev.elecapp.features.finance.ui

import com.matiasdev.elecapp.core.time.TimeProvider
import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.finance.data.FakeFinanceRepository
import com.matiasdev.elecapp.features.finance.domain.PaymentMethod
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import com.matiasdev.elecapp.features.visits.ui.FakeVisitRepository
import com.matiasdev.elecapp.features.visits.ui.FakeVisitWorkSessionRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitCloseViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val timeProvider = TimeProvider { now }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `cash close creates full payment for total labor plus materials`() = runTest(dispatcher) {
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance)

        completeRequiredFields(viewModel)
        viewModel.updateMoney(VisitCloseMoneyField.LABOR, "56.000")
        viewModel.updateMoney(VisitCloseMoneyField.MATERIALS, "12.000")
        viewModel.selectPaymentMethod(ClosePaymentMethod.CASH)
        viewModel.save()

        val draft = requireNotNull(finance.lastCloseDraft)
        assertEquals(68_000_00L, draft.initialPayments.single().amountCents)
        assertEquals(PaymentMethod.CASH, draft.initialPayments.single().method)
        assertEquals(2, draft.items.size)
        assertEquals(0L, draft.discountCents)
    }

    @Test
    fun `transfer and mercado pago create complete payments with references`() = runTest(dispatcher) {
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance)
        completeRequiredFields(viewModel)
        viewModel.updateMoney(VisitCloseMoneyField.LABOR, "100.000")
        viewModel.selectPaymentMethod(ClosePaymentMethod.BANK_TRANSFER)
        viewModel.updateText(VisitCloseTextField.TRANSFER_REFERENCE, "TRX-1")
        viewModel.save()

        assertEquals(PaymentMethod.BANK_TRANSFER, finance.lastCloseDraft?.initialPayments?.single()?.method)
        assertEquals("TRX-1", finance.lastCloseDraft?.initialPayments?.single()?.reference)

        val financeMp = FakeFinanceRepository()
        val mpViewModel = viewModel(financeMp)
        completeRequiredFields(mpViewModel)
        mpViewModel.updateMoney(VisitCloseMoneyField.LABOR, "100.000")
        mpViewModel.selectPaymentMethod(ClosePaymentMethod.MERCADO_PAGO)
        mpViewModel.updateText(VisitCloseTextField.MERCADO_PAGO_REFERENCE, "MP-1")
        mpViewModel.save()

        assertEquals(PaymentMethod.MERCADO_PAGO, financeMp.lastCloseDraft?.initialPayments?.single()?.method)
        assertEquals("MP-1", financeMp.lastCloseDraft?.initialPayments?.single()?.reference)
    }

    @Test
    fun `mixed payment requires exact distribution and creates multiple payments`() = runTest(dispatcher) {
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance)
        completeRequiredFields(viewModel)
        viewModel.updateMoney(VisitCloseMoneyField.LABOR, "100.000")
        viewModel.selectPaymentMethod(ClosePaymentMethod.MIXED)
        viewModel.updateMoney(VisitCloseMoneyField.MIXED_CASH, "30.000")
        viewModel.updateMoney(VisitCloseMoneyField.MIXED_TRANSFER, "70.000")
        viewModel.updateText(VisitCloseTextField.TRANSFER_REFERENCE, "TRX-2")
        viewModel.save()

        val payments = requireNotNull(finance.lastCloseDraft).initialPayments
        assertEquals(2, payments.size)
        assertEquals(100_000_00L, payments.sumOf { it.amountCents })
        assertTrue(payments.any { it.method == PaymentMethod.CASH && it.amountCents == 30_000_00L })
        assertTrue(payments.any { it.method == PaymentMethod.BANK_TRANSFER && it.reference == "TRX-2" })
    }

    @Test
    fun `mixed payment lower or higher than total is blocked`() = runTest(dispatcher) {
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance)
        completeRequiredFields(viewModel)
        viewModel.updateMoney(VisitCloseMoneyField.LABOR, "100.000")
        viewModel.selectPaymentMethod(ClosePaymentMethod.MIXED)
        viewModel.updateMoney(VisitCloseMoneyField.MIXED_CASH, "70.000")
        viewModel.save()

        assertEquals(0, finance.closeCallCount)
        assertTrue(viewModel.uiState.value.validationErrors.isNotEmpty())

        viewModel.updateMoney(VisitCloseMoneyField.MIXED_CASH, "110.000")
        viewModel.save()

        assertEquals(0, finance.closeCallCount)
    }

    @Test
    fun `follow up visit is saved linked to current visit and included in receipt notes`() = runTest(dispatcher) {
        val visitRepository = FakeVisitRepository(listOf(visit()))
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance, visitRepository)
        completeRequiredFields(viewModel)
        viewModel.updateMoney(VisitCloseMoneyField.LABOR, "50.000")
        viewModel.updateFollowUp { it.copy(date = "12/08/2026", time = "14:30", reason = "Revisión del circuito del patio") }
        viewModel.saveFollowUp()
        viewModel.save()

        val followUp = visitRepository.currentVisits().first { it.parentVisitId == "visit" }
        assertEquals("client", followUp.clientId)
        assertEquals("Revisión del circuito del patio", followUp.reason)
        assertTrue(requireNotNull(finance.lastCloseDraft?.customerNotes).contains("Próxima visita agendada"))
    }

    @Test
    fun `no charge close creates receipt without payments and avoids duplicate save`() = runTest(dispatcher) {
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance)
        completeRequiredFields(viewModel)
        viewModel.requestNoCharge()
        viewModel.confirmNoCharge()
        viewModel.save()
        viewModel.save()

        assertEquals(1, finance.closeCallCount)
        assertEquals(0L, viewModel.uiState.value.totalCents)
        assertTrue(requireNotNull(finance.lastCloseDraft).initialPayments.isEmpty())
    }

    private fun completeRequiredFields(viewModel: VisitCloseViewModel) {
        viewModel.updateText(VisitCloseTextField.WORK, "Cambio de térmica y revisión de tablero")
    }

    private fun viewModel(
        financeRepository: FakeFinanceRepository,
        visitRepository: FakeVisitRepository = FakeVisitRepository(listOf(visit())),
    ): VisitCloseViewModel {
        return VisitCloseViewModel(
            clientRepository = FakeClientRepository(listOf(client)),
            visitRepository = visitRepository,
            workSessionRepository = FakeVisitWorkSessionRepository(listOf(session()), visitRepository, timeProvider),
            financeRepository = financeRepository,
            visitId = "visit",
            timeProvider = timeProvider,
            ioDispatcher = dispatcher,
        )
    }

    private fun visit(): Visit {
        return Visit("visit", "client", now.minusSeconds(3_600), 120, "Urgencia", null, VisitStatus.IN_PROGRESS, now, now, false, startedAt = now.minusSeconds(3_600))
    }

    private fun session(): VisitWorkSession {
        return VisitWorkSession("session", "visit", now.minusSeconds(3_600), null, VisitWorkSessionStatus.RUNNING, null, now, now, false)
    }

    private companion object {
        val now: Instant = Instant.parse("2026-08-05T14:20:00Z")
        val client = Client("client", "Carlos López", "111", null, "Av. Espora 1234", "Adrogué", null, now, now, false)
    }
}
