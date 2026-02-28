package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.Producto
import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.Locale

/**
 * Diálogo para seleccionar productos y agregarlos al carrito
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelectorDialog(
    productosFlow: Flow<List<Producto>>,
    categorias: List<com.manrique.trailerstock.data.local.entities.Categoria>,
    precioTipo: String, // "LISTA" o "MAYORISTA"
    onDismiss: () -> Unit,
    onProductoSeleccionado: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val productos by productosFlow.collectAsState(initial = emptyList())
    
    val productosFiltrados = remember(productos, searchQuery) {
        if (searchQuery.isBlank()) {
            productos.filter { !it.eliminado }
        } else {
            productos.filter {
                !it.eliminado && it.nombre.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxHeight(0.8f)
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleccionar Producto",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Borrar", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de productos
                if (productosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay productos disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = productosFiltrados,
                            key = { it.id }
                        ) { producto ->
                            val categoria = categorias.find { it.id == producto.categoriaId }
                            ProductoSelectorItem(
                                producto = producto,
                                categoriaNombre = categoria?.nombre ?: "Sin categoría",
                                categoriaColor = categoria?.color,
                                precioTipo = precioTipo,
                                onClick = {
                                    onProductoSeleccionado(producto)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoSelectorItem(
    producto: Producto,
    categoriaNombre: String,
    categoriaColor: String?,
    precioTipo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val precio = if (precioTipo == "MAYORISTA") {
        producto.precioMayorista ?: producto.precioLista
    } else {
        producto.precioLista
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Chip de categoría
                if (categoriaNombre.isNotBlank()) {
                    val color = com.manrique.trailerstock.utils.ColorUtils.parseHexColor(categoriaColor)
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = categoriaNombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Stock: ${producto.stockActual}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (producto.stockActual <= producto.stockMinimo) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Text(
                text = NumberFormat.getCurrencyInstance(Locale("es", "AR")).format(precio),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
