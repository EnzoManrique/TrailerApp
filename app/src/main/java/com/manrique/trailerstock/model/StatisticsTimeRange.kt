package com.manrique.trailerstock.model

/**
 * Rangos de tiempo para las estadísticas y filtrado
 */
enum class StatisticsTimeRange(val label: String) {
    HOY("Hoy"),
    SEMANA("Esta Semana"),
    MES("Este Mes")
}
