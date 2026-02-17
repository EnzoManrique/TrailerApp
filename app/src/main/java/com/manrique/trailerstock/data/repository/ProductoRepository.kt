package com.manrique.trailerstock.data.repository

import com.manrique.trailerstock.data.local.dao.ProductoDao
import com.manrique.trailerstock.data.local.entities.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository que abstrae el acceso a datos de Producto.
 * Implementa el patrón Repository para separar la lógica de datos de la UI.
 */
class ProductoRepository(private val productoDao: ProductoDao) {
    
    // Flow para observar cambios reactivamente
    val allProductos: Flow<List<Producto>> = productoDao.obtenerTodos()
    
    // Productos activos (no eliminados)
    val productosActivos: Flow<List<Producto>> = productoDao.obtenerTodos()
    
    // Productos con stock bajo reactivo
    val contarStockBajo: Flow<Int> = productoDao.contarStockBajoFlow()
    
    // Operaciones CRUD con suspend (Coroutines)
    suspend fun insert(producto: Producto): Long {
        return productoDao.insertar(producto)
    }
    
    suspend fun insertAll(productos: List<Producto>): List<Long> {
        return productoDao.insertarTodos(productos)
    }
    
    suspend fun update(producto: Producto) {
        productoDao.actualizar(producto)
    }
    
    suspend fun delete(producto: Producto) {
        productoDao.eliminar(producto)
    }
    
    suspend fun softDelete(producto: Producto) {
        val updated = producto.copy(eliminado = true)
        productoDao.actualizar(updated)
    }
    
    suspend fun getById(id: Int): Producto? {
        return productoDao.obtenerPorId(id)
    }
    
    /**
     * Descuenta stock de un producto al realizar una venta
     */
    suspend fun descontarStock(productoId: Int, cantidad: Int) {
        val producto = getById(productoId) ?: return
        val nuevoStock = (producto.stockActual - cantidad).coerceAtLeast(0)
        update(producto.copy(stockActual = nuevoStock))
    }
    
    suspend fun getLowStockProducts(): List<Producto> {
        return productoDao.obtenerStockBajo()
    }
    
    fun getByCategory(categoriaId: Int): Flow<List<Producto>> {
        return productoDao.obtenerPorCategoria(categoriaId)
    }
    
    fun searchByName(query: String): Flow<List<Producto>> {
        return productoDao.buscarPorNombre(query)
    }
    
    suspend fun countProducts(): Int {
        return productoDao.contarProductos()
    }
    
    /**
     * Incrementa el stock de un producto (Restock)
     */
    suspend fun restock(productoId: Int, cantidad: Int) {
        val producto = getById(productoId) ?: return
        val nuevoStock = producto.stockActual + cantidad
        update(producto.copy(stockActual = nuevoStock))
    }

    /**
     * Actualiza el stock de un producto después de una venta
     */
    suspend fun updateStock(productoId: Int, cantidadVendida: Int) {
        val producto = getById(productoId) ?: return
        val nuevoStock = producto.stockActual - cantidadVendida
        val updated = producto.copy(stockActual = nuevoStock)
        update(updated)
    }
}
