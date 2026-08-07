package com.matiasdev.elecapp.features.inspections.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query(
        """
        SELECT ei.*,
            v.scheduled_at AS visit_scheduled_at,
            p.pillar_exists AS pillar_exists,
            p.general_condition AS pillar_condition,
            mp.accessible AS main_panel_accessible,
            mp.general_condition AS main_panel_condition,
            (
                SELECT COUNT(*) FROM inspection_findings f
                WHERE f.inspection_id = ei.id AND f.is_deleted = 0
            ) AS finding_count,
            (
                SELECT COUNT(*) FROM inspection_unverified_items u
                WHERE u.inspection_id = ei.id AND u.is_deleted = 0
            ) AS unverified_count
        FROM electrical_inspections ei
        LEFT JOIN visits v ON v.id = ei.visit_id AND v.is_deleted = 0
        LEFT JOIN pillar_inspections p ON p.inspection_id = ei.id
        LEFT JOIN main_panel_inspections mp ON mp.inspection_id = ei.id
        WHERE ei.is_deleted = 0
            AND (:status IS NULL OR ei.status = :status)
            AND (
                :query = ''
                OR ei.client_name_snapshot LIKE '%' || :query || '%' COLLATE NOCASE
                OR ei.address_snapshot LIKE '%' || :query || '%' COLLATE NOCASE
                OR ei.locality_snapshot LIKE '%' || :query || '%' COLLATE NOCASE
                OR ei.visit_reason_snapshot LIKE '%' || :query || '%' COLLATE NOCASE
            )
        ORDER BY COALESCE(v.scheduled_at, ei.started_at) DESC
        """,
    )
    fun observeInspectionList(status: String?, query: String): Flow<List<InspectionListItemEntity>>

    @Query("SELECT COUNT(*) FROM electrical_inspections WHERE is_deleted = 0 AND status = 'DRAFT'")
    fun observeDraftInspectionCount(): Flow<Int>

    @Query("SELECT * FROM electrical_inspections WHERE visit_id = :visitId AND is_deleted = 0 LIMIT 1")
    fun observeActiveInspectionForVisit(visitId: String): Flow<ElectricalInspectionEntity?>

    @Query("SELECT * FROM electrical_inspections WHERE id = :inspectionId AND is_deleted = 0 LIMIT 1")
    fun observeActiveInspectionById(inspectionId: String): Flow<ElectricalInspectionEntity?>

    @Query("SELECT * FROM electrical_inspections WHERE visit_id = :visitId AND is_deleted = 0 LIMIT 1")
    suspend fun findActiveInspectionForVisit(visitId: String): ElectricalInspectionEntity?

    @Query("SELECT * FROM electrical_inspections WHERE id = :inspectionId AND is_deleted = 0 LIMIT 1")
    suspend fun findActiveInspectionById(inspectionId: String): ElectricalInspectionEntity?

    @Query("SELECT * FROM pillar_inspections WHERE inspection_id = :inspectionId LIMIT 1")
    fun observePillar(inspectionId: String): Flow<PillarInspectionEntity?>

    @Query(
        """
        SELECT * FROM pillar_measurements
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    fun observePillarMeasurements(inspectionId: String): Flow<List<PillarMeasurementEntity>>

    @Query("SELECT * FROM main_panel_inspections WHERE inspection_id = :inspectionId LIMIT 1")
    fun observeMainPanel(inspectionId: String): Flow<MainPanelInspectionEntity?>

    @Query("SELECT * FROM grounding_inspections WHERE inspection_id = :inspectionId LIMIT 1")
    fun observeGrounding(inspectionId: String): Flow<GroundingInspectionEntity?>

    @Query(
        """
        SELECT * FROM main_panel_measurements
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    fun observeMainPanelMeasurements(inspectionId: String): Flow<List<MainPanelMeasurementEntity>>

    @Query(
        """
        SELECT * FROM main_panel_circuits
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    fun observeMainPanelCircuits(inspectionId: String): Flow<List<MainPanelCircuitEntity>>

    @Query(
        """
        SELECT * FROM inspection_findings
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    fun observeFindings(inspectionId: String): Flow<List<InspectionFindingEntity>>

    @Query(
        """
        SELECT * FROM inspection_unverified_items
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY created_at ASC
        """,
    )
    fun observeUnverifiedItems(inspectionId: String): Flow<List<InspectionUnverifiedItemEntity>>

    @Query("SELECT * FROM pillar_inspections WHERE inspection_id = :inspectionId LIMIT 1")
    suspend fun findPillar(inspectionId: String): PillarInspectionEntity?

    @Query(
        """
        SELECT * FROM pillar_measurements
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    suspend fun findPillarMeasurements(inspectionId: String): List<PillarMeasurementEntity>

    @Query("SELECT * FROM main_panel_inspections WHERE inspection_id = :inspectionId LIMIT 1")
    suspend fun findMainPanel(inspectionId: String): MainPanelInspectionEntity?

    @Query("SELECT * FROM grounding_inspections WHERE inspection_id = :inspectionId LIMIT 1")
    suspend fun findGrounding(inspectionId: String): GroundingInspectionEntity?

    @Query(
        """
        SELECT * FROM main_panel_measurements
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    suspend fun findMainPanelMeasurements(inspectionId: String): List<MainPanelMeasurementEntity>

    @Query(
        """
        SELECT * FROM main_panel_circuits
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    suspend fun findMainPanelCircuits(inspectionId: String): List<MainPanelCircuitEntity>

    @Query(
        """
        SELECT * FROM inspection_findings
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    suspend fun findFindings(inspectionId: String): List<InspectionFindingEntity>

    @Query(
        """
        SELECT * FROM inspection_unverified_items
        WHERE inspection_id = :inspectionId AND is_deleted = 0
        ORDER BY created_at ASC
        """,
    )
    suspend fun findUnverifiedItems(inspectionId: String): List<InspectionUnverifiedItemEntity>

    @Upsert
    suspend fun upsertInspection(inspection: ElectricalInspectionEntity)

    @Upsert
    suspend fun upsertPillar(pillar: PillarInspectionEntity)

    @Upsert
    suspend fun upsertPillarMeasurement(measurement: PillarMeasurementEntity)

    @Upsert
    suspend fun upsertMainPanel(mainPanel: MainPanelInspectionEntity)

    @Upsert
    suspend fun upsertGrounding(grounding: GroundingInspectionEntity)

    @Upsert
    suspend fun upsertMainPanelMeasurement(measurement: MainPanelMeasurementEntity)

    @Upsert
    suspend fun upsertMainPanelCircuit(circuit: MainPanelCircuitEntity)

    @Upsert
    suspend fun upsertFinding(finding: InspectionFindingEntity)

    @Upsert
    suspend fun upsertUnverifiedItem(item: InspectionUnverifiedItemEntity)

    @Query("UPDATE inspection_findings SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteFinding(id: String, updatedAt: Long)

    @Query("UPDATE pillar_measurements SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeletePillarMeasurement(id: String, updatedAt: Long)

    @Query("UPDATE main_panel_measurements SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteMainPanelMeasurement(id: String, updatedAt: Long)

    @Query("UPDATE main_panel_circuits SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteMainPanelCircuit(id: String, updatedAt: Long)

    @Query("UPDATE inspection_unverified_items SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteUnverifiedItem(id: String, updatedAt: Long)

    @Query(
        """
        UPDATE inspection_unverified_items
        SET is_deleted = 1, updated_at = :updatedAt
        WHERE inspection_id = :inspectionId
        """,
    )
    suspend fun softDeleteUnverifiedItemsForInspection(inspectionId: String, updatedAt: Long)

    @Transaction
    suspend fun replaceUnverifiedItems(
        inspectionId: String,
        updatedAt: Long,
        items: List<InspectionUnverifiedItemEntity>,
    ) {
        softDeleteUnverifiedItemsForInspection(inspectionId, updatedAt)
        items.forEach { upsertUnverifiedItem(it) }
    }
}
