package com.manrique.trailerstock.model

import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Promocion

/**
 * Modelo de UI que combina una promoción con sus productos y métodos de pago asociados
 */
data class PromocionConProductos(
    val promocion: Promocion,
    val productos: List<ProductoEnPromocion>,
    val metodosPago: List<MetodoPago> = emptyList()
)

/**
 * Modelo de UI que representa un producto dentro de una promoción,
 * con la cantidad requerida para que aplique el descuento
 */
data class ProductoEnPromocion(
    val producto: Producto,
    val cantidadRequerida: Int
)
