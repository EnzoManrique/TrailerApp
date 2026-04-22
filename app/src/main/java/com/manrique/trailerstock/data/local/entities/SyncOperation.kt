package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity que registra cada cambio local para ser sincronizado con Firestore.
 * Actúa como auditoría de cambios y queue de sync pendiente.
 */
@Entity(tableName = "sync_operations")
data class SyncOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String,  // "PRODUCTO", "VENTA", "VENTA_DETALLE", etc
    
    @ColumnInfo(name = "entity_id")
    val entityId: Int,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String,  // "CREATE", "UPDATE", "DELETE"
    
    @ColumnInfo(name = "changes")
    val changes: String,  // JSON con los cambios específicos (field: value pairs)
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "device_id")
    val deviceId: String,  // UUID del dispositivo que hace el cambio
    
    @ColumnInfo(name = "synced")
    val synced: Boolean = false,  // ¿Ya fue enviado a Firestore?
    
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,  // Timestamp de cuando se sincronizó
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,  // Número de intentos fallidos
    
    @ColumnInfo(name = "failure_reason")
    val failureReason: String? = null  // Razón del último fallo (si la hay)
)
