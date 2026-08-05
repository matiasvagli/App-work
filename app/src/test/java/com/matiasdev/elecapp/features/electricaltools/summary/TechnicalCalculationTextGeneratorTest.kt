package com.matiasdev.elecapp.features.electricaltools.summary

import com.matiasdev.elecapp.features.electricaltools.calculators.VoltageDropCalculator
import com.matiasdev.elecapp.features.electricaltools.data.CalculationJson
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.CalculationContext
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalSystemType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalConductorMaterial
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import com.matiasdev.elecapp.features.electricaltools.domain.TemperatureMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropCurrentMode
import com.matiasdev.elecapp.features.electricaltools.domain.VoltageDropInput
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TechnicalCalculationTextGeneratorTest {
    @Test
    fun voltageDropTextDifferentiatesSourceAndAvoidsNormativeClaims() {
        val calculation = voltageDropCalculation()
        val text = TechnicalCalculationTextGenerator.generate(calculation)
        assertTrue(text.contains("Origen: Estimado"))
        assertTrue(text.contains("Clasificación orientativa: Revisión crítica"))
        assertTrue(text.contains("Supuestos:"))
        assertTrue(text.contains("Resultado orientativo"))
        assertFalse(text.contains("Cumple reglamentación"))
        assertFalse(text.contains("No cumple reglamentación"))
    }

    @Test
    fun suggestedFindingUsesEditableDraftValues() {
        val finding = SuggestedFindingFactory.fromCalculation(voltageDropCalculation())!!
        assertTrue(finding.title.contains("Caída de tensión estimada"))
        assertTrue(finding.description.contains("caída aproximada"))
        assertTrue(finding.recommendation!!.contains("Verificar la sección"))
    }

    private fun voltageDropCalculation(): TechnicalCalculation {
        val input = VoltageDropInput(
            systemType = ElectricalSystemType.AC_SINGLE_PHASE,
            nominalVoltageVolts = 220.0,
            currentMode = VoltageDropCurrentMode.DIRECT_CURRENT,
            currentAmps = 20.45,
            activePowerWatts = null,
            powerFactor = null,
            efficiency = null,
            conductorLengthMeters = 50.0,
            conductorSectionMm2 = 2.5,
            conductorMaterial = TechnicalConductorMaterial.COPPER,
            temperatureMode = TemperatureMode.NOT_CONSIDERED,
            conductorTemperatureCelsius = null,
            source = CalculationSource.ESTIMATED,
            context = CalculationContext(assumptions = "Longitud declarada por el cliente"),
        )
        val result = VoltageDropCalculator.calculate(input).value!!
        return TechnicalCalculation(
            id = "calc",
            type = TechnicalCalculationType.VOLTAGE_DROP,
            source = CalculationSource.ESTIMATED,
            clientId = "client",
            visitId = "visit",
            inspectionId = "inspection",
            title = "Caída de tensión",
            description = null,
            inputDataJson = CalculationJson.encode(input),
            resultDataJson = CalculationJson.encode(result),
            primaryResultValue = result.voltageDropPercent,
            primaryResultUnit = "%",
            classification = TechnicalClassification.CRITICAL_REVIEW,
            technicianConclusion = TechnicianConclusion.NOT_REVIEWED,
            technicianNotes = "Revisar en tablero",
            formulaVersion = result.formulaVersion,
            createdAt = Instant.parse("2026-08-05T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-05T12:00:00Z"),
            isDeleted = false,
        )
    }
}
