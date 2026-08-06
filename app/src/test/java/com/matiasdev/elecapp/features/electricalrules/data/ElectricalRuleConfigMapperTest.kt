package com.matiasdev.elecapp.features.electricalrules.data

import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleCode
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleConfig
import com.matiasdev.elecapp.features.electricalrules.domain.ElectricalRuleSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class ElectricalRuleConfigMapperTest {
    @Test
    fun `entity to domain and back keeps enum names and numeric values`() {
        val domain = ElectricalRuleConfig(
            code = ElectricalRuleCode.MAX_FEEDER_VOLTAGE_DROP_PERCENT,
            name = "Caída máxima entre pilar y tablero principal",
            enabled = true,
            severity = ElectricalRuleSeverity.IMPORTANT,
            numericValue = 3.0,
            secondaryNumericValue = null,
            unit = "%",
            findingTitle = "Caída de tensión entre pilar y tablero a revisar",
            findingDescriptionTemplate = "La caída de tensión supera el límite configurado de {limit} {unit}.",
            recommendationTemplate = "Verificar tensión en origen y destino.",
            configVersion = 1,
        )

        val entity = domain.toEntity()
        val mapped = entity.toDomain()

        assertEquals("MAX_FEEDER_VOLTAGE_DROP_PERCENT", entity.code)
        assertEquals("IMPORTANT", entity.severity)
        assertEquals(domain, mapped)
    }
}
