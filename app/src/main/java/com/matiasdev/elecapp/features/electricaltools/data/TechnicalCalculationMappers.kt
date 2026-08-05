package com.matiasdev.elecapp.features.electricaltools.data

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import java.time.Instant

fun TechnicalCalculationEntity.toDomain(): TechnicalCalculation = TechnicalCalculation(
    id = id,
    type = TechnicalCalculationType.valueOf(type),
    source = CalculationSource.valueOf(source),
    clientId = clientId,
    visitId = visitId,
    inspectionId = inspectionId,
    title = title,
    description = description,
    inputDataJson = inputDataJson,
    resultDataJson = resultDataJson,
    primaryResultValue = primaryResultValue,
    primaryResultUnit = primaryResultUnit,
    classification = TechnicalClassification.valueOf(classification),
    technicianConclusion = TechnicianConclusion.valueOf(technicianConclusion),
    technicianNotes = technicianNotes,
    formulaVersion = formulaVersion,
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    isDeleted = isDeleted,
)

fun TechnicalCalculation.toEntity(): TechnicalCalculationEntity = TechnicalCalculationEntity(
    id = id,
    type = type.name,
    source = source.name,
    clientId = clientId,
    visitId = visitId,
    inspectionId = inspectionId,
    title = title,
    description = description,
    inputDataJson = inputDataJson,
    resultDataJson = resultDataJson,
    primaryResultValue = primaryResultValue,
    primaryResultUnit = primaryResultUnit,
    classification = classification.name,
    technicianConclusion = technicianConclusion.name,
    technicianNotes = technicianNotes,
    formulaVersion = formulaVersion,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
    isDeleted = isDeleted,
)
