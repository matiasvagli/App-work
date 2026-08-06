package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.testClient
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import com.matiasdev.elecapp.features.visits.ui.FakeVisitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionScopeSelectionViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `selecting scope creates inspection and emits selected scope`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val viewModel = viewModel(repository)
        val eventDeferred = async { viewModel.events.first() as InspectionScopeSelectionEvent.InspectionReady }

        viewModel.onScopeSelected(InspectionScope.VISUAL_INSPECTION)
        val event = eventDeferred.await()

        val inspection = repository.findAggregate(event.inspectionId)?.inspection
        assertEquals(InspectionScope.VISUAL_INSPECTION, viewModel.uiState.value.selectedScope)
        assertEquals(InspectionScope.VISUAL_INSPECTION, inspection?.scope)
    }

    @Test
    fun `not selecting scope does not create empty inspection`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        viewModel(repository)

        assertNull(repository.findActiveInspectionForVisit("visit-1"))
    }

    private fun viewModel(repository: FakeInspectionRepository): InspectionScopeSelectionViewModel {
        return InspectionScopeSelectionViewModel(
            clientRepository = FakeClientRepository(listOf(testClient())),
            visitRepository = FakeVisitRepository(listOf(testVisit())),
            inspectionRepository = repository,
            visitId = "visit-1",
            ioDispatcher = dispatcher,
        )
    }
}
