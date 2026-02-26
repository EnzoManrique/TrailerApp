package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.VentaDao
import com.manrique.trailerstock.data.local.dao.VentaDetalleDao
import com.manrique.trailerstock.data.local.entities.EstadoVenta
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.local.entities.VentaDetalle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun getVentasCompletasByDateRange(inicio: Long, fin: Long): Flow<List<com.manrique.trailerstock.model.VentaConDetalles>> {
        return ventaDao.obtenerVentasPorFecha(inicio, fin).map { ventas ->
            ventas.map { venta ->
                val detalles = ventaDetalleDao.obtenerPorVentaSuspend(venta.id).map { dv ->
                    val producto = productoDao.obtenerPorId(dv.productoId) ?: Producto(
                        id = dv.productoId,
                        nombre = "Descatalogado (#${dv.productoId})",
                        precioCosto = 0.0,
                        precioLista = dv.precioUnitario,
                        precioMayorista = dv.precioUnitario,
                        stockActual = 0,
                        stockMinimo = 0,
                        categoriaId = 0
                    )
                    com.manrique.trailerstock.model.VentaDetalleConProducto(dv, producto)
                }
                com.manrique.trailerstock.model.VentaConDetalles(venta, detalles)
            }
        }
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
    
    suspend fun getTotalVentasPeriodo(inicio: Long): Double {
        return ventaDao.obtenerTotalDesde(inicio) ?: 0.0
    }
    
    suspend fun getTicketPromedio(inicio: Long): Double {
        return ventaDao.obtenerTicketPromedio(inicio) ?: 0.0
    }
    
    suspend fun getGananciaPeriodo(inicio: Long): Double {
        return ventaDao.obtenerGananciaEstimada(inicio) ?: 0.0
    }

    suspend fun getCantidadVentasPeriodo(inicio: Long): Int {
        val calendar = Calendar.getInstance()
        val fin = calendar.timeInMillis
        return ventaDao.contarVentasPorFecha(inicio, fin)
    }

    suspend fun getTopProductos(inicio: Long, limit: Int = 5): List<com.manrique.trailerstock.data.local.dao.ProductoVendido> {
        return ventaDao.obtenerTopProductos(inicio, limit)
    }

    suspend fun getVentasPorCategoria(inicio: Long): List<com.manrique.trailerstock.data.local.dao.CategoriaVenta> {
        return ventaDao.obtenerVentasPorCategoria(inicio)
    }

    suspend fun getProductosMasRentables(inicio: Long, limit: Int = 5): List<com.manrique.trailerstock.data.local.dao.ProductoRentable> {
        return ventaDao.obtenerProductosMasRentables(inicio, limit)
    }

    suspend fun getGananciaPorCategoria(inicio: Long): List<com.manrique.trailerstock.data.local.dao.CategoriaGanancia> {
        return ventaDao.obtenerGananciaPorCategoria(inicio)
    }

    // ===== Helpers de fecha =====

    fun getInicioSemanaTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getInicioMesTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun get30DiasAtrasTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
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
