package com.matiasdev.elecapp.features.inspections.domain

import com.matiasdev.elecapp.features.clients.domain.Client
import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import java.time.Instant

fun testClient(): Client = Client(
    id = "client-1",
    fullName = "Carlos López",
    phone = "111",
    email = null,
    address = "Av. X 1234",
    locality = "Temperley",
    notes = null,
    createdAt = Instant.parse("2026-08-01T10:00:00Z"),
    updatedAt = Instant.parse("2026-08-01T10:00:00Z"),
    isDeleted = false,
)

fun testVisit(): Visit = Visit(
    id = "visit-1",
    clientId = "client-1",
    scheduledAt = Instant.parse("2026-08-04T14:00:00Z"),
    estimatedDurationMinutes = 60,
    reason = "cortes frecuentes",
    notes = null,
    status = VisitStatus.COMPLETED,
    createdAt = Instant.parse("2026-08-01T10:00:00Z"),
    updatedAt = Instant.parse("2026-08-01T10:00:00Z"),
    isDeleted = false,
)

fun testInspection(): ElectricalInspection = ElectricalInspection(
    id = "inspection-1",
    visitId = "visit-1",
    status = InspectionStatus.DRAFT,
    scope = InspectionScope.GENERAL_ASSESSMENT,
    inspectionType = InspectionType.VISUAL,
    generalCondition = GeneralCondition.NOT_ASSESSED,
    supplyType = SupplyType.SINGLE_PHASE,
    propertyType = PropertyType.HOUSE,
    reviewReason = "cortes frecuentes",
    reviewedElement = null,
    taskDescription = null,
    visitReasonSnapshot = "cortes frecuentes",
    clientNameSnapshot = "Carlos López",
    addressSnapshot = "Av. X 1234",
    localitySnapshot = "Temperley",
    technicianName = null,
    accessLimitations = null,
    originalTechnicalComment = "Comentario técnico exacto.",
    finalClientReport = null,
    startedAt = Instant.parse("2026-08-04T14:00:00Z"),
    completedAt = null,
    createdAt = Instant.parse("2026-08-04T14:00:00Z"),
    updatedAt = Instant.parse("2026-08-04T14:00:00Z"),
    isDeleted = false,
)
