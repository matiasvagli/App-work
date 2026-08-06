package com.matiasdev.elecapp.features.electricalrules.domain

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource

data class SupplyVoltageInput(
    val voltage: Double,
    val location: String?,
    val origin: CalculationSource,
    val sourceCalculationId: String?,
    val inspectionId: String?,
)
