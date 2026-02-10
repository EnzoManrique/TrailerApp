package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity que representa una promoción activa.
 */
@Entity(tableName = "promociones")
data class Promocion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "nombre_promo")
    val nombrePromo: String,
    
    @ColumnInfo(name = "porcentaje_descuento")
    val porcentajeDescuento: Double,
    
    @ColumnInfo(name = "esta_activa")
    val estaActiva: Boolean = true
) {
    /**
     * Calcula el precio final después de aplicar el descuento
     */
    fun calcularPrecioConDescuento(precioOriginal: Double): Double {
        return precioOriginal * (1 - porcentajeDescuento / 100)
    }
}
