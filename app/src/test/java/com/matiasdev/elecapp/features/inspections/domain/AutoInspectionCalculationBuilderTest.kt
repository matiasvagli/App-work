package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoInspectionCalculationBuilderTest {
    @Test
    fun buildsProtectionCompatibilityFromPillarAndCircuits() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 25, conductorSectionMm2 = 2.5),
                circuits = listOf(circuit(breakerAmps = 10, conductorSectionMm2 = 1.5)),
            ),
        )

        assertTrue(calculations.any { it.id == "auto:pillar:protection" && it.classification == TechnicalClassification.CRITICAL_REVIEW })
        assertTrue(calculations.any { it.id.startsWith("auto:circuit:") && it.classification == TechnicalClassification.ACCEPTABLE })
    }

    @Test
    fun acceptsBreakerAtOrBelowConfiguredConductorMaximum() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                circuits = listOf(
                    circuit(breakerAmps = 10, conductorSectionMm2 = 2.5),
                    circuit(id = "circuit-2", breakerAmps = 16, conductorSectionMm2 = 2.5),
                    circuit(id = "circuit-3", breakerAmps = 25, conductorSectionMm2 = 4.0),
                ),
            ),
        ).filter { it.id.endsWith(":protection") }

        assertEquals(3, calculations.size)
        assertTrue(calculations.all { it.classification == TechnicalClassification.ACCEPTABLE })
    }

    @Test
    fun comparesMeasuredConsumptionAgainstCircuitBreaker() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(circuits = listOf(circuit(breakerAmps = 16, conductorSectionMm2 = 2.5, consumptionAmps = 18.0))),
        )

        val result = calculations.first { it.id.endsWith(":consumption-breaker") }
        assertEquals(TechnicalClassification.CRITICAL_REVIEW, result.classification)
    }

    @Test
    fun buildsMeasuredVoltageDropWhenPillarAndPanelVoltagesExist() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillarMeasurements = listOf(pillarVoltage(220.0)),
                panelMeasurements = listOf(panelVoltage(209.0)),
            ),
        )

        val drop = calculations.first { it.id == "auto:voltage-drop:measured" }
        assertEquals(TechnicalClassification.REQUIRES_REVIEW, drop.classification)
        assertTrue(drop.primaryResult.contains("5"))
    }

    @Test
    fun buildsBasicGroundingAssessmentFromPanelChecks() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                panel = panel(
                    groundBarPresent = YesNoUnknown.YES,
                    neutralAndGroundSeparated = YesNoUnknown.YES,
                    protectionConductorsPresent = YesNoPartialUnknown.YES,
                ),
                panelMeasurements = listOf(neutralGroundVoltage(1.0)),
            ),
        )

        val grounding = calculations.first { it.id == "auto:grounding:basic" }
        assertEquals(TechnicalClassification.ACCEPTABLE, grounding.classification)
    }

    private fun aggregate(
        inspection: ElectricalInspection = testInspection(),
        pillar: PillarInspection? = null,
        panel: MainPanelInspection? = panel(),
        circuits: List<MainPanelCircuit> = emptyList(),
        pillarMeasurements: List<PillarMeasurement> = emptyList(),
        panelMeasurements: List<MainPanelMeasurement> = emptyList(),
    ) = InspectionAggregate(
        inspection = inspection,
        pillar = pillar,
        mainPanel = panel,
        findings = emptyList(),
        unverifiedItems = emptyList(),
        pillarMeasurements = pillarMeasurements,
        mainPanelMeasurements = panelMeasurements,
        mainPanelCircuits = circuits,
    )

    private fun pillar(
        mainBreakerAmps: Int? = null,
        conductorSectionMm2: Double? = null,
    ) = PillarInspection(
        inspectionId = "inspection-1",
        reviewStatus = InspectionSectionReviewStatus.REVIEWED,
        exists = true,
        propertyType = PropertyType.HOUSE,
        propertyTypeOther = null,
        supplyType = SupplyType.SINGLE_PHASE,
        accessible = AccessStatus.YES,
        generalCondition = GeneralCondition.GOOD,
        mainBreakerPresent = YesNoUnknown.YES,
        mainBreakerAmps = mainBreakerAmps,
        mainBreakerOtherAmps = null,
        differentialPresent = YesNoUnknown.UNKNOWN,
        differentialRatedAmps = null,
        differentialOtherRatedAmps = null,
        differentialSensitivityMa = null,
        differentialOtherSensitivityMa = null,
        differentialTestResult = DifferentialTestResult.NOT_TESTED,
        conductorSectionMm2 = conductorSectionMm2,
        conductorOtherSectionMm2 = null,
        conductorMaterial = ConductorMaterial.COPPER,
        conductorMaterialOther = null,
        conductorCondition = ConductorCondition.GOOD,
        neutralIdentified = YesNoUnknown.YES,
        groundingVisible = YesNoUnknown.YES,
        protectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
        protectionCompatibilityNotes = null,
        notes = null,
        createdAt = now,
        updatedAt = now,
    )

    private fun panel(
        groundBarPresent: YesNoUnknown = YesNoUnknown.UNKNOWN,
        neutralAndGroundSeparated: YesNoUnknown = YesNoUnknown.UNKNOWN,
        protectionConductorsPresent: YesNoPartialUnknown = YesNoPartialUnknown.UNKNOWN,
    ) = MainPanelInspection(
        inspectionId = "inspection-1",
        reviewStatus = InspectionSectionReviewStatus.REVIEWED,
        accessible = AccessStatus.YES,
        generalCondition = GeneralCondition.GOOD,
        differentialPresent = YesNoUnknown.UNKNOWN,
        differentialRatedAmps = null,
        differentialOtherRatedAmps = null,
        differentialSensitivityMa = null,
        differentialOtherSensitivityMa = null,
        differentialTestResult = DifferentialTestResult.NOT_TESTED,
        circuitCount = null,
        circuitsIdentified = YesNoPartialUnknown.UNKNOWN,
        neutralBarPresent = YesNoUnknown.UNKNOWN,
        groundBarPresent = groundBarPresent,
        neutralAndGroundSeparated = neutralAndGroundSeparated,
        protectionConductorsPresent = protectionConductorsPresent,
        improvisedConnections = YesNoUnknown.UNKNOWN,
        conductorColorStatus = ConductorColorStatus.UNKNOWN,
        mixedOrIncorrectColors = YesNoUnknown.UNKNOWN,
        overheatingSigns = YesNoUnknown.UNKNOWN,
        exposedPartsOrDamagedInsulation = YesNoUnknown.UNKNOWN,
        protectionCompatibility = ProtectionCompatibility.NOT_ASSESSED,
        wiringRisksNotes = null,
        protectionConductorCheckResult = ProtectionConductorCheckResult.NOT_VERIFIED,
        feederDistanceMeters = null,
        feederConductorSectionMm2 = null,
        feederConductorMaterial = ConductorMaterial.UNKNOWN,
        feederDataOrigin = MeasurementOrigin.NOT_VERIFIED,
        notes = null,
        createdAt = now,
        updatedAt = now,
    )

    private fun circuit(
        id: String = "circuit-1",
        breakerAmps: Int?,
        conductorSectionMm2: Double?,
        consumptionAmps: Double? = null,
    ) = MainPanelCircuit(
        id = id,
        inspectionId = "inspection-1",
        sortOrder = 0,
        destination = CircuitDestination.LIGHTING,
        destinationOther = null,
        breakerAmps = breakerAmps,
        breakerOtherAmps = null,
        breakerCurve = BreakerCurve.C,
        conductorSectionMm2 = conductorSectionMm2,
        conductorOtherSectionMm2 = null,
        conductorMaterial = ConductorMaterial.COPPER,
        conductorMaterialOther = null,
        consumptionAmps = consumptionAmps,
        consumptionOrigin = if (consumptionAmps == null) MeasurementOrigin.NOT_VERIFIED else MeasurementOrigin.MEASURED,
        notes = null,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )

    private fun pillarVoltage(value: Double) = PillarMeasurement(
        id = "pillar-voltage",
        inspectionId = "inspection-1",
        type = PillarMeasurementType.SINGLE_PHASE_VOLTAGE_LN,
        value = value,
        unit = "V",
        origin = MeasurementOrigin.MEASURED,
        sortOrder = 0,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )

    private fun panelVoltage(value: Double) = MainPanelMeasurement(
        id = "panel-voltage",
        inspectionId = "inspection-1",
        section = MainPanelMeasurementSection.INPUT_VOLTAGE,
        type = MainPanelMeasurementType.INPUT_VOLTAGE_LN,
        value = value,
        unit = "V",
        origin = MeasurementOrigin.MEASURED,
        sortOrder = 0,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )

    private fun neutralGroundVoltage(value: Double) = MainPanelMeasurement(
        id = "neutral-ground",
        inspectionId = "inspection-1",
        section = MainPanelMeasurementSection.PROTECTION_CONDUCTOR_CHECK,
        type = MainPanelMeasurementType.PROTECTION_VOLTAGE_NEUTRAL_GROUND,
        value = value,
        unit = "V",
        origin = MeasurementOrigin.MEASURED,
        sortOrder = 0,
        createdAt = now,
        updatedAt = now,
        isDeleted = false,
    )

    private companion object {
        val now: Instant = Instant.parse("2026-08-04T14:00:00Z")
    }
}
