package com.matiasdev.elecapp.features.finance.ui

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.finance.data.FakeFinanceRepository
import com.matiasdev.elecapp.features.finance.domain.VisitAttentionType
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickVisitViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `existing client and attention type allow starting without detail`() = runTest(dispatcher) {
        val finance = FakeFinanceRepository()
        val viewModel = viewModel(finance)

        viewModel.selectClient("client")
        viewModel.selectType(VisitAttentionType.REPAIR)
        assertTrue(viewModel.uiState.value.canStart)
        viewModel.start()

        val draft = requireNotNull(finance.lastQuickVisitDraft)
        assertEquals(VisitAttentionType.REPAIR, draft.attentionType)
        assertEquals("", draft.briefDetail)
    }

    @Test
    fun `other attention type requires brief detail`() = runTest(dispatcher) {
        val viewModel = viewModel(FakeFinanceRepository())

        viewModel.selectClient("client")
        viewModel.selectType(VisitAttentionType.OTHER)

        assertFalse(viewModel.uiState.value.canStart)
        viewModel.updateDraft { it.copy(briefDetail = "Ruido en tablero") }
        assertTrue(viewModel.uiState.value.canStart)
    }

    private fun viewModel(financeRepository: FakeFinanceRepository): QuickVisitViewModel {
        return QuickVisitViewModel(
            clientRepository = FakeClientRepository(listOf(client)),
            financeRepository = financeRepository,
            ioDispatcher = dispatcher,
        )
    }

    private companion object {
        val now: Instant = Instant.parse("2026-08-05T12:00:00Z")
        val client = Client("client", "Carlos López", "111", null, "Av. Espora 1234", "Adrogué", null, now, now, false)
    }
}
