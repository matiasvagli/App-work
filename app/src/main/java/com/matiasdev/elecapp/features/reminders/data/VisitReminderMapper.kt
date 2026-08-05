package com.matiasdev.elecapp.features.reminders.data

import com.matiasdev.elecapp.features.reminders.domain.VisitReminder
import java.time.Instant

fun VisitReminderEntity.toDomain(): VisitReminder = VisitReminder(
    id = id,
    visitId = visitId,
    minutesBefore = minutesBefore,
    enabled = enabled,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun VisitReminder.toEntity(): VisitReminderEntity = VisitReminderEntity(
    id = id,
    visitId = visitId,
    minutesBefore = minutesBefore,
    enabled = enabled,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)
