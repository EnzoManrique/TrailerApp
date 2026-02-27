package com.manrique.trailerstock.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.Categoria

/**
 * Item de lista para mostrar una categoría.
 */
@Composable
fun CategoryListItem(
    categoria: Categoria,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Círculo de color
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = parseColor(categoria.color),
                            shape = CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Nombre
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Ícono
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Parsea un string de color hex a Color.
 * Si es null o inválido, retorna un color por defecto.
 */
private fun parseColor(colorHex: String?): Color {
    return try {
        if (colorHex.isNullOrBlank()) {
            Color(0xFF6200EE) // Color por defecto (Material Purple)
        } else {
            val hex = colorHex.removePrefix("#")
            Color(android.graphics.Color.parseColor("#$hex"))
        }
    } catch (e: Exception) {
        Color(0xFF6200EE)
    }
}
