package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.testClient
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionsListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `filters inspections by status`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val draft = repository.startOrGetInspection(testVisit(), testClient())
        val completed = repository.startOrGetInspection(testVisit().copy(id = "visit-2"), testClient())
        repository.saveInspection(completed.copy(status = InspectionStatus.COMPLETED))
        val viewModel = InspectionsListViewModel(repository, dispatcher)

        assertEquals(listOf(draft.id), viewModel.uiState.value.inspections.map { it.inspection.id })

        viewModel.onFilterChange(InspectionListFilter.COMPLETED)
        assertEquals(listOf(completed.id), viewModel.uiState.value.inspections.map { it.inspection.id })
    }

    @Test
    fun `searches inspections by snapshot fields`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = InspectionsListViewModel(repository, dispatcher)

        viewModel.onQueryChange("Temperley")

        assertEquals(1, viewModel.uiState.value.inspections.size)
    }
}
