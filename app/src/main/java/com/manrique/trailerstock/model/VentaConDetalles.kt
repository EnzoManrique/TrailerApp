package com.manrique.trailerstock.model

import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.local.entities.VentaDetalle

/**
 * Modelo de UI que combina una venta con sus detalles y productos
 */
data class VentaConDetalles(
    val venta: Venta,
    val detalles: List<VentaDetalleConProducto>
)

/**
 * Modelo que combina el detalle de venta con la info del producto
 */
data class VentaDetalleConProducto(
    val detalle: VentaDetalle,
    val producto: Producto
)
