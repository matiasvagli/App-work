package com.matiasdev.elecapp.features.clients.domain

import java.time.Instant

data class Client(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val address: String?,
    val locality: String?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isDeleted: Boolean,
)
