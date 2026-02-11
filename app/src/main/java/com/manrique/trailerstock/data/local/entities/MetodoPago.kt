package com.manrique.trailerstock.data.local.entities

/**
 * Enum que representa los métodos de pago disponibles en el sistema.
 */
enum class MetodoPago(val displayName: String) {
    EFECTIVO("Efectivo"),
    TARJETA_DEBITO("Tarjeta de Débito"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    TRANSFERENCIA("Transferencia")
}
