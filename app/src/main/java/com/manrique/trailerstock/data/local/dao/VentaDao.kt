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
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :timestampInicio AND fecha < :timestampFin AND estado = 'ACTIVA'")
    suspend fun obtenerTotalPorFecha(timestampInicio: Long, timestampFin: Long): Double?
    
    // Total del mes
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :timestampInicio AND estado = 'ACTIVA'")
    suspend fun obtenerTotalDesde(timestampInicio: Long): Double?
    
    // Ventas por tipo de cliente
    @Query("SELECT SUM(total) FROM ventas WHERE tipo_cliente = :tipoCliente AND fecha >= :timestampInicio AND estado = 'ACTIVA'")
    suspend fun obtenerTotalPorTipoCliente(tipoCliente: String, timestampInicio: Long): Double?
    
    // Ticket promedio
    @Query("SELECT AVG(total) FROM ventas WHERE fecha >= :timestampInicio AND estado = 'ACTIVA'")
    suspend fun obtenerTicketPromedio(timestampInicio: Long): Double?
    
    // Cantidad de ventas
    @Query("SELECT COUNT(*) FROM ventas WHERE fecha >= :timestampInicio AND fecha < :timestampFin AND estado = 'ACTIVA'")
    suspend fun contarVentasPorFecha(timestampInicio: Long, timestampFin: Long): Int
    
    // Filtros para búsqueda de ventas
    @Query("SELECT * FROM ventas WHERE metodo_pago = :metodoPago ORDER BY fecha DESC")
    fun obtenerPorMetodoPago(metodoPago: String): Flow<List<Venta>>
    
    @Query("SELECT * FROM ventas WHERE tipo_cliente = :tipoCliente ORDER BY fecha DESC")
    fun obtenerPorTipoCliente(tipoCliente: String): Flow<List<Venta>>
    
    @Query("SELECT * FROM ventas WHERE fecha >= :fechaInicio AND fecha <= :fechaFin ORDER BY fecha DESC")
    fun obtenerPorRangoFechas(fechaInicio: Long, fechaFin: Long): Flow<List<Venta>>

    // Ganancia estimada (Ventas - Costo)
    @Query("""
        SELECT SUM(vd.subtotal - (vd.cantidad * p.precio_costo)) 
        FROM venta_detalles vd 
        JOIN productos p ON vd.producto_id = p.id 
        JOIN ventas v ON vd.venta_id = v.id 
        WHERE v.fecha >= :timestampInicio AND v.estado = 'ACTIVA'
    """)
    suspend fun obtenerGananciaEstimada(timestampInicio: Long): Double?

    // Top productos más vendidos
    @Query("""
        SELECT p.nombre as nombre, SUM(vd.cantidad) as cantidadVendida 
        FROM venta_detalles vd 
        JOIN productos p ON vd.producto_id = p.id 
        JOIN ventas v ON vd.venta_id = v.id 
        WHERE v.fecha >= :timestampInicio AND v.estado = 'ACTIVA' 
        GROUP BY vd.producto_id 
        ORDER BY cantidadVendida DESC 
        LIMIT :limit
    """)
    suspend fun obtenerTopProductos(timestampInicio: Long, limit: Int): List<ProductoVendido>
}

/**
 * Pojo para los productos más vendidos
 */
data class ProductoVendido(
    val nombre: String,
    val cantidadVendida: Int
)
