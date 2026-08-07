package com.matiasdev.elecapp.features.finance.domain

enum class VisitWorkType {
    REPAIR,
    INSTALLATION,
    REPLACEMENT,
    DIAGNOSIS,
    FAULT_FINDING,
    MAINTENANCE,
    TECHNICAL_INSPECTION,
    MODIFICATION,
    OTHER,
}

enum class VisitTechnicalResult {
    RESOLVED,
    PARTIALLY_RESOLVED,
    NOT_RESOLVED,
    DIAGNOSED,
    TECHNICAL_INSPECTION_COMPLETED,
    PENDING_CONTINUATION,
}

fun VisitWorkType.label(): String = when (this) {
    VisitWorkType.REPAIR -> "Reparación"
    VisitWorkType.INSTALLATION -> "Instalación"
    VisitWorkType.REPLACEMENT -> "Reemplazo"
    VisitWorkType.DIAGNOSIS -> "Diagnóstico"
    VisitWorkType.FAULT_FINDING -> "Búsqueda de falla"
    VisitWorkType.MAINTENANCE -> "Mantenimiento"
    VisitWorkType.TECHNICAL_INSPECTION -> "Relevamiento técnico"
    VisitWorkType.MODIFICATION -> "Modificación"
    VisitWorkType.OTHER -> "Otro"
}

fun VisitTechnicalResult.label(): String = when (this) {
    VisitTechnicalResult.RESOLVED -> "Resuelto"
    VisitTechnicalResult.PARTIALLY_RESOLVED -> "Parcialmente resuelto"
    VisitTechnicalResult.NOT_RESOLVED -> "No resuelto"
    VisitTechnicalResult.DIAGNOSED -> "Diagnosticado"
    VisitTechnicalResult.TECHNICAL_INSPECTION_COMPLETED -> "Relevamiento completado"
    VisitTechnicalResult.PENDING_CONTINUATION -> "Pendiente de continuación"
}
