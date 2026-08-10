package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.electricalrules.domain.DefaultElectricalRuleConfigs
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La caída de tensión del alimentador se calculaba desde siempre, pero vivía sola en el
 * bloque de cálculos [AUTO] del informe: no era hallazgo, así que no llegaba a la lista de
 * acciones ni la contaba el overview. Estos tests fijan que sí lo sea y, sobre todo, que la
 * clasificación del cálculo y la existencia del hallazgo no puedan discrepar.
 */
class FeederVoltageDropFindingsTest {

    @Test
    fun `abre hallazgo cuando la caida medida supera el umbral configurado`() {
        val aggregate = aggregate(
            pillarMeasurements = listOf(pillarVoltage(210.0)),
            panelMeasurements = listOf(panelVoltage(193.0)),
        )

        val finding = aggregate.suggestedFinding("auto:rule:feeder_voltage_drop:measured")

        assertEquals(FindingSeverity.PRIORITY, finding.severity)
        assertEquals(FindingSourceType.RULE_SUGGESTION, finding.sourceType)
        assertTrue(finding.includeInReport)
        assertTrue(finding.description.contains("8,1 %"))
        assertTrue(finding.description.contains("máximo configurado (3 %)"))
        assertTrue(finding.description.contains("210 V"))
        assertTrue(finding.description.contains("193 V"))
        assertEquals(InspectionFindingRecommendations.FEEDER_VOLTAGE_DROP_ABOVE_LIMIT, finding.recommendation)
    }

    @Test
    fun `no abre hallazgo cuando la caida medida esta dentro del umbral`() {
        val aggregate = aggregate(
            pillarMeasurements = listOf(pillarVoltage(220.0)),
            panelMeasurements = listOf(panelVoltage(216.0)),
        )

        assertTrue(aggregate.findingIds().none { it.startsWith("auto:rule:feeder_voltage_drop") })
    }

    /**
     * El umbral es configurable: si el hallazgo mirara el default mientras el cálculo [AUTO]
     * mira la regla guardada, el mismo informe diría "aceptable" arriba y abriría un hallazgo
     * abajo. Esa contradicción es justamente lo que este caso evita.
     */
    @Test
    fun `respeta el umbral configurado por el tecnico en vez del default`() {
        val aggregate = aggregate(
            pillarMeasurements = listOf(pillarVoltage(220.0)),
            panelMeasurements = listOf(panelVoltage(211.0)),
        )
        val relaxed = rulesWithMaxDrop(6.0)

        val withDefault = aggregate.findingIds()
        val withRelaxedLimit = aggregate.findingIds(relaxed)

        assertTrue(withDefault.contains("auto:rule:feeder_voltage_drop:measured"))
        assertFalse(withRelaxedLimit.contains("auto:rule:feeder_voltage_drop:measured"))
        assertTrue(
            "el cálculo [AUTO] y el hallazgo tienen que clasificar igual",
            AutoInspectionCalculationBuilder.build(aggregate, relaxed)
                .first { it.id == "auto:voltage-drop:measured" }
                .primaryResult
                .contains("aceptable"),
        )
    }

    @Test
    fun `usa la caida estimada solo cuando no hay mediciones para confirmarla`() {
        val aggregate = aggregate(
            panel = panel(feederDistanceMeters = 40.0, feederConductorSectionMm2 = 2.5),
            pillarMeasurements = listOf(pillarCurrent(25.0)),
        )

        val finding = aggregate.suggestedFinding("auto:rule:feeder_voltage_drop:estimated")

        assertEquals(FindingSeverity.RECOMMENDED, finding.severity)
        assertTrue(finding.description.contains("No se registraron mediciones"))
        assertEquals(
            InspectionFindingRecommendations.FEEDER_VOLTAGE_DROP_ESTIMATED_ABOVE_LIMIT,
            finding.recommendation,
        )
    }

