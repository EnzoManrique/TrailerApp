package com.manrique.trailerstock.data.local.dao

import androidx.room.*
import com.manrique.trailerstock.data.local.entities.PromocionProducto

/**
 * DAO para operaciones de PromocionProducto (relación muchos a muchos).
 */
@Dao
interface PromocionProductoDao {
    
    @Query("SELECT * FROM promocion_productos WHERE promocionId = :promocionId")
    suspend fun obtenerProductosPorPromocion(promocionId: Int): List<PromocionProducto>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(promocionProducto: PromocionProducto)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(promocionProductos: List<PromocionProducto>)
    
    @Query("DELETE FROM promocion_productos WHERE promocionId = :promocionId")
    suspend fun eliminarProductosDePromocion(promocionId: Int)
    
    @Delete
    suspend fun eliminar(promocionProducto: PromocionProducto)
}
