package com.matiasdev.elecapp.features.finance.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitCompletionDao {
    @Query("SELECT * FROM visit_completions WHERE visit_id = :visitId AND is_deleted = 0 LIMIT 1")
    fun observeByVisitId(visitId: String): Flow<VisitCompletionEntity?>

    @Query("SELECT * FROM visit_completions WHERE visit_id = :visitId AND is_deleted = 0 LIMIT 1")
    suspend fun findByVisitId(visitId: String): VisitCompletionEntity?

    @Upsert
    suspend fun upsert(completion: VisitCompletionEntity)
}

@Dao
interface ServiceReceiptDao {
    @Query("SELECT * FROM service_receipts WHERE is_deleted = 0 ORDER BY COALESCE(issued_at, created_at) DESC")
    fun observeAll(): Flow<List<ServiceReceiptEntity>>

    @Query("SELECT * FROM service_receipts WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun observeById(id: String): Flow<ServiceReceiptEntity?>

    @Query("SELECT * FROM service_receipts WHERE visit_id = :visitId AND is_deleted = 0 ORDER BY created_at DESC LIMIT 1")
    fun observeByVisitId(visitId: String): Flow<ServiceReceiptEntity?>

    @Query("SELECT * FROM service_receipts WHERE client_id = :clientId AND is_deleted = 0 ORDER BY COALESCE(issued_at, created_at) DESC")
    fun observeByClientId(clientId: String): Flow<List<ServiceReceiptEntity>>

    @Query("SELECT * FROM service_receipts WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun findById(id: String): ServiceReceiptEntity?

    @Query("SELECT * FROM service_receipts WHERE visit_id = :visitId AND is_deleted = 0 ORDER BY created_at DESC LIMIT 1")
    suspend fun findByVisitId(visitId: String): ServiceReceiptEntity?

    @Query(
        """
        SELECT * FROM service_receipts
        WHERE is_deleted = 0 AND status != 'CANCELLED'
            AND COALESCE(issued_at, created_at) >= :startMillis
            AND COALESCE(issued_at, created_at) < :endMillis
        """,
    )
    suspend fun findValidInRange(startMillis: Long, endMillis: Long): List<ServiceReceiptEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(receipt: ServiceReceiptEntity)

    @Update
    suspend fun update(receipt: ServiceReceiptEntity)

    @Query("UPDATE service_receipts SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id AND status = 'DRAFT'")
    suspend fun softDeleteDraft(id: String, updatedAt: Long)
}

@Dao
interface ServiceReceiptItemDao {
    @Query("SELECT * FROM service_receipt_items WHERE receipt_id = :receiptId AND is_deleted = 0 ORDER BY sort_order ASC")
    fun observeByReceiptId(receiptId: String): Flow<List<ServiceReceiptItemEntity>>

    @Query("SELECT * FROM service_receipt_items WHERE receipt_id = :receiptId AND is_deleted = 0 ORDER BY sort_order ASC")
    suspend fun findByReceiptId(receiptId: String): List<ServiceReceiptItemEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<ServiceReceiptItemEntity>)

    @Update
    suspend fun update(item: ServiceReceiptItemEntity)

    @Query("UPDATE service_receipt_items SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("UPDATE service_receipt_items SET is_deleted = 1, updated_at = :updatedAt WHERE receipt_id = :receiptId")
    suspend fun softDeleteByReceiptId(receiptId: String, updatedAt: Long)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE is_deleted = 0 ORDER BY paid_at DESC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE service_receipt_id = :receiptId AND is_deleted = 0 ORDER BY paid_at ASC")
    fun observeByReceiptId(receiptId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE client_id = :clientId AND is_deleted = 0 ORDER BY paid_at DESC")
    fun observeByClientId(clientId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE service_receipt_id = :receiptId AND is_deleted = 0 ORDER BY paid_at ASC")
    suspend fun findByReceiptId(receiptId: String): List<PaymentEntity>

    @Query(
        """
        SELECT * FROM payments
        WHERE is_deleted = 0 AND paid_at >= :startMillis AND paid_at < :endMillis
        ORDER BY paid_at DESC
        """,
    )
    suspend fun findByDateRange(startMillis: Long, endMillis: Long): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(payment: PaymentEntity)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Query("UPDATE payments SET status = 'CANCELLED', updated_at = :updatedAt WHERE id = :id AND is_deleted = 0")
    suspend fun cancel(id: String, updatedAt: Long)

    @Query(
        """
        SELECT COALESCE(SUM(amount_cents), 0) FROM payments
        WHERE service_receipt_id = :receiptId AND status = 'CONFIRMED' AND is_deleted = 0
        """,
    )
    suspend fun sumConfirmedByReceiptId(receiptId: String): Long
}

@Dao
interface ReceiptSequenceDao {
    @Query("SELECT * FROM receipt_sequence WHERE id = 'service_receipt' LIMIT 1")
    suspend fun find(): ReceiptSequenceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sequence: ReceiptSequenceEntity)

    @Query("UPDATE receipt_sequence SET next_number = :nextNumber, updated_at = :updatedAt WHERE id = 'service_receipt'")
    suspend fun updateNext(nextNumber: Long, updatedAt: Long)
}
