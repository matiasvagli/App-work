package com.matiasdev.elecapp.features.electricaltools.ui

import com.matiasdev.elecapp.features.clients.ui.FakeClientRepository
import com.matiasdev.elecapp.features.clients.ui.MainDispatcherRule
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationFilters
import com.matiasdev.elecapp.features.electricaltools.data.TechnicalCalculationRepository
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.inspections.data.FakeInspectionRepository
import com.matiasdev.elecapp.features.inspections.domain.AccessStatus
import com.matiasdev.elecapp.features.inspections.domain.ConductorMaterial
import com.matiasdev.elecapp.features.inspections.domain.GeneralCondition
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionReviewStatus
import com.matiasdev.elecapp.features.inspections.domain.MainPanelCircuit
import com.matiasdev.elecapp.features.inspections.domain.MainPanelInspection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurement
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementSection
import com.matiasdev.elecapp.features.inspections.domain.MainPanelMeasurementType
import com.matiasdev.elecapp.features.inspections.domain.MeasurementOrigin
import com.matiasdev.elecapp.features.inspections.domain.SupplyType
import com.matiasdev.elecapp.features.inspections.domain.testInspection
import com.matiasdev.elecapp.features.inspections.domain.testVisit
import com.matiasdev.elecapp.features.visits.ui.FakeVisitRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VoltageDropViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `prefills voltage drop from inspection main feeder measurements`() = runTest(dispatcher) {
        val now = Instant.parse("2026-08-04T14:30:00Z")
        val inspection = testInspection().copy(supplyType = SupplyType.THREE_PHASE)
        val inspectionRepository = FakeInspectionRepository()
        inspectionRepository.saveInspection(inspection)
        inspectionRepository.saveMainPanel(
            MainPanelInspection(
                inspectionId = inspection.id,
                reviewStatus = InspectionSectionReviewStatus.REVIEWED,
                accessible = AccessStatus.YES,
                generalCondition = GeneralCondition.GOOD,
                differentialPresent = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                differentialRatedAmps = null,
                differentialOtherRatedAmps = null,
                differentialSensitivityMa = null,
                differentialOtherSensitivityMa = null,
                differentialTestResult = com.matiasdev.elecapp.features.inspections.domain.DifferentialTestResult.NOT_TESTED,
                circuitCount = 1,
                circuitsIdentified = com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown.UNKNOWN,
                neutralBarPresent = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                groundBarPresent = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                neutralAndGroundSeparated = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                protectionConductorsPresent = com.matiasdev.elecapp.features.inspections.domain.YesNoPartialUnknown.UNKNOWN,
                improvisedConnections = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                conductorColorStatus = com.matiasdev.elecapp.features.inspections.domain.ConductorColorStatus.UNKNOWN,
                mixedOrIncorrectColors = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                overheatingSigns = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                exposedPartsOrDamagedInsulation = com.matiasdev.elecapp.features.inspections.domain.YesNoUnknown.UNKNOWN,
                protectionCompatibility = com.matiasdev.elecapp.features.inspections.domain.ProtectionCompatibility.NOT_ASSESSED,
                wiringRisksNotes = null,
                protectionConductorCheckResult = com.matiasdev.elecapp.features.inspections.domain.ProtectionConductorCheckResult.NOT_VERIFIED,
                feederDistanceMeters = 12.0,
                feederConductorSectionMm2 = 2.5,
                feederConductorMaterial = ConductorMaterial.COPPER,
                feederDataOrigin = MeasurementOrigin.MEASURED,
                notes = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
        inspectionRepository.saveMainPanelMeasurement(
            MainPanelMeasurement(
                id = "panel-voltage",
                inspectionId = inspection.id,
                section = MainPanelMeasurementSection.INPUT_VOLTAGE,
                type = MainPanelMeasurementType.INPUT_VOLTAGE_LN,
                value = 192.0,
                unit = "V",
                origin = MeasurementOrigin.MEASURED,
                sortOrder = 0,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        )
        inspectionRepository.saveMainPanelCircuit(
            MainPanelCircuit(
                id = "circuit-1",
                inspectionId = inspection.id,
                sortOrder = 0,
                destination = com.matiasdev.elecapp.features.inspections.domain.CircuitDestination.LIGHTING,
                destinationOther = null,
                breakerAmps = 10,
                breakerOtherAmps = null,
                breakerCurve = com.matiasdev.elecapp.features.inspections.domain.BreakerCurve.UNKNOWN,
                conductorSectionMm2 = 1.5,
                conductorOtherSectionMm2 = null,
                conductorMaterial = ConductorMaterial.COPPER,
                conductorMaterialOther = null,
                consumptionAmps = 21.0,
                consumptionOrigin = MeasurementOrigin.MEASURED,
                notes = null,
                createdAt = now,
                updatedAt = now,
                isDeleted = false,
            ),
        )

        val viewModel = VoltageDropViewModel(
            repository = FakeTechnicalCalculationRepository(),
            clientRepository = FakeClientRepository(),
            visitRepository = FakeVisitRepository(listOf(testVisit())),
            inspectionRepository = inspectionRepository,
            initialClientId = null,
            initialVisitId = null,
            initialInspectionId = inspection.id,
            duplicateId = null,
            ioDispatcher = dispatcher,
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ElectricalSystemType.AC_THREE_PHASE, state.systemType)
        assertEquals("192", state.nominalVoltage)
        assertEquals("21", state.current)
        assertEquals("12", state.length)
        assertEquals("2.5", state.section)
        assertEquals(TechnicalConductorMaterial.COPPER, state.material)
        assertEquals(CalculationSource.MEASURED, state.source)
        assertEquals(inspection.visitId, state.association.visitId)
        assertEquals(inspection.id, state.association.inspectionId)
    }
}

private class FakeTechnicalCalculationRepository : TechnicalCalculationRepository {
    private val calculations = MutableStateFlow<List<TechnicalCalculation>>(emptyList())

    override fun observeAll(): Flow<List<TechnicalCalculation>> = calculations
    override fun observeById(id: String): Flow<TechnicalCalculation?> = calculations.map { rows -> rows.firstOrNull { it.id == id } }
    override fun observeByClient(clientId: String): Flow<List<TechnicalCalculation>> = calculations.map { rows -> rows.filter { it.clientId == clientId } }
    override fun observeByVisit(visitId: String): Flow<List<TechnicalCalculation>> = calculations.map { rows -> rows.filter { it.visitId == visitId } }
    override fun observeByInspection(inspectionId: String): Flow<List<TechnicalCalculation>> = calculations.map { rows -> rows.filter { it.inspectionId == inspectionId } }
    override fun observeByType(type: TechnicalCalculationType): Flow<List<TechnicalCalculation>> = calculations.map { rows -> rows.filter { it.type == type } }
    override fun observeBySource(source: CalculationSource): Flow<List<TechnicalCalculation>> = calculations.map { rows -> rows.filter { it.source == source } }
    override fun observeByClassification(classification: TechnicalClassification): Flow<List<TechnicalCalculation>> = calculations.map { rows -> rows.filter { it.classification == classification } }
    override fun search(filters: TechnicalCalculationFilters): Flow<List<TechnicalCalculation>> = calculations
    override suspend fun findById(id: String): TechnicalCalculation? = calculations.value.firstOrNull { it.id == id }
    override suspend fun save(calculation: TechnicalCalculation) {
        calculations.value = calculations.value.filterNot { it.id == calculation.id }.plus(calculation)
    }
    override suspend fun updateEditableFields(id: String, title: String, description: String?, technicianConclusion: TechnicianConclusion, technicianNotes: String?) = Unit
    override suspend fun associate(id: String, clientId: String?, visitId: String?, inspectionId: String?) = Unit
    override suspend fun unlinkInspection(id: String) = Unit
    override suspend fun softDelete(id: String) = Unit
}
