package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.UnverifiedItemType
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

    @Test
    fun `visual inspection completes without pillar panel or measurements`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val visit = testVisit()
        val inspection = repository.startOrGetInspection(visit, testClient(), InspectionScope.VISUAL_INSPECTION)
        val viewModel = InspectionOverviewViewModel(repository, FakeVisitRepository(listOf(visit)), inspection.id, dispatcher)

        viewModel.requestComplete()

        assertTrue(viewModel.uiState.value.showCompleteConfirmation)
        assertEquals(emptyList<String>(), viewModel.uiState.value.completionMissingItems)
    }

    @Test
    fun `visual overview exposes visual complementary step for existing inspection`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val visit = testVisit()
        val inspection = repository.startOrGetInspection(visit, testClient(), InspectionScope.VISUAL_INSPECTION)
        val viewModel = InspectionOverviewViewModel(repository, FakeVisitRepository(listOf(visit)), inspection.id, dispatcher)

        val sections = viewModel.uiState.value.progress?.sections.orEmpty().map { it.section }

        assertTrue(sections.contains(InspectionSection.VISUAL_COMPLEMENTARY))
        assertFalse(sections.contains(InspectionSection.UNVERIFIED))
    }

    @Test
    fun `visual pillar not applicable does not create unverified item or finding`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient(), InspectionScope.VISUAL_INSPECTION)
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(reviewStatus = InspectionSectionReviewStatus.NOT_APPLICABLE, addToUnverified = true) }
        viewModel.save()

        val aggregate = repository.findAggregate(inspection.id)
        assertEquals(InspectionSectionReviewStatus.NOT_APPLICABLE, aggregate?.pillar?.reviewStatus)
        assertEquals(emptyList<UnverifiedItemType>(), aggregate?.unverifiedItems?.map { it.type })
        assertEquals(emptyList<Any>(), aggregate?.findings)
    }

    @Test
    fun `visual pillar not verified can be added to unverified items`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient(), InspectionScope.VISUAL_INSPECTION)
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(reviewStatus = InspectionSectionReviewStatus.NOT_VERIFIED, addToUnverified = true) }
        viewModel.save()

        assertEquals(
            listOf(UnverifiedItemType.PILLAR_NOT_ACCESSIBLE),
            repository.findAggregate(inspection.id)?.unverifiedItems?.map { it.type },
        )
    }

    @Test
    fun `visual basic data survives saving and reopening the section`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient(), InspectionScope.VISUAL_INSPECTION)
        InspectionGeneralViewModel(repository, inspection.id, dispatcher).apply {
            onReviewReasonChange("Urgencia en patio")
            onReviewedElementChange("Reflector exterior")
            onTaskDescriptionChange("Se revisó el reflector que no enciende.")
            save()
        }

        val reopened = InspectionGeneralViewModel(repository, inspection.id, dispatcher)

        assertEquals("Urgencia en patio", reopened.uiState.value.reviewReason)
        assertEquals("Reflector exterior", reopened.uiState.value.reviewedElement)
        assertEquals("Se revisó el reflector que no enciende.", reopened.uiState.value.taskDescription)
    }

    @Test
    fun `visual complementary saves observation and selected unverified items`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient(), InspectionScope.VISUAL_INSPECTION)
        val viewModel = VisualInspectionComplementaryViewModel(repository, inspection.id, dispatcher)

        viewModel.onObservationChange("Se observó únicamente el sector indicado.")
        viewModel.toggle(UnverifiedItemType.NO_MEASUREMENTS)
        viewModel.onDescriptionChange(UnverifiedItemType.NO_MEASUREMENTS, "No era necesario para la urgencia.")
        viewModel.save()

        val aggregate = repository.findAggregate(inspection.id)
        assertEquals("Se observó únicamente el sector indicado.", aggregate?.inspection?.originalTechnicalComment)
        assertEquals(listOf(UnverifiedItemType.NO_MEASUREMENTS), aggregate?.unverifiedItems?.map { it.type })
    }
}
