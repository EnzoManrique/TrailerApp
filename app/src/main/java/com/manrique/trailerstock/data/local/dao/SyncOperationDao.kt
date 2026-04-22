package com.manrique.trailerstock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.manrique.trailerstock.data.local.entities.SyncOperation
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de sincronización.
 * Gestiona la queue de cambios pendientes de sincronizar con Firestore.
 */
@Dao
interface SyncOperationDao {
    
    /**
     * Inserta una nueva operación de sync
     */
    @Insert
    suspend fun insert(operation: SyncOperation): Long
    
    /**
     * Inserta múltiples operaciones
     */
    @Insert
    suspend fun insertAll(operations: List<SyncOperation>): List<Long>
    
    /**
     * Actualiza una operación (marcar como synced, incrementar retry count, etc)
     */
    @Update
    suspend fun update(operation: SyncOperation)
    
    /**
     * Obtiene todas las operaciones pendientes de sincronizar (synced = false)
     */
    @Query("SELECT * FROM sync_operations WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingOperations(): List<SyncOperation>
    
    /**
     * Observa todas las operaciones pendientes reactivamente
     */
    @Query("SELECT * FROM sync_operations WHERE synced = 0 ORDER BY timestamp ASC")
    fun observePendingOperations(): Flow<List<SyncOperation>>
    
    /**
     * Obtiene operaciones con reintentos fallidos (para logging/debugging)
     */
    @Query("SELECT * FROM sync_operations WHERE retry_count > 0 AND synced = 0 ORDER BY timestamp DESC")
    suspend fun getFailedOperations(): List<SyncOperation>
    
    /**
     * Obtiene una operación por ID
     */
    @Query("SELECT * FROM sync_operations WHERE id = :id")
    suspend fun getById(id: Int): SyncOperation?
    
    /**
     * Marca una operación como sincronizada
     */
    @Query("UPDATE sync_operations SET synced = 1, synced_at = :syncedAt WHERE id = :id")
    suspend fun markAsSynced(id: Int, syncedAt: Long = System.currentTimeMillis())
    
    /**
     * Incrementa el retry count de una operación
     */
    @Query("UPDATE sync_operations SET retry_count = retry_count + 1, failure_reason = :reason WHERE id = :id")
    suspend fun incrementRetryCount(id: Int, reason: String?)
    
    /**
     * Obtiene el número de operaciones pendientes
     */
    @Query("SELECT COUNT(*) FROM sync_operations WHERE synced = 0")
    suspend fun countPending(): Int
    
    /**
     * Obtiene el número de operaciones pendientes (Flow reactivo)
     */
    @Query("SELECT COUNT(*) FROM sync_operations WHERE synced = 0")
    fun observeCountPending(): Flow<Int>
    
    /**
     * Limpia operaciones antiguas sincronizadas (más de X días)
     */
    @Query("DELETE FROM sync_operations WHERE synced = 1 AND synced_at < :beforeTimestamp")
    suspend fun deleteOldSyncedOperations(beforeTimestamp: Long)
    
    /**
     * Obtiene operaciones pendientes del último N días
     */
    @Query("""
        SELECT * FROM sync_operations 
        WHERE synced = 0 AND timestamp > :sinceTimestamp 
        ORDER BY timestamp ASC
    """)
    suspend fun getPendingOperationsSince(sinceTimestamp: Long): List<SyncOperation>
}
