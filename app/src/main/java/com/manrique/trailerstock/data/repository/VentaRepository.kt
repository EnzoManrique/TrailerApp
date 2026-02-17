package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.VentaDao
import com.manrique.trailerstock.data.local.dao.VentaDetalleDao
import com.manrique.trailerstock.data.local.entities.EstadoVenta
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.local.entities.VentaDetalle
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository para operaciones de Venta con lógica de negocio.
 */
class VentaRepository(
    private val ventaDao: VentaDao,
    private val ventaDetalleDao: VentaDetalleDao,
    private val productoDao: com.manrique.trailerstock.data.local.dao.ProductoDao
) {
    
    val allVentas: Flow<List<Venta>> = ventaDao.obtenerTodas()
    
    suspend fun insert(venta: Venta): Long {
        return ventaDao.insertar(venta)
    }
    
    suspend fun insertWithDetails(venta: Venta, detalles: List<VentaDetalle>): Long {
        val ventaId = ventaDao.insertar(venta)
        val detallesConVentaId = detalles.map { it.copy(ventaId = ventaId.toInt()) }
        ventaDetalleDao.insertarTodos(detallesConVentaId)
        return ventaId
    }
    
    suspend fun delete(venta: Venta, restaurarStock: Boolean = true) {
        if (restaurarStock) {
            // Restaurar stock antes de anular la venta
            val detalles = ventaDetalleDao.obtenerPorVentaSuspend(venta.id)
            detalles.forEach { detalle ->
                val producto = productoDao.obtenerPorId(detalle.productoId)
                if (producto != null) {
                    val nuevoStock = producto.stockActual + detalle.cantidad
                    productoDao.actualizar(producto.copy(stockActual = nuevoStock))
                }
            }
        }
        
        // El borrado de venta tiene ON DELETE CASCADE para detalles en el schema Room
        // Pero ahora no borramos, sino que cambiamos el estado
        ventaDao.actualizar(venta.copy(estado = EstadoVenta.ANULADA))
    }
    
    suspend fun getById(id: Int): Venta? {
        return ventaDao.obtenerPorId(id)
    }
    
    suspend fun getDetallesById(ventaId: Int): List<com.manrique.trailerstock.data.local.entities.DetalleVenta> {
        val detalles = ventaDetalleDao.obtenerPorVentaSuspend(ventaId)
        
        return detalles.map { detalle ->
            val producto = productoDao.obtenerPorId(detalle.productoId)
            com.manrique.trailerstock.data.local.entities.DetalleVenta(
                id = detalle.id,
                ventaId = detalle.ventaId,
                productoId = detalle.productoId,
                nombreProducto = producto?.nombre ?: "Producto #${detalle.productoId}",
                cantidad = detalle.cantidad,
                precioUnitario = detalle.precioUnitario,
                subtotal = detalle.subtotal,
                descuento = 0.0
            )
        }
    }
    
    fun getVentasByDateRange(inicio: Long, fin: Long): Flow<List<Venta>> {
        return ventaDao.obtenerVentasPorFecha(inicio, fin)
    }
    
    // ===== Estadísticas =====
    
    suspend fun getTotalHoy(): Double {
        val (inicio, fin) = getHoyTimestamps()
        return ventaDao.obtenerTotalPorFecha(inicio, fin) ?: 0.0
    }
    
    suspend fun getCantidadVentasHoy(): Int {
        val (inicio, fin) = getHoyTimestamps()
        return ventaDao.contarVentasPorFecha(inicio, fin)
    }
    
    suspend fun getTotalMes(): Double {
        val inicioMes = getInicioMesTimestamp()
        return ventaDao.obtenerTotalDesde(inicioMes) ?: 0.0
    }
    
    suspend fun getTicketPromedio(): Double {
        val inicioMes = getInicioMesTimestamp()
        return ventaDao.obtenerTicketPromedio(inicioMes) ?: 0.0
    }
    
    suspend fun getTotalPorTipoCliente(tipoCliente: String): Double {
        val inicioMes = getInicioMesTimestamp()
        return ventaDao.obtenerTotalPorTipoCliente(tipoCliente, inicioMes) ?: 0.0
    }
    
    // ===== Helpers de fecha =====
    
    private fun getHoyTimestamps(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicio = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val fin = calendar.timeInMillis
        
        return Pair(inicio, fin)
    }
    
    private fun getInicioMesTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    // ===== Métodos para filtros de búsqueda =====
    
    fun getVentasByMetodoPago(metodoPago: String): Flow<List<Venta>> {
        return ventaDao.obtenerPorMetodoPago(metodoPago)
    }
    
    fun getVentasByTipoCliente(tipoCliente: String): Flow<List<Venta>> {
        return ventaDao.obtenerPorTipoCliente(tipoCliente)
    }
    
    fun getVentasByRangoFechas(fechaInicio: Long, fechaFin: Long): Flow<List<Venta>> {
        return ventaDao.obtenerPorRangoFechas(fechaInicio, fechaFin)
    }
    
    /**
     * Obtiene una venta con todos sus detalles y productos
     */
    suspend fun getVentaConDetalles(ventaId: Int): com.manrique.trailerstock.model.VentaConDetalles? {
        val venta = getById(ventaId) ?: return null
        val detalles = ventaDetalleDao.obtenerPorVentaSuspend(ventaId)
        
        // Aquí necesitamos ProductoRepository para obtener los productos
        // Se pasará desde el ViewModel
        return null // Se implementará en el ViewModel
    }
}
