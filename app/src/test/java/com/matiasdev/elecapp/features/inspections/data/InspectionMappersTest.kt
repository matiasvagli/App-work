package com.matiasdev.elecapp.features.inspections.data

import com.matiasdev.elecapp.features.inspections.domain.InspectionScope
import com.matiasdev.elecapp.features.inspections.domain.testInspection
import org.junit.Assert.assertEquals
import org.junit.Test

class InspectionMappersTest {
    @Test
    fun `inspection scope maps to entity and back`() {
        InspectionScope.entries.forEach { scope ->
            val inspection = testInspection().copy(scope = scope)

            val mapped = inspection.toEntity().toDomain()

            assertEquals(scope.name, inspection.toEntity().scope)
            assertEquals(scope, mapped.scope)
        }
    }
}
