package com.manrique.trailerstock.ui.screens.promotions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.TipoDescuento
import com.manrique.trailerstock.model.PromocionConProductos
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable que muestra un item de promoción en la lista
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionListItem(
    promocionConProductos: PromocionConProductos,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val promocion = promocionConProductos.promocion
    val metodosPago = promocionConProductos.metodosPago
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado: nombre y switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = promocion.nombrePromo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Switch de estado
                Switch(
                    checked = promocion.estaActiva,
                    onCheckedChange = { onToggleStatus() }
                )
            }

            // Descripción si existe
            if (!promocion.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = promocion.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Detalles del descuento
            Text(
                text = when (promocion.tipoDescuento) {
                    TipoDescuento.PORCENTAJE -> "${promocion.porcentajeDescuento.toInt()}% de descuento"
                    TipoDescuento.MONTO_FIJO -> "-$${promocion.montoDescuento.toInt()} de descuento"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Fechas si existen
            if (promocion.fechaInicio != null || promocion.fechaFin != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaTexto = buildString {
                    if (promocion.fechaInicio != null) {
                        append("Desde ${dateFormat.format(Date(promocion.fechaInicio))}")
                    }
                    if (promocion.fechaFin != null) {
                        if (promocion.fechaInicio != null) append(" ")
                        append("hasta ${dateFormat.format(Date(promocion.fechaFin))}")
                    }
                }
                Text(
                    text = fechaTexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Indicador si está vigente
            if (!promocion.estaVigente() && promocion.estaActiva) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Fuera de vigencia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Badge de métodos de pago si hay restricción
            if (metodosPago.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "💳 " + metodosPago.joinToString(", ") { it.displayName },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}
