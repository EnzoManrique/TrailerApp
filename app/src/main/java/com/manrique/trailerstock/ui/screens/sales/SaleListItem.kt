package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.EstadoVenta
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.utils.DateUtils
import java.text.NumberFormat
import java.util.Locale

/**
 * Item de venta para la lista con diseño mejorado
 */
@Composable
fun SaleListItem(
    venta: Venta,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Encabezado: número y total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Número de venta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = venta.numeroVenta ?: "Venta #${venta.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Total destacado
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                            .format(venta.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (venta.estado == EstadoVenta.ANULADA)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary,
                        textDecoration = if (venta.estado == EstadoVenta.ANULADA)
                            androidx.compose.ui.text.style.TextDecoration.LineThrough
                        else
                            null
                    )
                    
                    if (venta.estado == EstadoVenta.ANULADA) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Text(
                                text = "ANULADA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Fecha con formato relativo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = DateUtils.formatRelativeDateTime(venta.fecha),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Info compacta: método de pago y tipo cliente
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Método de pago con icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = getPaymentIcon(venta.metodoPago),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatPaymentMethod(venta.metodoPago),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Separador
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                // Tipo cliente con icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatClientType(venta.tipoCliente),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notas si existen
            if (!venta.notas.isNullOrBlank()) {
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Text(
                    text = venta.notas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Obtiene el icono correspondiente al método de pago
 */
private fun getPaymentIcon(metodoPago: MetodoPago): ImageVector {
    return when (metodoPago) {
        MetodoPago.EFECTIVO -> Icons.Default.Money
        MetodoPago.TARJETA_DEBITO -> Icons.Default.CreditCard
        MetodoPago.TARJETA_CREDITO -> Icons.Default.CreditCard
        MetodoPago.TRANSFERENCIA -> Icons.Default.AccountBalance
    }
}
/**
 * Formatea el nombre del método de pago para mejor legibilidad
 */
private fun formatPaymentMethod(metodoPago: MetodoPago): String {
    return when (metodoPago) {
        MetodoPago.EFECTIVO -> "Efectivo"
        MetodoPago.TARJETA_DEBITO -> "Tarjeta Débito"
        MetodoPago.TARJETA_CREDITO -> "Tarjeta Crédito"
        MetodoPago.TRANSFERENCIA -> "Transferencia"
    }
}

/**
 * Formatea el tipo de cliente
 */
private fun formatClientType(tipoCliente: String) = when (tipoCliente.uppercase()) {
    "LISTA" -> "Lista"
    "MAYORISTA" -> "Mayorista"
    else -> tipoCliente.capitalize()
}
