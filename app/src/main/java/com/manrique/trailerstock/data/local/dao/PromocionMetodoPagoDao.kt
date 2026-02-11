package com.manrique.trailerstock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.manrique.trailerstock.data.local.entities.PromocionMetodoPago

/**
 * DAO para gestionar la relación entre promociones y métodos de pago.
 */
@Dao
interface PromocionMetodoPagoDao {
    
    /**
     * Obtiene todos los métodos de pago asociados a una promoción.
     */
    @Query("SELECT * FROM promocion_metodos_pago WHERE promocionId = :promocionId")
    suspend fun obtenerMetodosPorPromocion(promocionId: Int): List<PromocionMetodoPago>
    
    /**
     * Inserta varios métodos de pago para una promoción.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(metodos: List<PromocionMetodoPago>)
    
    /**
     * Elimina todos los métodos de pago de una promoción.
     * Útil al editar una promoción.
     */
    @Query("DELETE FROM promocion_metodos_pago WHERE promocionId = :promocionId")
    suspend fun eliminarMetodosDePromocion(promocionId: Int)
}
