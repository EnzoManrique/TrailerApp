package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.PromocionDao
import com.manrique.trailerstock.data.local.dao.PromocionProductoDao
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.PromocionProducto
import kotlinx.coroutines.flow.Flow

/**
 * Repository para operaciones de Promoción.
 */
class PromocionRepository(
    private val promocionDao: PromocionDao,
    private val promocionProductoDao: PromocionProductoDao
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
}
