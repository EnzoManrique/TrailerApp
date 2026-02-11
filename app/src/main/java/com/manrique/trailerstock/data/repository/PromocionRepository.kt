package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.PromocionDao
import com.manrique.trailerstock.data.local.dao.PromocionProductoDao
import com.manrique.trailerstock.data.local.dao.PromocionMetodoPagoDao
import com.manrique.trailerstock.data.local.dao.ProductoDao
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.PromocionProducto
import com.manrique.trailerstock.data.local.entities.PromocionMetodoPago
import com.manrique.trailerstock.model.PromocionConProductos
import com.manrique.trailerstock.model.ProductoEnPromocion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para el manejo de Promociones y sus relaciones.
 */
class PromocionRepository(
    private val promocionDao: PromocionDao,
    private val promocionProductoDao: PromocionProductoDao,
    private val promocionMetodoPagoDao: PromocionMetodoPagoDao,
    private val productoDao: ProductoDao
) {
    
    val allPromociones: Flow<List<Promocion>> = promocionDao.obtenerTodas()
    
    val promocionesActivas: Flow<List<Promocion>> = promocionDao.obtenerActivas()
    
    suspend fun insert(promocion: Promocion): Long {
        return promocionDao.insertar(promocion)
    }
    
    suspend fun update(promocion: Promocion) {
        promocionDao.actualizar(promocion)
    }
    
    suspend fun delete(promocion: Promocion) {
        promocionDao.eliminar(promocion)
    }
    
    suspend fun softDelete(promocion: Promocion) {
        promocionDao.softDelete(promocion.id)
    }
    
    suspend fun cambiarEstado(promocionId: Int, activa: Boolean) {
        promocionDao.cambiarEstado(promocionId, activa)
    }
    
    suspend fun getById(id: Int): Promocion? {
        return promocionDao.obtenerPorId(id)
    }
    
    suspend fun count(): Int {
        return promocionDao.contar()
    }
    
    /**
     * Obtiene todas las promociones con sus productos y métodos de pago asociados
     */
    fun getPromocionesConProductos(): Flow<List<PromocionConProductos>> {
        return allPromociones.map { promociones ->
            promociones.map { promocion ->
                val productosPromo = promocionProductoDao.obtenerProductosPorPromocion(promocion.id)
                val productos = productosPromo.map { pp ->
                    val producto = productoDao.obtenerPorId(pp.productoId)
                    ProductoEnPromocion(
                        producto = producto!!,
                        cantidadRequerida = pp.cantidadRequerida
                    )
                }
                val metodosPago = promocionMetodoPagoDao.obtenerMetodosPorPromocion(promocion.id)
                    .map { it.metodoPago }
                
                PromocionConProductos(
                    promocion = promocion,
                    productos = productos,
                    metodosPago = metodosPago
                )
            }
        }
    }
    
    // Métodos para productos de la promoción
    suspend fun getProductosDePromocion(promocionId: Int): List<PromocionProducto> {
        return promocionProductoDao.obtenerProductosPorPromocion(promocionId)
    }
    
    suspend fun insertProductoPromocion(promocionProducto: PromocionProducto) {
        promocionProductoDao.insertar(promocionProducto)
    }
    
    suspend fun insertProductosPromocion(promocionProductos: List<PromocionProducto>) {
        promocionProductoDao.insertarVarios(promocionProductos)
    }
    
    suspend fun eliminarProductosDePromocion(promocionId: Int) {
        promocionProductoDao.eliminarProductosDePromocion(promocionId)
    }
    
    /**
     * Obtiene los métodos de pago asociados a una promoción
     */
    suspend fun getMetodosPagoDePromocion(promocionId: Int): List<PromocionMetodoPago> {
        return promocionMetodoPagoDao.obtenerMetodosPorPromocion(promocionId)
    }
    
    /**
     * Inserta métodos de pago para una promoción
     */
    suspend fun insertMetodosPagoPromocion(metodos: List<PromocionMetodoPago>) {
        promocionMetodoPagoDao.insertarVarios(metodos)
    }
    
    /**
     * Elimina todos los métodos de pago de una promoción
     */
    suspend fun eliminarMetodosPagoDePromocion(promocionId: Int) {
        promocionMetodoPagoDao.eliminarMetodosDePromocion(promocionId)
    }
}
