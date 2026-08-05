package com.matiasdev.elecapp.features.visits.data

import com.matiasdev.elecapp.features.visits.domain.VisitWorkSession
import com.matiasdev.elecapp.features.visits.domain.VisitWorkSessionStatus
import java.time.Instant

fun VisitWorkSessionEntity.toDomain(): VisitWorkSession = VisitWorkSession(
    id = id,
    visitId = visitId,
    startedAt = Instant.ofEpochMilli(startedAt),
    endedAt = endedAt?.let(Instant::ofEpochMilli),
    status = VisitWorkSessionStatus.valueOf(status),
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun VisitWorkSession.toEntity(): VisitWorkSessionEntity = VisitWorkSessionEntity(
    id = id,
    visitId = visitId,
    startedAt = startedAt.toEpochMilli(),
    endedAt = endedAt?.toEpochMilli(),
    status = status.name,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)
