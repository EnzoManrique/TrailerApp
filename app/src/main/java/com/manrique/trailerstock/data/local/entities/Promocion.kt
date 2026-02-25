package com.manrique.trailerstock.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.manrique.trailerstock.data.local.entities.TipoDescuento
import com.manrique.trailerstock.utils.DateUtils

/**
 * Entity que representa una promoción.
 */
@Entity(tableName = "promociones")
data class Promocion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "nombre_promo")
    val nombrePromo: String,
    
    @ColumnInfo(name = "descripcion")
    val descripcion: String? = null,
    
    @ColumnInfo(name = "tipo_descuento")
    val tipoDescuento: TipoDescuento = TipoDescuento.PORCENTAJE,
    
    @ColumnInfo(name = "porcentaje_descuento")
    val porcentajeDescuento: Double = 0.0,
    
    @ColumnInfo(name = "monto_descuento")
    val montoDescuento: Double = 0.0,
    
    @ColumnInfo(name = "fecha_inicio")
    val fechaInicio: Long? = null,  // Timestamp en milisegundos
    
    @ColumnInfo(name = "fecha_fin")
    val fechaFin: Long? = null,  // Timestamp en milisegundos
    
    @ColumnInfo(name = "esta_activa")
    val estaActiva: Boolean = true,
    
    @ColumnInfo(name = "eliminado")
    val eliminado: Boolean = false
) {
    /**
     * Calcula el precio final después de aplicar el descuento
     */
    fun calcularPrecioConDescuento(precioOriginal: Double): Double {
        return when (tipoDescuento) {
            TipoDescuento.PORCENTAJE -> {
                precioOriginal * (1 - porcentajeDescuento / 100)
            }
            TipoDescuento.MONTO_FIJO -> {
                (precioOriginal - montoDescuento).coerceAtLeast(0.0)
            }
        }
    }
    
    /**
     * Verifica si la promoción está vigente según las fechas
     */
    fun estaVigente(timestamp: Long = System.currentTimeMillis()): Boolean {
        val despuesDeFechaInicio = fechaInicio?.let { timestamp >= DateUtils.getStartOfDay(it) } ?: true
        val antesDeFechaFin = fechaFin?.let { timestamp <= DateUtils.getEndOfDay(it) } ?: true
        return despuesDeFechaInicio && antesDeFechaFin
    }
    
    /**
     * Verifica si la promoción está activa y vigente
     */
    fun esAplicable(): Boolean {
        return estaActiva && !eliminado && estaVigente()
    }
}

