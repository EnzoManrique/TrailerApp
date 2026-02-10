package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.VentaDao
import com.manrique.trailerstock.data.local.dao.VentaDetalleDao
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.local.entities.VentaDetalle
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository para operaciones de Venta con lógica de negocio.
 */
class VentaRepository(
    private val ventaDao: VentaDao,
    private val ventaDetalleDao: VentaDetalleDao
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
    
    suspend fun delete(venta: Venta) {
        ventaDao.eliminar(venta)
    }
    
    suspend fun getById(id: Int): Venta? {
        return ventaDao.obtenerPorId(id)
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
}
