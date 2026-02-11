package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.model.CarritoItem
import java.text.NumberFormat
import java.util.Locale

/**
 * Item individual del carrito de compras
 */
@Composable
fun CartItem(
    item: CarritoItem,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Fila principal: Nombre y eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${NumberFormat.getCurrencyInstance(Locale("es", "AR")).format(item.precioUnitario)} c/u",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Controles de cantidad y subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Controles +/-
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onDecrementar,
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("-", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Text(
                        text = "${item.cantidad}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.widthIn(min = 30.dp)
                    )
                    
                    FilledTonalButton(
                        onClick = onIncrementar,
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                // Subtotal
                Column(horizontalAlignment = Alignment.End) {
                    if (item.descuentoAplicado > 0) {
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                                .format(item.precioUnitario * item.cantidad),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                            .format(item.subtotal),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Badge de promoción si aplica
            if (item.promocionAplicada != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "🎁 ${item.promocionAplicada.promocion.nombrePromo}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }
    }
}
