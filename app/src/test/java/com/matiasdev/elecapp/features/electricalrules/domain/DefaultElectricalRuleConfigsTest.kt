package com.matiasdev.elecapp.features.electricalrules.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultElectricalRuleConfigsTest {
    @Test
    fun `default rule codes are unique and complete`() {
        val defaults = DefaultElectricalRuleConfigs.all
        val codes = defaults.map { it.code }

        assertEquals(codes.toSet().size, codes.size)
        assertEquals(ElectricalRuleCode.entries.toSet(), codes.toSet())
    }
}
