package com.manrique.trailerstock.ui.screens.products

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
import androidx.compose.ui.unit.dp
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
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar producto"
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
                        message = uiState.error ?: "Error desconocido",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.productos.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    ProductsList(
                        productos = uiState.productos,
                        getCategoryName = { viewModel.getCategoryName(it) },
                        onProductClick = onEditProduct,
                        onRestockClick = { 
                            productToRestock = it
                            showRestockDialog = true
                        }
                    )
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
        title = { Text("Ingresar Stock") },
        text = {
            Column {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stock actual: ${producto.stockActual}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = cantidadText,
                    onValueChange = { if (it.length <= 5) cantidadText = it.filter { char -> char.isDigit() } },
                    label = { Text("Cantidad a ingresar") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (cantidad > 0) {
                    Text(
                        text = "Nuevo stock: ${producto.stockActual + cantidad}",
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
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ProductsList(
    productos: List<Producto>,
    getCategoryName: (Int) -> String,
    onProductClick: (Int) -> Unit,
    onRestockClick: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = productos,
            key = { it.id }
        ) { producto ->
            ProductListItem(
                producto = producto,
                categoryName = getCategoryName(producto.categoriaId),
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
            text = "No hay productos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Agrega tu primer producto pulsando el botón +",
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
            text = "Error",
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
