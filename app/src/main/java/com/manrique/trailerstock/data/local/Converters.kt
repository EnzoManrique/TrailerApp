package com.manrique.trailerstock.data.local

import androidx.room.TypeConverter
import com.manrique.trailerstock.data.local.entities.EstadoVenta
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.TipoDescuento

/**
 * Conversores para tipos personalizados en Room.
 */
class Converters {
    @TypeConverter
    fun fromMetodoPago(value: MetodoPago): String = value.name

    @TypeConverter
    fun toMetodoPago(value: String): MetodoPago = try {
        MetodoPago.valueOf(value)
    } catch (e: Exception) {
        MetodoPago.EFECTIVO
    }

    @TypeConverter
    fun fromEstadoVenta(value: EstadoVenta): String = value.name

    @TypeConverter
    fun toEstadoVenta(value: String): EstadoVenta = try {
        EstadoVenta.valueOf(value)
    } catch (e: Exception) {
        EstadoVenta.ACTIVA
    }

    @TypeConverter
    fun fromTipoDescuento(value: TipoDescuento): String = value.name

    @TypeConverter
    fun toTipoDescuento(value: String): TipoDescuento = try {
        TipoDescuento.valueOf(value)
    } catch (e: Exception) {
        TipoDescuento.PORCENTAJE
    }
}
