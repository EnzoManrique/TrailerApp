package com.manrique.trailerstock.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Done
import com.manrique.trailerstock.R
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.manrique.trailerstock.data.local.entities.Producto

/**
 * Pantalla principal de productos.
 * 
 * Muestra la lista de productos en el inventario con opción de agregar nuevos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var productToRestock by remember { mutableStateOf<Producto?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_product)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.msg_error_unknown),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // Contenido principal en un Box para permitir superposición
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Lista de productos (va debajo)
                        ProductsList(
                            productos = uiState.productosFiltrados,
                            getCategory = { viewModel.getCategory(it) },
                            onProductClick = onEditProduct,
                            onRestockClick = { 
                                productToRestock = it
                                showRestockDialog = true
                            },
                            modifier = Modifier.fillMaxSize(),
                            headerPadding = 56.dp // Espacio para los chips flotantes
                        )

                        // Chips de filtrado flotantes con efecto glassmorphism
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            shadowElevation = 8.dp
                        ) {
                            Column {
                                CategoryFilters(
                                    categorias = uiState.categorias,
                                    selectedId = uiState.filterCategoryId,
                                    onSelect = { viewModel.setFilterCategory(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                // Divisor sutil opcional para separar el header del contenido que scrollea
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestockDialog && productToRestock != null) {
        RestockDialog(
            producto = productToRestock!!,
            onDismiss = {
                showRestockDialog = false
                productToRestock = null
            },
            onConfirm = { cantidad ->
                viewModel.restockProduct(productToRestock!!, cantidad)
                showRestockDialog = false
                productToRestock = null
            }
        )
    }
}

@Composable
private fun RestockDialog(
    producto: Producto,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    val cantidad = cantidadText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_restock)) },
        text = {
            Column {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.label_current_stock_fmt, producto.stockActual),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = cantidadText,
                    onValueChange = { if (it.length <= 5) cantidadText = it.filter { char -> char.isDigit() } },
                    label = { Text(stringResource(R.string.label_quantity_to_restock)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                if (cantidad > 0) {
                    Text(
                        text = stringResource(R.string.label_new_stock_fmt, producto.stockActual + cantidad),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(cantidad) },
                enabled = cantidad > 0
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ProductsList(
    productos: List<Producto>,
    getCategory: (Int) -> com.manrique.trailerstock.data.local.entities.Categoria?,
    onProductClick: (Int) -> Unit,
    onRestockClick: (Producto) -> Unit,
    modifier: Modifier = Modifier,
    headerPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, 
            top = 16.dp + headerPadding, 
            end = 16.dp, 
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = productos,
            key = { it.id }
        ) { producto ->
            val categoria = getCategory(producto.categoriaId)
            ProductListItem(
                producto = producto,
                categoryName = categoria?.nombre ?: "Sin categoría",
                categoryColor = categoria?.color,
                onClick = { onProductClick(producto.id) },
                onRestockClick = { onRestockClick(producto) }
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_empty_products),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.msg_empty_products_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.msg_error_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
@Composable
private fun CategoryFilters(
    categorias: List<com.manrique.trailerstock.data.local.entities.Categoria>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text("Todos") },
                leadingIcon = if (selectedId == null) {
                    { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null
            )
        }
        
        items(categorias) { categoria ->
            val color = com.manrique.trailerstock.utils.ColorUtils.parseHexColor(categoria.color)
            FilterChip(
                selected = selectedId == categoria.id,
                onClick = { onSelect(categoria.id) },
                label = { Text(categoria.nombre) },
                leadingIcon = if (selectedId == categoria.id) {
                    { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    { 
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(color)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedId == categoria.id,
                    borderColor = if (selectedId == categoria.id) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }
    }
}
