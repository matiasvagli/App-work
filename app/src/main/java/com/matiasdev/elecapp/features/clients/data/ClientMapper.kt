package com.matiasdev.elecapp.features.clients.data

import com.matiasdev.elecapp.features.clients.domain.Client
import java.time.Instant

fun ClientEntity.toDomain(): Client = Client(
    id = id,
    fullName = fullName,
    phone = phone,
    email = email,
    address = address,
    locality = locality,
    notes = notes,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun Client.toEntity(): ClientEntity = ClientEntity(
    id = id,
    fullName = fullName,
    phone = phone,
    email = email,
    address = address,
    locality = locality,
    notes = notes,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)
