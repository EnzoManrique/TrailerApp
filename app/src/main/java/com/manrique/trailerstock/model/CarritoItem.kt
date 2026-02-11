package com.manrique.trailerstock.model

import com.manrique.trailerstock.data.local.entities.Producto

/**
 * Modelo de UI que representa un item en el carrito de compras
 */
data class CarritoItem(
    val producto: Producto,
    var cantidad: Int,
    val precioUnitario: Double,
    val promocionAplicada: PromocionConProductos? = null,
    val descuentoAplicado: Double = 0.0
) {
    /**
     * Calcula el subtotal considerando descuentos
     */
    val subtotal: Double
        get() = (precioUnitario * cantidad) - descuentoAplicado
}
