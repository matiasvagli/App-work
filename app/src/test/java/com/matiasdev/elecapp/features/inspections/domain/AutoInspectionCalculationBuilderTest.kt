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
        assertEquals(TechnicalClassification.REQUIRES_REVIEW, result.classification)
        assertEquals("18 A sobre 16 A · requiere revisión", result.primaryResult)
        assertTrue(!result.detail.contains("sobrecarga", ignoreCase = true))
    }

    @Test
    fun usesReportCircuitNameForUnidentifiedCircuitCalculations() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                circuits = listOf(
                    circuit(id = "circuit-1", breakerAmps = 10, conductorSectionMm2 = 1.5),
                    circuit(
                        id = "circuit-2",
                        breakerAmps = 16,
                        conductorSectionMm2 = 2.5,
                        consumptionAmps = 18.0,
                        destination = CircuitDestination.UNIDENTIFIED,
                        sortOrder = 1,
                    ),
                ),
            ),
        )

        assertTrue(calculations.any { it.title == "Circuito 2 sin identificar: compatibilidad térmica-conductor" })
        assertTrue(calculations.any { it.title == "Circuito 2 sin identificar: consumo y térmica" })
        assertTrue(calculations.none { it.title.contains("unidentified") })
    }

    @Test
    fun usesProtectionLoadPercentageLabelsInAutomaticCalculationText() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                circuits = listOf(
                    circuit(id = "circuit-1", breakerAmps = 16, conductorSectionMm2 = 2.5, consumptionAmps = 12.0),
                    circuit(id = "circuit-2", breakerAmps = 10, conductorSectionMm2 = 2.5, consumptionAmps = 8.0, sortOrder = 1),
                    circuit(id = "circuit-3", breakerAmps = 10, conductorSectionMm2 = 2.5, consumptionAmps = 9.7, sortOrder = 2),
                    circuit(id = "circuit-4", breakerAmps = 10, conductorSectionMm2 = 2.5, consumptionAmps = 10.0, sortOrder = 3),
                ),
            ),
        )

        val acceptable = calculations.first { it.id == "auto:circuit:circuit-1:consumption-breaker" }
        val elevated = calculations.first { it.id == "auto:circuit:circuit-2:consumption-breaker" }
        val nearLimit = calculations.first { it.id == "auto:circuit:circuit-3:consumption-breaker" }
        val review = calculations.first { it.id == "auto:circuit:circuit-4:consumption-breaker" }

        assertEquals("12 A sobre 16 A · aceptable", acceptable.primaryResult)
        assertEquals("Carga medida equivalente al 75 % de la corriente nominal de la protección.", acceptable.detail)
        assertEquals("8 A sobre 10 A · carga elevada", elevated.primaryResult)
        assertEquals("9,7 A sobre 10 A · próximo al límite", nearLimit.primaryResult)
        assertEquals("10 A sobre 10 A · requiere revisión", review.primaryResult)
        assertTrue(review.detail.contains("alcanza la corriente nominal"))
        assertTrue(!review.detail.contains("sobrecarga", ignoreCase = true))
    }

    @Test
    fun usesNeutralProtectionConductorCompatibilityText() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 32, conductorSectionMm2 = 2.5),
                circuits = listOf(circuit(breakerAmps = 16, conductorSectionMm2 = 1.5)),
            ),
        )

        val pillar = calculations.first { it.id == "auto:pillar:protection" }
        val circuit = calculations.first { it.id == "auto:circuit:circuit-1:protection" }

        assertEquals("Pilar: compatibilidad térmica-conductor", pillar.title)
        assertEquals("crítico", pillar.primaryResult)
        assertTrue(pillar.detail.contains("requiere revisión"))
        assertTrue(pillar.detail.contains("2,5 mm² -> protección de referencia 16 A"))
        assertTrue(pillar.detail.contains("Para 32 A -> sección de referencia 6 mm²"))
        assertTrue(!pillar.detail.contains("bajar", ignoreCase = true))
        assertTrue(!pillar.detail.contains("cambiar térmica", ignoreCase = true))
        assertEquals("Circuito 1 (iluminación): compatibilidad térmica-conductor", circuit.title)
        assertTrue(circuit.detail.contains("1,5 mm² -> protección de referencia 10 A"))
    }

    @Test
    fun usesReportCircuitNameForUnidentifiedCircuitFindings() {
        val groups = InspectionFindingProposalBuilder.buildGroups(
            aggregate(
                circuits = listOf(
                    circuit(id = "circuit-1", breakerAmps = 10, conductorSectionMm2 = 1.5),
                    circuit(
                        id = "circuit-2",
                        breakerAmps = 16,
                        conductorSectionMm2 = 2.5,
                        consumptionAmps = 18.0,
                        destination = CircuitDestination.UNIDENTIFIED,
                        sortOrder = 1,
                    ),
                ),
            ),
        )

        assertTrue(groups.suggested.any { it.description.startsWith("Circuito 2 sin identificar:") })
        assertTrue(groups.suggested.none { it.description.contains("unidentified") })
    }

    @Test
    fun measuredVoltageDropUsesPillarVoltageAsReference() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 63, conductorSectionMm2 = 10.0),
                panel = panel(feederDistanceMeters = 80.0, feederConductorSectionMm2 = 1.5),
                pillarMeasurements = listOf(pillarVoltage(205.0), pillarCurrent(99.0)),
                panelMeasurements = listOf(panelVoltage(192.0)),
            ),
        )

        val drop = calculations.first { it.id == "auto:voltage-drop:measured" }
        assertEquals(TechnicalClassification.CRITICAL_REVIEW, drop.classification)
        assertTrue(drop.primaryResult.contains("6,34 %"))
        assertTrue(drop.detail.contains("diferencia 13 V"))
    }

    @Test
    fun measuredVoltageDropDoesNotUseCurrentSectionOrLength() {
        val base = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 16, conductorSectionMm2 = 2.5),
                panel = panel(feederDistanceMeters = 5.0, feederConductorSectionMm2 = 2.5),
                pillarMeasurements = listOf(pillarVoltage(220.0), pillarCurrent(8.0)),
                panelMeasurements = listOf(panelVoltage(200.0)),
            ),
        ).first { it.id == "auto:voltage-drop:measured" }

        val changedFeederData = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 63, conductorSectionMm2 = 10.0),
                panel = panel(feederDistanceMeters = 100.0, feederConductorSectionMm2 = 1.5),
                pillarMeasurements = listOf(pillarVoltage(220.0), pillarCurrent(70.0)),
                panelMeasurements = listOf(panelVoltage(200.0)),
            ),
        ).first { it.id == "auto:voltage-drop:measured" }

        assertTrue(base.primaryResult.contains("9,09 %"))
        assertEquals(base.primaryResult, changedFeederData.primaryResult)
        assertEquals(base.detail, changedFeederData.detail)
    }

    @Test
    fun estimatedVoltageDropUsesMeasuredCurrentAndNotBreakerRating() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 63, conductorSectionMm2 = 10.0),
                panel = panel(feederDistanceMeters = 5.0, feederConductorSectionMm2 = 2.5),
                pillarMeasurements = listOf(pillarVoltage(220.0), pillarCurrent(21.0)),
            ),
        )

        val drop = calculations.first { it.id == "auto:voltage-drop:estimated" }
        assertEquals(TechnicalClassification.ACCEPTABLE, drop.classification)
        assertTrue(drop.detail.contains("5 m / cobre / 2,5 mm² / 21 A medidos"))
    }

    @Test
    fun doesNotBuildEstimatedVoltageDropWhenCurrentIsMissing() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                pillar = pillar(mainBreakerAmps = 63, conductorSectionMm2 = 10.0),
                panel = panel(feederDistanceMeters = 5.0, feederConductorSectionMm2 = 2.5),
                pillarMeasurements = listOf(pillarVoltage(220.0)),
            ),
        )

        assertTrue(calculations.none { it.id == "auto:voltage-drop:estimated" })
    }

    @Test
    fun doesNotBuildMeasuredVoltageDropWhenEitherEndpointVoltageIsMissing() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                panel = panel(feederDistanceMeters = 5.0, feederConductorSectionMm2 = 2.5),
                pillarMeasurements = listOf(pillarVoltage(220.0), pillarCurrent(21.0)),
            ),
        )

        assertTrue(calculations.none { it.id == "auto:voltage-drop:measured" })
    }

    @Test
    fun comparesMeasuredDropAgainstEstimatedDropWithoutRecommendingConductorReplacement() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                panel = panel(feederDistanceMeters = 5.0, feederConductorSectionMm2 = 2.5),
                pillarMeasurements = listOf(pillarVoltage(205.0), pillarCurrent(21.0)),
                panelMeasurements = listOf(panelVoltage(192.0)),
            ),
        )

        val review = calculations.first { it.id == "auto:voltage-drop:feeder-review" }
        assertEquals(TechnicalClassification.REQUIRES_REVIEW, review.classification)
        assertTrue(review.detail.contains("Verificar conexiones, empalmes, bornes"))
        assertTrue(!review.detail.contains("cambiar"))
    }

    @Test
    fun doesNotAcceptGroundingWithoutResistanceMeasurement() {
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
        assertEquals(TechnicalClassification.NOT_CLASSIFIED, grounding.classification)
    }

    @Test
    fun acceptsGroundingWithCompleteChecksAndResistanceWithinConfiguredMaximum() {
        val calculations = AutoInspectionCalculationBuilder.build(
            aggregate(
                grounding = grounding(resistanceOhms = 18.0),
                panel = panel(
                    groundBarPresent = YesNoUnknown.YES,
                    neutralAndGroundSeparated = YesNoUnknown.YES,
                    protectionConductorsPresent = YesNoPartialUnknown.YES,
                ),
                panelMeasurements = listOf(neutralGroundVoltage(1.0)),
            ),
        )

        assertEquals(
            TechnicalClassification.ACCEPTABLE,
            calculations.first { it.id == "auto:grounding:basic" }.classification,
        )
    }

    private fun aggregate(
        inspection: ElectricalInspection = testInspection(),
        pillar: PillarInspection? = null,
        panel: MainPanelInspection? = panel(),
        grounding: GroundingInspection? = null,
        circuits: List<MainPanelCircuit> = emptyList(),
        pillarMeasurements: List<PillarMeasurement> = emptyList(),
        panelMeasurements: List<MainPanelMeasurement> = emptyList(),
    ) = InspectionAggregate(
        inspection = inspection,
        pillar = pillar,
        mainPanel = panel,
        grounding = grounding,
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
        feederDistanceMeters: Double? = null,
        feederConductorSectionMm2: Double? = null,
        feederConductorMaterial: ConductorMaterial = ConductorMaterial.COPPER,
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
        feederDistanceMeters = feederDistanceMeters,
        feederConductorSectionMm2 = feederConductorSectionMm2,
        feederConductorMaterial = feederConductorMaterial,
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
        destination: CircuitDestination = CircuitDestination.LIGHTING,
        sortOrder: Int = 0,
    ) = MainPanelCircuit(
        id = id,
        inspectionId = "inspection-1",
        sortOrder = sortOrder,
        destination = destination,
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

    private fun grounding(resistanceOhms: Double) = GroundingInspection(
        inspectionId = "inspection-1",
        electrodePresent = YesNoUnknown.YES,
        inspectionChamberAccessible = YesNoUnknown.YES,
        mainGroundConductorPresent = YesNoUnknown.YES,
        protectiveConductorContinuity = YesNoUnknown.YES,
        resistanceOhms = resistanceOhms,
        resistanceOrigin = MeasurementOrigin.MEASURED,
        notes = null,
        createdAt = now,
        updatedAt = now,
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

    private fun pillarCurrent(value: Double) = PillarMeasurement(
        id = "pillar-current",
        inspectionId = "inspection-1",
        type = PillarMeasurementType.SINGLE_PHASE_CURRENT,
        value = value,
        unit = "A",
        origin = MeasurementOrigin.MEASURED,
        sortOrder = 1,
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
