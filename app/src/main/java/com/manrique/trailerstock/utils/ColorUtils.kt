package com.manrique.trailerstock.utils

import androidx.compose.ui.graphics.Color

/**
 * Utilidades para manejo de colores en la UI
 */
object ColorUtils {

    /**
     * Convierte un string hexadecimal (ej: "#FF5733" o "FF5733") a objeto Color.
     * Si falla, devuelve el color fallback proporcionado.
     */
    fun parseHexColor(hex: String?, fallback: Color = Color.Gray): Color {
        if (hex.isNullOrBlank()) return fallback
        
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = android.graphics.Color.parseColor("#$cleanHex")
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Devuelve un color con opacidad reducida para fondos suaves
     */
    fun getSoftBackground(color: Color, alpha: Float = 0.15f): Color {
        return color.copy(alpha = alpha)
    }
}
