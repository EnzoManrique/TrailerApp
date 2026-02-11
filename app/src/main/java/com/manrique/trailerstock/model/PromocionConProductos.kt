package com.manrique.trailerstock.model

import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Promocion

/**
 * Data class que combina una promoción con sus productos asociados.
 * Útil para mostrar en la UI.
 */
data class PromocionConProductos(
    val promocion: Promocion,
    val productos: List<ProductoEnPromocion>
)

/**
 * Representa un producto dentro de una promoción con su cantidad requerida.
 */
data class ProductoEnPromocion(
    val producto: Producto,
    val cantidadRequerida: Int
)
