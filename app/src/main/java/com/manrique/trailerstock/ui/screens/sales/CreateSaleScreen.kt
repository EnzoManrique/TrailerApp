package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.manrique.trailerstock.R
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import android.content.Intent
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.model.CarritoItem
import com.manrique.trailerstock.utils.ExportManager
import java.io.File
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

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                            text = stringResource(R.string.msg_cart_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { showProductSelector = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.label_add_products))
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
                            Text(stringResource(R.string.label_add_more_products))
                        }
                    }
                }
            }

            // Panel de pago (siempre visible)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Selector tipo cliente
                    Text(
                        text = stringResource(R.string.sale_client_type),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.tipoCliente == Venta.TIPO_LISTA,
                            onClick = { viewModel.cambiarTipoCliente(Venta.TIPO_LISTA) },
                            label = { 
                                Text(
                                    text = stringResource(R.string.sale_client_type_list),
                                    fontWeight = if (uiState.tipoCliente == Venta.TIPO_LISTA) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.tipoCliente == Venta.TIPO_LISTA,
                                borderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 2.dp
                            )
                        )
                        FilterChip(
                            selected = uiState.tipoCliente == Venta.TIPO_MAYORISTA,
                            onClick = { viewModel.cambiarTipoCliente(Venta.TIPO_MAYORISTA) },
                            label = { 
                                Text(
                                    text = stringResource(R.string.sale_client_type_wholesale),
                                    fontWeight = if (uiState.tipoCliente == Venta.TIPO_MAYORISTA) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.tipoCliente == Venta.TIPO_MAYORISTA,
                                borderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 2.dp
                            )
                        )
                    }

                    if (!uiState.isQuoteMode) {
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
                                    label = { 
                                        Text(
                                            text = MetodoPago.EFECTIVO.displayName,
                                            fontWeight = if (uiState.metodoPago == MetodoPago.EFECTIVO) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = uiState.metodoPago == MetodoPago.EFECTIVO,
                                        borderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 2.dp
                                    )
                                )
                                FilterChip(
                                    selected = uiState.metodoPago == MetodoPago.TARJETA_DEBITO,
                                    onClick = { viewModel.cambiarMetodoPago(MetodoPago.TARJETA_DEBITO) },
                                    label = { 
                                        Text(
                                            text = MetodoPago.TARJETA_DEBITO.displayName,
                                            fontWeight = if (uiState.metodoPago == MetodoPago.TARJETA_DEBITO) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = uiState.metodoPago == MetodoPago.TARJETA_DEBITO,
                                        borderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 2.dp
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.metodoPago == MetodoPago.TARJETA_CREDITO,
                                    onClick = { viewModel.cambiarMetodoPago(MetodoPago.TARJETA_CREDITO) },
                                    label = { 
                                        Text(
                                            text = MetodoPago.TARJETA_CREDITO.displayName,
                                            fontWeight = if (uiState.metodoPago == MetodoPago.TARJETA_CREDITO) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = uiState.metodoPago == MetodoPago.TARJETA_CREDITO,
                                        borderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 2.dp
                                    )
                                )
                                FilterChip(
                                    selected = uiState.metodoPago == MetodoPago.TRANSFERENCIA,
                                    onClick = { viewModel.cambiarMetodoPago(MetodoPago.TRANSFERENCIA) },
                                    label = { 
                                        Text(
                                            text = MetodoPago.TRANSFERENCIA.displayName,
                                            fontWeight = if (uiState.metodoPago == MetodoPago.TRANSFERENCIA) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = uiState.metodoPago == MetodoPago.TRANSFERENCIA,
                                        borderColor = MaterialTheme.colorScheme.primary,
                                        borderWidth = 2.dp
                                    )
                                )
                            }
                        }
                    }

                    Divider()

                    // Resumen de totales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.label_subtotal), style = MaterialTheme.typography.bodyLarge)
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
                                stringResource(R.string.label_discounts),
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
                            stringResource(R.string.label_total_caps),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                                .format(uiState.total),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón finalizar / generar presupuesto
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = { 
                            if (uiState.isQuoteMode) {
                                viewModel.generarPresupuesto(
                                    onSuccess = { file ->
                                        // Compartir PDF
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Compartir Presupuesto"))
                                    },
                                    onError = { /* TODO: Error */ }
                                )
                            } else {
                                showConfirmDialog = true 
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.carritoItems.isNotEmpty()
                    ) {
                        Text(
                            text = if (uiState.isQuoteMode) "Generar Presupuesto PDF" 
                                   else stringResource(R.string.finish_sale), 
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (showProductSelector) {
        ProductSelectorDialog(
            productosFlow = viewModel.productosDisponibles,
            categorias = uiState.categorias,
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
            title = { Text(stringResource(R.string.label_confirm_sale_title)) },
            text = {
                Text(
                    stringResource(R.string.label_confirm_sale_msg, NumberFormat.getCurrencyInstance(Locale("es", "AR")).format(uiState.total))
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
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
