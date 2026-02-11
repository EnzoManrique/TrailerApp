package com.manrique.trailerstock.data.local.dao

import androidx.room.*
import com.manrique.trailerstock.data.local.entities.Categoria
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Categoria con soporte de Coroutines.
 */
@Dao
interface CategoriaDao {
    
    @Query("SELECT * FROM categorias WHERE eliminado = 0 ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Categoria>>
    
    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Categoria?
    
    @Query("UPDATE categorias SET eliminado = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(categoria: Categoria): Long
    
    @Update
    suspend fun actualizar(categoria: Categoria)
    
    @Delete
    suspend fun eliminar(categoria: Categoria)
    
    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun contar(): Int
}
