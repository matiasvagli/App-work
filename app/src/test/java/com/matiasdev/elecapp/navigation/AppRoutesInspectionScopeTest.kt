package com.matiasdev.elecapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesInspectionScopeTest {
    @Test
    fun `visit inspection scope route keeps visit id only`() {
        assertEquals("visits/visit-1/inspection-scope", AppRoutes.visitInspectionScope("visit-1"))
    }

    @Test
    fun `visual complementary route keeps inspection id only`() {
        assertEquals("inspections/inspection-1/visual-complementary", AppRoutes.inspectionVisualComplementary("inspection-1"))
    }
}