    @Test
    fun `con caida medida no duplica el hallazgo con la estimada`() {
        val aggregate = aggregate(
            panel = panel(feederDistanceMeters = 40.0, feederConductorSectionMm2 = 2.5),
            pillarMeasurements = listOf(pillarVoltage(210.0), pillarCurrent(25.0)),
            panelMeasurements = listOf(panelVoltage(193.0)),
        )

        val ids = aggregate.findingIds()

        assertTrue(ids.contains("auto:rule:feeder_voltage_drop:measured"))
        assertFalse(ids.contains("auto:rule:feeder_voltage_drop:estimated"))
    }

    /**
     * Caso del informe real que disparó el cambio: 12 m de 4 mm² con 25 A estiman ~1 %, pero
     * lo medido dio 8,1 %. La sección del cable no explica esa diferencia, así que es un
     * hallazgo aparte del de la caída excesiva y apunta al recorrido, no al calibre.
     */
    @Test
    fun `abre un hallazgo aparte cuando la caida medida es muy superior a la estimada`() {
        val aggregate = aggregate(
            panel = panel(feederDistanceMeters = 12.0, feederConductorSectionMm2 = 4.0),
            pillarMeasurements = listOf(pillarVoltage(210.0), pillarCurrent(25.0)),
            panelMeasurements = listOf(panelVoltage(193.0)),
        )

        val ids = aggregate.findingIds()
        val finding = aggregate.suggestedFinding("auto:rule:feeder_voltage_drop:review")

        assertTrue(ids.contains("auto:rule:feeder_voltage_drop:measured"))
        assertEquals(FindingSeverity.PRIORITY, finding.severity)
        assertTrue(finding.description.contains("muy superior a la estimada"))
        assertEquals(
            InspectionFindingRecommendations.FEEDER_VOLTAGE_DROP_HIGHER_THAN_ESTIMATED,
            finding.recommendation,
        )
    }

    @Test
    fun `el hallazgo no aparece sin tension medida en el tablero`() {
        val aggregate = aggregate(pillarMeasurements = listOf(pillarVoltage(210.0)))

        assertTrue(aggregate.findingIds().none { it.startsWith("auto:rule:feeder_voltage_drop") })
    }

    private fun InspectionAggregate.findingIds(
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): List<String> = InspectionFindingProposalBuilder.buildGroups(this, rules).suggested.map { it.id }

    private fun InspectionAggregate.suggestedFinding(
        id: String,
        rules: List<ElectricalRuleConfig> = DefaultElectricalRuleConfigs.all,
    ): InspectionFinding =
        InspectionFindingProposalBuilder.buildGroups(this, rules).suggested.first { it.id == id }

    private fun rulesWithMaxDrop(percent: Double): List<ElectricalRuleConfig> =
        DefaultElectricalRuleConfigs.all.map {
            if (it.code == ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT) {
                it.copy(numericValue = percent)
            } else {
                it
            }
        }

    private fun aggregate(
        panel: MainPanelInspection? = panel(),
        pillarMeasurements: List<PillarMeasurement> = emptyList(),
        panelMeasurements: List<MainPanelMeasurement> = emptyList(),
    ) = InspectionAggregate(
        inspection = testInspection(),
        pillar = null,
        mainPanel = panel,
        grounding = null,
        findings = emptyList(),
        unverifiedItems = emptyList(),
        pillarMeasurements = pillarMeasurements,
        mainPanelMeasurements = panelMeasurements,
        mainPanelCircuits = emptyList(),
    )

    private fun panel(
        feederDistanceMeters: Double? = null,
        feederConductorSectionMm2: Double? = null,
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
        groundBarPresent = YesNoUnknown.UNKNOWN,
        neutralAndGroundSeparated = YesNoUnknown.UNKNOWN,
        protectionConductorsPresent = YesNoPartialUnknown.UNKNOWN,
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
        feederConductorMaterial = ConductorMaterial.COPPER,
        feederDataOrigin = MeasurementOrigin.NOT_VERIFIED,
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

    private companion object {
        val now: Instant = Instant.parse("2026-08-04T14:00:00Z")
    }
}
