package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity que registra cada movimiento de stock (deltas).
 * En lugar de guardar el total, guardamos cada cambio (+5, -3, etc).
 * El server suma todos los deltas para obtener el stock real.
 * Esto evita conflictos cuando dos dispositivos editan stock en paralelo.
 */
@Entity(
    tableName = "stock_movements",
    indices = [
        Index(name = "idx_product", value = ["product_id"]),
        Index(name = "idx_timestamp", value = ["timestamp"])
    ]
)
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "product_id")
    val productId: Int,
    
    @ColumnInfo(name = "delta")
    val delta: Int,  // Cantidad que cambió: +5 (restock), -3 (venta), etc
    
    @ColumnInfo(name = "device_id")
    val deviceId: String,  // Dispositivo que hizo el cambio
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "description")
    val description: String? = null,  // Razón del movimiento: "Venta", "Restock", etc
    
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,  // Timestamp de cuando se sincronizó con Firestore
    
    @ColumnInfo(name = "synced")
    val synced: Boolean = false  // ¿Ya fue confirmado en Firestore?
)
