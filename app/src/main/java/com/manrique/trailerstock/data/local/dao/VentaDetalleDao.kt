package com.manrique.trailerstock.data.local.dao

import androidx.room.*
import com.manrique.trailerstock.data.local.entities.VentaDetalle
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de VentaDetalle con soporte de Coroutines.
 */
@Dao
interface VentaDetalleDao {
    
    @Query("SELECT * FROM venta_detalles WHERE venta_id = :ventaId")
    fun obtenerPorVenta(ventaId: Int): Flow<List<VentaDetalle>>
    
    @Query("SELECT * FROM venta_detalles WHERE venta_id = :ventaId")
    suspend fun obtenerPorVentaSuspend(ventaId: Int): List<VentaDetalle>
    
    @Insert
    suspend fun insertar(detalle: VentaDetalle): Long
    
    @Insert
    suspend fun insertarTodos(detalles: List<VentaDetalle>): List<Long>
    
    @Update
    suspend fun actualizar(detalle: VentaDetalle)
    
    @Delete
    suspend fun eliminar(detalle: VentaDetalle)
    
    @Query("DELETE FROM venta_detalles WHERE venta_id = :ventaId")
    suspend fun eliminarPorVenta(ventaId: Int)
}
