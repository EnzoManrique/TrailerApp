package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.manrique.trailerstock.data.local.entities.EstadoVenta
import com.manrique.trailerstock.data.local.entities.MetodoPago

/**
 * Entity que representa una venta realizada.
 */
@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "fecha")
    val fecha: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "total")
    val total: Double,
    
    @ColumnInfo(name = "tipo_cliente")
    val tipoCliente: String,  // "LISTA" o "MAYORISTA"
    
    @ColumnInfo(name = "metodo_pago")
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    
    @ColumnInfo(name = "numero_venta")
    val numeroVenta: String? = null,
    
    @ColumnInfo(name = "notas")
    val notas: String? = null,
    
    @ColumnInfo(name = "estado")
    val estado: EstadoVenta = EstadoVenta.ACTIVA,
    
    // === Columnas de sincronización ===
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,  // Timestamp de última sincronización con Firestore
    
    @ColumnInfo(name = "device_id")
    val deviceId: String = "",  // UUID del dispositivo que creó la venta
    
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null  // ID del documento en Firestore
) {
    companion object {
        const val TIPO_LISTA = "LISTA"
        const val TIPO_MAYORISTA = "MAYORISTA"
    }
}
