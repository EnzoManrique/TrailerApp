package com.manrique.trailerstock.data.local.dao

import androidx.room.*
import com.manrique.trailerstock.data.local.entities.Promocion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Promoción con soporte de Coroutines.
 */
@Dao
interface PromocionDao {
    
    @Query("SELECT * FROM promociones WHERE eliminado = 0 ORDER BY nombre_promo ASC")
    fun obtenerTodas(): Flow<List<Promocion>>
    
    @Query("SELECT * FROM promociones WHERE eliminado = 0 AND esta_activa = 1 ORDER BY nombre_promo ASC")
    fun obtenerActivas(): Flow<List<Promocion>>
    
    @Query("SELECT * FROM promociones WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Promocion?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(promocion: Promocion): Long
    
    @Update
    suspend fun actualizar(promocion: Promocion)
    
    @Delete
    suspend fun eliminar(promocion: Promocion)
    
    @Query("UPDATE promociones SET eliminado = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)
    
    @Query("UPDATE promociones SET esta_activa = :activa WHERE id = :id")
    suspend fun cambiarEstado(id: Int, activa: Boolean)
    
    @Query("SELECT COUNT(*) FROM promociones WHERE eliminado = 0")
    suspend fun contar(): Int
}
