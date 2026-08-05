package com.matiasdev.elecapp.features.electricaltools.data

import com.matiasdev.elecapp.features.electricaltools.domain.CalculationSource
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculation
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalCalculationType
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicalClassification
import com.matiasdev.elecapp.features.electricaltools.domain.TechnicianConclusion
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TechnicalCalculationRepository {
    fun observeAll(): Flow<List<TechnicalCalculation>>

    fun observeById(id: String): Flow<TechnicalCalculation?>

    fun observeByClient(clientId: String): Flow<List<TechnicalCalculation>>

    fun observeByVisit(visitId: String): Flow<List<TechnicalCalculation>>

    fun observeByInspection(inspectionId: String): Flow<List<TechnicalCalculation>>

    fun observeByType(type: TechnicalCalculationType): Flow<List<TechnicalCalculation>>

    fun observeBySource(source: CalculationSource): Flow<List<TechnicalCalculation>>

    fun observeByClassification(classification: TechnicalClassification): Flow<List<TechnicalCalculation>>

    fun search(filters: TechnicalCalculationFilters): Flow<List<TechnicalCalculation>>

    suspend fun findById(id: String): TechnicalCalculation?

    suspend fun save(calculation: TechnicalCalculation)

    suspend fun updateEditableFields(
        id: String,
        title: String,
        description: String?,
        technicianConclusion: TechnicianConclusion,
        technicianNotes: String?,
    )

    suspend fun associate(id: String, clientId: String?, visitId: String?, inspectionId: String?)

    suspend fun unlinkInspection(id: String)

    suspend fun softDelete(id: String)
}

data class TechnicalCalculationFilters(
    val query: String = "",
    val type: TechnicalCalculationType? = null,
    val source: CalculationSource? = null,
    val classification: TechnicalClassification? = null,
    val associatedToInspection: Boolean? = null,
    val unassociated: Boolean? = null,
)

class RoomTechnicalCalculationRepository(
    private val dao: TechnicalCalculationDao,
) : TechnicalCalculationRepository {
    override fun observeAll(): Flow<List<TechnicalCalculation>> = dao.observeAll().mapEntities()

    override fun observeById(id: String): Flow<TechnicalCalculation?> = dao.observeById(id).map { it?.toDomain() }

    override fun observeByClient(clientId: String): Flow<List<TechnicalCalculation>> = dao.observeByClient(clientId).mapEntities()

    override fun observeByVisit(visitId: String): Flow<List<TechnicalCalculation>> = dao.observeByVisit(visitId).mapEntities()

    override fun observeByInspection(inspectionId: String): Flow<List<TechnicalCalculation>> = dao.observeByInspection(inspectionId).mapEntities()

    override fun observeByType(type: TechnicalCalculationType): Flow<List<TechnicalCalculation>> = dao.observeByType(type.name).mapEntities()

    override fun observeBySource(source: CalculationSource): Flow<List<TechnicalCalculation>> = dao.observeBySource(source.name).mapEntities()

    override fun observeByClassification(classification: TechnicalClassification): Flow<List<TechnicalCalculation>> {
        return dao.observeByClassification(classification.name).mapEntities()
    }

    override fun search(filters: TechnicalCalculationFilters): Flow<List<TechnicalCalculation>> {
        return dao.search(
            query = filters.query.trim(),
            type = filters.type?.name,
            source = filters.source?.name,
            classification = filters.classification?.name,
            associatedToInspection = filters.associatedToInspection,
            unassociated = filters.unassociated,
        ).mapEntities()
    }

    override suspend fun findById(id: String): TechnicalCalculation? = dao.findById(id)?.toDomain()

    override suspend fun save(calculation: TechnicalCalculation) {
        dao.upsert(calculation.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun updateEditableFields(
        id: String,
        title: String,
        description: String?,
        technicianConclusion: TechnicianConclusion,
        technicianNotes: String?,
    ) {
        dao.updateEditableFields(
            id = id,
            title = title.trim(),
            description = description?.trim()?.takeIf(String::isNotBlank),
            technicianConclusion = technicianConclusion.name,
            technicianNotes = technicianNotes?.trim()?.takeIf(String::isNotBlank),
            updatedAt = Instant.now().toEpochMilli(),
        )
    }

    override suspend fun associate(id: String, clientId: String?, visitId: String?, inspectionId: String?) {
        dao.associate(
            id = id,
            clientId = clientId?.takeIf(String::isNotBlank),
            visitId = visitId?.takeIf(String::isNotBlank),
            inspectionId = inspectionId?.takeIf(String::isNotBlank),
            updatedAt = Instant.now().toEpochMilli(),
        )
    }

    override suspend fun unlinkInspection(id: String) = dao.unlinkInspection(id, Instant.now().toEpochMilli())

    override suspend fun softDelete(id: String) = dao.softDelete(id, Instant.now().toEpochMilli())
}

private fun Flow<List<TechnicalCalculationEntity>>.mapEntities(): Flow<List<TechnicalCalculation>> {
    return map { rows -> rows.map(TechnicalCalculationEntity::toDomain) }
}
