package com.manrique.trailerstock.model

/**
 * Modelo para representar un producto estrella en las estadísticas.
 */
data class ProductoEstrella(
    val nombre: String,
    val cantidadVendida: Int,
    val porcentajeRotacion: Float // Para la barra de progreso
)
