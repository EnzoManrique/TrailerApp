package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla de crear venta (Punto de Venta / POS)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSaleScreen(
    viewModel: CreateSaleViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProductSelector by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Venta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Carrito de compras
            if (uiState.carritoItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Carrito vacío",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { showProductSelector = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar Productos")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.carritoItems,
                        key = { it.producto.id }
                    ) { item ->
                        CartItem(
                            item = item,
                            onIncrementar = {
                                viewModel.actualizarCantidad(
                                    item.producto.id,
                                    item.cantidad + 1
                                )
                            },
                            onDecrementar = {
                                viewModel.actualizarCantidad(
                                    item.producto.id,
                                    item.cantidad - 1
                                )
                            },
                            onEliminar = {
                                viewModel.eliminarProducto(item.producto.id)
                            }
                        )
                    }

                    // Botón agregar más productos
                    item {
                        OutlinedButton(
                            onClick = { showProductSelector = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar más productos")
                        }
                    }
                }
            }

            // Panel de pago (siempre visible)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selector tipo cliente
                    Text(
                        text = "Tipo de Cliente",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.tipoCliente == Venta.TIPO_LISTA,
                            onClick = { viewModel.cambiarTipoCliente(Venta.TIPO_LISTA) },
                            label = { Text("Lista") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.tipoCliente == Venta.TIPO_MAYORISTA,
                            onClick = { viewModel.cambiarTipoCliente(Venta.TIPO_MAYORISTA) },
                            label = { Text("Mayorista") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Divider()

                    // Selector método de pago
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.metodoPago == MetodoPago.EFECTIVO,
                                onClick = { viewModel.cambiarMetodoPago(MetodoPago.EFECTIVO) },
                                label = { Text(MetodoPago.EFECTIVO.displayName) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.metodoPago == MetodoPago.TARJETA_DEBITO,
                                onClick = { viewModel.cambiarMetodoPago(MetodoPago.TARJETA_DEBITO) },
                                label = { Text(MetodoPago.TARJETA_DEBITO.displayName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.metodoPago == MetodoPago.TARJETA_CREDITO,
                                onClick = { viewModel.cambiarMetodoPago(MetodoPago.TARJETA_CREDITO) },
                                label = { Text(MetodoPago.TARJETA_CREDITO.displayName) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.metodoPago == MetodoPago.TRANSFERENCIA,
                                onClick = { viewModel.cambiarMetodoPago(MetodoPago.TRANSFERENCIA) },
                                label = { Text(MetodoPago.TRANSFERENCIA.displayName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Divider()

                    // Resumen de totales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                                .format(uiState.subtotal),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (uiState.descuentoTotal > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Descuentos:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                "- " + NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                                    .format(uiState.descuentoTotal),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "TOTAL:",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                                .format(uiState.total),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón finalizar
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.carritoItems.isNotEmpty()
                    ) {
                        Text("Finalizar Venta", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // Diálogo selector de productos
    if (showProductSelector) {
        ProductSelectorDialog(
            productosFlow = viewModel.productosDisponibles,
            precioTipo = uiState.tipoCliente,
            onDismiss = { showProductSelector = false },
            onProductoSeleccionado = { producto ->
                viewModel.agregarProducto(producto)
            }
        )
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Venta") },
            text = {
                Text(
                    "¿Finalizar venta por ${NumberFormat.getCurrencyInstance(Locale("es", "AR")).format(uiState.total)}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.finalizarVenta(
                            onSuccess = onNavigateBack,
                            onError = { /* TODO: Mostrar error */ }
                        )
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
