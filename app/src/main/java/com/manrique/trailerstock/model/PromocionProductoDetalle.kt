package com.manrique.trailerstock.model

import com.manrique.trailerstock.data.local.entities.Producto

/**
 * Representa un producto dentro de una promoción con su cantidad requerida
 */
data class PromocionProductoDetalle(
    val producto: Producto,
    val cantidadRequerida: Int
)
