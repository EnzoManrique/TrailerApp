package com.manrique.trailerstock.data.local.dao

import androidx.room.*
import com.manrique.trailerstock.data.local.entities.Producto
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Producto con soporte de Coroutines.
 */
@Dao
interface ProductoDao {
    
    @Query("SELECT * FROM productos WHERE eliminado = 0 ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Producto?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<Producto>): List<Long>
    
    @Update
    suspend fun actualizar(producto: Producto)
    
    @Delete
    suspend fun eliminar(producto: Producto)
    
    @Query("UPDATE productos SET eliminado = 1 WHERE id = :id")
    suspend fun eliminarLogico(id: Int)
    
    @Query("SELECT * FROM productos WHERE stock_actual <= stock_minimo AND eliminado = 0")
    suspend fun obtenerStockBajo(): List<Producto>
    
    @Query("SELECT * FROM productos WHERE categoria_id = :categoriaId AND eliminado = 0")
    fun obtenerPorCategoria(categoriaId: Int): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos WHERE nombre LIKE '%' || :query || '%' AND eliminado = 0")
    fun buscarPorNombre(query: String): Flow<List<Producto>>
    
    @Query("SELECT COUNT(*) FROM productos WHERE eliminado = 0")
    suspend fun contarProductos(): Int
    
    @Query("SELECT COUNT(*) FROM productos WHERE stock_actual <= stock_minimo AND eliminado = 0")
    fun contarStockBajoFlow(): Flow<Int>

    @Query("SELECT SUM(stock_actual * precio_costo) FROM productos WHERE eliminado = 0")
    suspend fun obtenerValorInventario(): Double?
}
