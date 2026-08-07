package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorCondition
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.InspectionSection
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.InspectionStatus
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.PillarMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
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
    fun `pillar not applicable hides dependent data and saves status`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update {
            copy(
                reviewStatus = InspectionSectionReviewStatus.NOT_APPLICABLE,
                mainBreakerPresent = YesNoUnknown.YES,
                mainBreakerAmps = "25",
            )
        }

        val pillar = repository.findAggregate(inspection.id)?.pillar
        assertEquals(InspectionSectionReviewStatus.NOT_APPLICABLE, pillar?.reviewStatus)
        assertEquals(null, pillar?.mainBreakerAmps)
    }

    @Test
    fun `pillar not verified saves status without dependent fields`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(reviewStatus = InspectionSectionReviewStatus.NOT_VERIFIED, accessible = AccessStatus.YES) }

        val pillar = repository.findAggregate(inspection.id)?.pillar
        assertEquals(InspectionSectionReviewStatus.NOT_VERIFIED, pillar?.reviewStatus)
        assertEquals(AccessStatus.UNKNOWN, pillar?.accessible)
    }

    @Test
    fun `monophase pillar can be saved without measurements`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(supplyType = SupplyType.SINGLE_PHASE, generalCondition = GeneralCondition.GOOD) }

        val aggregate = repository.findAggregate(inspection.id)
        assertEquals(SupplyType.SINGLE_PHASE, aggregate?.pillar?.supplyType)
        assertEquals(emptyList<Any>(), aggregate?.pillarMeasurements)
    }

    @Test
    fun `monophase pillar stores voltage and current measurements`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(supplyType = SupplyType.SINGLE_PHASE) }
        viewModel.updateMeasurementDraft(PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN, "221", MeasurementOrigin.MEASURED)
        viewModel.saveMeasurement()
        viewModel.updateMeasurementDraft(PillarMeasurementType.SINGLE_PHASE_CURRENT, "18,5", MeasurementOrigin.ESTIMATED)
        viewModel.saveMeasurement()

        val measurements = repository.findAggregate(inspection.id)?.pillarMeasurements.orEmpty()
        assertEquals(listOf(PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN, PillarMeasurementType.SINGLE_PHASE_CURRENT), measurements.map { it.type })
        assertEquals(listOf("V", "A"), measurements.map { it.unit })
    }

    @Test
    fun `three phase pillar stores only selected phase phase measurement`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(supplyType = SupplyType.THREE_PHASE) }
        viewModel.updateMeasurementDraft(PillarMeasurementType.VOLTAGE_L1_L2, "381", MeasurementOrigin.MEASURED)
        viewModel.saveMeasurement()

        assertEquals(listOf(PillarMeasurementType.VOLTAGE_L1_L2), repository.findAggregate(inspection.id)?.pillarMeasurements?.map { it.type })
    }

    @Test
    fun `three phase pillar stores six voltage measurements`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)
        val types = listOf(
            PillarMeasurementType.VOLTAGE_L1_N,
            PillarMeasurementType.VOLTAGE_L2_N,
            PillarMeasurementType.VOLTAGE_L3_N,
            PillarMeasurementType.VOLTAGE_L1_L2,
            PillarMeasurementType.VOLTAGE_L2_L3,
            PillarMeasurementType.VOLTAGE_L3_L1,
        )

        viewModel.update { copy(supplyType = SupplyType.THREE_PHASE) }
        types.forEachIndexed { index, type ->
            viewModel.updateMeasurementDraft(type, (220 + index).toString(), MeasurementOrigin.MEASURED)
            viewModel.saveMeasurement()
        }

        assertEquals(types, repository.findAggregate(inspection.id)?.pillarMeasurements?.map { it.type })
    }

    @Test
    fun `three phase pillar stores current measurements by phase`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)
        val types = listOf(PillarMeasurementType.CURRENT_L1, PillarMeasurementType.CURRENT_L2, PillarMeasurementType.CURRENT_L3, PillarMeasurementType.CURRENT_NEUTRAL)

        viewModel.update { copy(supplyType = SupplyType.THREE_PHASE) }
        types.forEachIndexed { index, type ->
            viewModel.updateMeasurementDraft(type, (10 + index).toString(), MeasurementOrigin.MEASURED)
            viewModel.saveMeasurement()
        }

        assertEquals(types, repository.findAggregate(inspection.id)?.pillarMeasurements?.map { it.type })
        assertTrue(repository.findAggregate(inspection.id)?.pillarMeasurements.orEmpty().all { it.unit == "A" })
    }

    @Test
    fun `pillar measurement can be edited and deleted individually`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.updateMeasurementDraft(PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN, "219", MeasurementOrigin.MEASURED)
        viewModel.saveMeasurement()
        val measurement = repository.findAggregate(inspection.id)?.pillarMeasurements?.single()!!
        viewModel.editMeasurement(measurement)
        viewModel.updateMeasurementDraft(value = "223")
        viewModel.saveMeasurement()
        assertEquals(223.0, repository.findAggregate(inspection.id)?.pillarMeasurements?.single()?.value)

        viewModel.deleteMeasurement(measurement.id)
        assertEquals(emptyList<Any>(), repository.findAggregate(inspection.id)?.pillarMeasurements)
    }

    @Test
    fun `pillar stores predefined and custom breaker values`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update { copy(mainBreakerPresent = YesNoUnknown.YES, mainBreakerAmps = "25") }
        assertEquals(25, repository.findAggregate(inspection.id)?.pillar?.mainBreakerAmps)

        viewModel.update { copy(mainBreakerPresent = YesNoUnknown.YES, mainBreakerAmps = OTHER_VALUE, mainBreakerOtherAmps = "70") }
        assertEquals(70, repository.findAggregate(inspection.id)?.pillar?.mainBreakerOtherAmps)
    }

    @Test
    fun `pillar differential dependent data is ignored when not present`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update {
            copy(
                differentialPresent = YesNoUnknown.NO,
                differentialRatedAmps = "40",
                differentialSensitivityMa = "30",
            )
        }

        val pillar = repository.findAggregate(inspection.id)?.pillar
        assertEquals(YesNoUnknown.NO, pillar?.differentialPresent)
        assertEquals(null, pillar?.differentialRatedAmps)
        assertEquals(null, pillar?.differentialSensitivityMa)
    }

    @Test
    fun `pillar differential stores rated current and sensitivity separately`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update {
            copy(
                differentialPresent = YesNoUnknown.YES,
                differentialRatedAmps = "40",
                differentialSensitivityMa = "30",
                differentialTestResult = DifferentialTestResult.PASSED,
            )
        }

        val pillar = repository.findAggregate(inspection.id)?.pillar
        assertEquals(40, pillar?.differentialRatedAmps)
        assertEquals(30, pillar?.differentialSensitivityMa)
    }

    @Test
    fun `pillar conductor and compatibility can remain not verified`() = runTest(dispatcher) {
        val repository = FakeInspectionRepository()
        val inspection = repository.startOrGetInspection(testVisit(), testClient())
        val viewModel = PillarInspectionViewModel(repository, inspection.id, dispatcher)

        viewModel.update {
            copy(
                conductorSectionMm2 = "",
                conductorMaterial = ConductorMaterial.UNKNOWN,
                conductorCondition = ConductorCondition.NOT_ASSESSED,
                protectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
            )
        }

        val pillar = repository.findAggregate(inspection.id)?.pillar
        assertEquals(null, pillar?.conductorSectionMm2)
        assertEquals(ConductorMaterial.UNKNOWN, pillar?.conductorMaterial)
        assertEquals(ProtectionCompatibility.NOT_ASSESSED, pillar?.protectionCompatibility)
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
