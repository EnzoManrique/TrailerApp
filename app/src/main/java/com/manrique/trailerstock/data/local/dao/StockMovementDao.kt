package com.manrique.trailerstock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.manrique.trailerstock.data.local.entities.StockMovement
import kotlinx.coroutines.flow.Flow

/**
 * DAO para movimientos de stock.
 * Cada insert, delete de stock se registra aquí como un delta (+5, -3, etc).
 * El servidor suma todos los deltas para obtener el stock real.
 */
@Dao
interface StockMovementDao {
    
    /**
     * Registra un movimiento de stock
     */
    @Insert
    suspend fun insert(movement: StockMovement): Long
    
    /**
     * Registra múltiples movimientos
     */
    @Insert
    suspend fun insertAll(movements: List<StockMovement>): List<Long>
    
    /**
     * Obtiene todos los movimientos de un producto
     */
    @Query("SELECT * FROM stock_movements WHERE product_id = :productId ORDER BY timestamp DESC")
    suspend fun getByProductId(productId: Int): List<StockMovement>
    
    /**
     * Observa movimientos de un producto reactivamente
     */
    @Query("SELECT * FROM stock_movements WHERE product_id = :productId ORDER BY timestamp DESC")
    fun observeByProductId(productId: Int): Flow<List<StockMovement>>
    
    /**
     * Calcula el delta total de stock para un producto (suma de todos los deltas)
     */
    @Query("SELECT COALESCE(SUM(delta), 0) FROM stock_movements WHERE product_id = :productId")
    suspend fun calculateTotalDelta(productId: Int): Int
    
    /**
     * Calcula el delta total de stock desde un timestamp específico (para sincronización incremental)
     */
    @Query("""
        SELECT COALESCE(SUM(delta), 0) 
        FROM stock_movements 
        WHERE product_id = :productId AND timestamp > :sinceTimestamp
    """)
    suspend fun calculateDeltaSince(productId: Int, sinceTimestamp: Long): Int
    
    /**
     * Obtiene movimientos no sincronizados
     */
    @Query("SELECT * FROM stock_movements WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingMovements(): List<StockMovement>
    
    /**
     * Observa movimientos no sincronizados
     */
    @Query("SELECT * FROM stock_movements WHERE synced = 0 ORDER BY timestamp ASC")
    fun observePendingMovements(): Flow<List<StockMovement>>
    
    /**
     * Marca un movimiento como sincronizado
     */
    @Query("UPDATE stock_movements SET synced = 1, synced_at = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: Long = System.currentTimeMillis())
    
    /**
     * Marca múltiples movimientos como sincronizados
     */
    @Query("UPDATE stock_movements SET synced = 1, synced_at = :syncedAt WHERE id IN (:ids)")
    suspend fun markMultipleAsSynced(ids: List<Int>, syncedAt: Long = System.currentTimeMillis())
    
    /**
     * Obtiene movimientos de un rango de fechas (para auditoría)
     */
    @Query("""
        SELECT * FROM stock_movements 
        WHERE product_id = :productId 
        AND timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp ASC
    """)
    suspend fun getMovementsByDateRange(productId: Int, startTime: Long, endTime: Long): List<StockMovement>
    
    /**
     * Cuenta movimientos pendientes
     */
    @Query("SELECT COUNT(*) FROM stock_movements WHERE synced = 0")
    suspend fun countPending(): Int
    
    /**
     * Limpia movimientos antiguos sincronizados
     */
    @Query("DELETE FROM stock_movements WHERE synced = 1 AND synced_at < :beforeTimestamp")
    suspend fun deleteOldSyncedMovements(beforeTimestamp: Long)
}
