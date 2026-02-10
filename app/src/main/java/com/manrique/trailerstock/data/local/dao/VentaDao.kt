package com.manrique.trailerstock.data.local.dao

import androidx.room.*
import com.manrique.trailerstock.data.local.entities.Venta
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Venta con soporte de Coroutines.
 */
@Dao
interface VentaDao {
    
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Venta>>
    
    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Venta?
    
    @Insert
    suspend fun insertar(venta: Venta): Long
    
    @Update
    suspend fun actualizar(venta: Venta)
    
    @Delete
    suspend fun eliminar(venta: Venta)
    
    // Ventas del día
    @Query("SELECT * FROM ventas WHERE fecha >= :timestampInicio AND fecha < :timestampFin ORDER BY fecha DESC")
    fun obtenerVentasPorFecha(timestampInicio: Long, timestampFin: Long): Flow<List<Venta>>
    
    // Total de ventas del día
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :timestampInicio AND fecha < :timestampFin")
    suspend fun obtenerTotalPorFecha(timestampInicio: Long, timestampFin: Long): Double?
    
    // Total del mes
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :timestampInicio")
    suspend fun obtenerTotalDesde(timestampInicio: Long): Double?
    
    // Ventas por tipo de cliente
    @Query("SELECT SUM(total) FROM ventas WHERE tipo_cliente = :tipoCliente AND fecha >= :timestampInicio")
    suspend fun obtenerTotalPorTipoCliente(tipoCliente: String, timestampInicio: Long): Double?
    
    // Ticket promedio
    @Query("SELECT AVG(total) FROM ventas WHERE fecha >= :timestampInicio")
    suspend fun obtenerTicketPromedio(timestampInicio: Long): Double?
    
    // Cantidad de ventas
    @Query("SELECT COUNT(*) FROM ventas WHERE fecha >= :timestampInicio AND fecha < :timestampFin")
    suspend fun contarVentasPorFecha(timestampInicio: Long, timestampFin: Long): Int
}
