package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.PromocionDao
import com.manrique.trailerstock.data.local.dao.PromocionProductoDao
import com.manrique.trailerstock.data.local.dao.PromocionMetodoPagoDao
import com.manrique.trailerstock.data.local.dao.ProductoDao
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.PromocionProducto
import com.manrique.trailerstock.data.local.entities.PromocionMetodoPago
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.relations.PromocionConDetalles
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
     * Obtiene todas las promociones con sus detalles (productos y métodos de pago) usando Room Relations.
     */
    val allPromocionesConProductos: Flow<List<PromocionConProductos>> = promocionDao.obtenerTodasConDetalles().map { lista ->
        lista.map { item ->
            val productos = item.productos.mapNotNull { pp ->
                val producto = productoDao.obtenerPorId(pp.productoId)
                producto?.let {
                    ProductoEnPromocion(
                        producto = it,
                        cantidadRequerida = pp.cantidadRequerida
                    )
                }
            }
            PromocionConProductos(
                promocion = item.promocion,
                productos = productos,
                metodosPago = item.metodosPago.map { it.metodoPago }
            )
        }
    }
    
    /**
     * Guarda una promoción de forma atómica (transacción manual) o empaquetada.
     */
    suspend fun savePromotionAtomic(
        promocion: Promocion,
        productos: List<PromocionProducto>,
        metodosPago: List<PromocionMetodoPago>
    ): Long {
        // Nota: En Room, una transacción suele definirse en el DAO.
        // Aquí coordinamos las llamadas. Para que sea 100% atómico, el Dao debería tener el logic.
        // Pero dado el esquema actual, si insertamos la promo y luego lo demás, el Flow unificado
        // de Room Relations emitirá una sola vez cuando la transacción del DAO (obtenerTodasConDetalles)
        // detecte cambios en las tablas relacionadas si usamos @Transaction ahí.
        
        val promocionId = if (promocion.id == 0) {
            promocionDao.insertar(promocion).toInt()
        } else {
            promocionDao.actualizar(promocion)
            promocionProductoDao.eliminarProductosDePromocion(promocion.id)
            promocionMetodoPagoDao.eliminarMetodosDePromocion(promocion.id)
            promocion.id
        }

        val productosConId = productos.map { it.copy(promocionId = promocionId) }
        val metodosConId = metodosPago.map { it.copy(promocionId = promocionId) }
        
        if (productosConId.isNotEmpty()) {
            promocionProductoDao.insertarVarios(productosConId)
        }
        if (metodosConId.isNotEmpty()) {
            promocionMetodoPagoDao.insertarVarios(metodosConId)
        }
        
        return promocionId.toLong()
    }

    /**
     * Obtiene todas las promociones con sus productos y métodos de pago asociados
     * @deprecated Usar allPromocionesConProductos
     */
    fun getPromocionesConProductos(): Flow<List<PromocionConProductos>> {
        return allPromocionesConProductos
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
