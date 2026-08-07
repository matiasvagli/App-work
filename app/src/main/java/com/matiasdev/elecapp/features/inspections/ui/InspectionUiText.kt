package com.matiasdev.elecapp.features.inspections.ui

import com.matiasdev.elecapp.features.inspections.domain.FindingSeverity
import com.matiasdev.elecapp.features.inspections.domain.InspectionSectionStatus

fun InspectionSectionStatus.symbol(): String = when (this) {
    InspectionSectionStatus.NOT_STARTED -> "○"
    InspectionSectionStatus.INCOMPLETE -> "◐"
    InspectionSectionStatus.COMPLETE -> "✓"
}

fun FindingSeverity.symbol(): String = when (this) {
    FindingSeverity.OK -> "✓"
    FindingSeverity.RECOMMENDED -> "!"
    FindingSeverity.PRIORITY -> "!!"
    FindingSeverity.URGENT -> "!!"
}
