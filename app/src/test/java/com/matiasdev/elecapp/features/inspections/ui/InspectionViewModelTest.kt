package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown
import com.matiasdev.elecapp.features.inspections.domain.testClient
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import com.matiasdev.elecapp.features.visits.ui.FakeVisitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InspectionViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @Test
    fun `overview warns but allows completion when inspection is incomplete`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val visit = testVisit()
        val inspection = repository.startOrGetInspection(visit, testClient())
        val viewModel = InspectionOverviewViewModel(repository, FakeVisitRepository(listOf(visit)), inspection.id, dispatcher)

        viewModel.requestComplete()

        assertFalse(viewModel.uiState.value.showCompleteConfirmation)
        assertTrue(viewModel.uiState.value.completionMissingItems.isNotEmpty())

        viewModel.confirmComplete()
        assertEquals(InspectionStatus.COMPLETED, repository.findAggregate(inspection.id)?.inspection?.status)
    }

    @Test
    fun `overview completes and reopens inspection`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val visit = testVisit()
        val inspection = repository.startOrGetInspection(visit, testClient())
        PillarInspectionViewModel(repository, inspection.id, dispatcher).apply {
            update { copy(exists = true, generalCondition = GeneralCondition.GOOD) }
            save()
        }
        MainPanelInspectionViewModel(repository, inspection.id, dispatcher).apply {
            update { copy(accessible = AccessStatus.YES, generalCondition = GeneralCondition.GOOD) }
            save()
        }
        InspectionTextSectionViewModel(repository, inspection.id, InspectionTextSection.TECHNICAL_COMMENT, dispatcher).apply {
            onTextChange("Comentario")
            save()
        }
        val viewModel = InspectionOverviewViewModel(repository, FakeVisitRepository(listOf(visit)), inspection.id, dispatcher)

        viewModel.requestComplete()
        viewModel.confirmComplete()
        assertEquals(InspectionStatus.COMPLETED, repository.findAggregate(inspection.id)?.inspection?.status)

        viewModel.requestReopen()
        viewModel.confirmReopen()
        assertEquals(InspectionStatus.DRAFT, repository.findAggregate(inspection.id)?.inspection?.status)
    }

    @Test
    fun `pillar viewmodel validates positive values and saves section`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update {
            copy(
                exists = true,
                generalCondition = GeneralCondition.FAIR,
                mainBreakerPresent = YesNoUnknown.YES,
                mainBreakerAmps = "40",
                conductorSectionMm2 = "4.0",
                protectionCompatibility = ProtectionCompatibility.REQUIRES_VERIFICATION,
            )
        }
        viewModel.save()

        val pillar = repository.findAggregate(inspection.id)?.pillar
        assertEquals(40, pillar?.mainBreakerAmps)
        assertEquals(4.0, pillar?.conductorSectionMm2)
    }
}
