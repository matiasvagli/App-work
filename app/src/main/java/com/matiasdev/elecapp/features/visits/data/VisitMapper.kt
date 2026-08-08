package com.matiasdev.elecapp.features.visits.data

import com.matiasdev.elecapp.features.visits.domain.Visit
import com.matiasdev.elecapp.features.visits.domain.VisitStatus
import com.matiasdev.elecapp.features.visits.domain.WorkHistoryItem
import java.time.Instant

fun VisitEntity.toDomain(): Visit = Visit(
    id = id,
    clientId = clientId,
    scheduledAt = Instant.ofEpochMilli(scheduledAt),
    estimatedDurationMinutes = estimatedDurationMinutes,
    reason = reason,
    notes = notes,
    status = runCatching { VisitStatus.valueOf(status) }.getOrDefault(VisitStatus.PENDING),
    startedAt = startedAt?.let(Instant::ofEpochMilli),
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    completionNotes = completionNotes,
    pendingWorkNotes = pendingWorkNotes,
    attentionType = attentionType,
    parentVisitId = parentVisitId,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun Visit.toEntity(): VisitEntity = VisitEntity(
    id = id,
    clientId = clientId,
    scheduledAt = scheduledAt.toEpochMilli(),
    estimatedDurationMinutes = estimatedDurationMinutes,
    reason = reason,
    notes = notes,
    status = status.name,
    startedAt = startedAt?.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli(),
    completionNotes = completionNotes,
    pendingWorkNotes = pendingWorkNotes,
    attentionType = attentionType,
    parentVisitId = parentVisitId,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)

fun WorkHistoryItemEntity.toDomain(): WorkHistoryItem = WorkHistoryItem(
    visitId = visitId,
    clientId = clientId,
    clientName = clientName,
    completedAt = Instant.ofEpochMilli(completedAt),
    reason = reason,
    workType = workType,
    workDescription = workDescription,
    durationMinutes = durationMinutes,
)
