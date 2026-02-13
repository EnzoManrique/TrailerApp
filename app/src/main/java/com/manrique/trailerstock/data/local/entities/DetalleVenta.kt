package com.manrique.trailerstock.data.local.entities

/**
 * Data class auxiliar para mostrar detalles de venta con toda la información necesaria
 * Usado en la UI para mostrar productos vendidos con sus nombres
 */
data class DetalleVenta(
    val id: Int,
    val ventaId: Int,
    val productoId: Int,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val descuento: Double = 0.0
)
