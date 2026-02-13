package com.manrique.trailerstock.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utilidades para formateo de fechas
 */
object DateUtils {
    
    /**
     * Formatea una fecha de manera relativa para mejor legibilidad
     * Ejemplos:
     * - "Hace 5 minutos"
     * - "Hoy a las 22:20"
     * - "Ayer a las 15:30"
     * - "11 feb 2026, 22:20"
     */
    fun formatRelativeDateTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val date = Date(timestamp)
        val diffMillis = now - timestamp
        
        val calendar = Calendar.getInstance()
        val today = calendar.clone() as Calendar
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        
        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = date
        
        return when {
            // Menos de 1 minuto
            diffMillis < TimeUnit.MINUTES.toMillis(1) -> {
                "Hace unos segundos"
            }
            // Menos de 1 hora
            diffMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
                "Hace ${minutes}m"
            }
            // Hoy (menos de 24 horas y mismo día)
            timestamp >= today.timeInMillis -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Hoy a las ${timeFormat.format(date)}"
            }
            // Ayer
            timestamp >= yesterday.timeInMillis -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Ayer a las ${timeFormat.format(date)}"
            }
            // Esta semana (últimos 7 días)
            diffMillis < TimeUnit.DAYS.toMillis(7) -> {
                val dayFormat = SimpleDateFormat("EEEE", Locale("es", "AR"))
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                "${dayFormat.format(date).capitalize()} a las ${timeFormat.format(date)}"
            }
            // Este año
            dateCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
                val dateFormat = SimpleDateFormat("d 'de' MMM, HH:mm", Locale("es", "AR"))
                dateFormat.format(date)
            }
            // Año diferente
            else -> {
                val dateFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "AR"))
                dateFormat.format(date)
            }
        }
    }
    
    /**
     * Formatea una fecha de manera compacta
     * Ejemplo: "11/02/2026"
     */
    fun formatShortDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Formatea una hora
     * Ejemplo: "22:20"
     */
    fun formatTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }
}
