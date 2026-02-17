package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla principal de ventas (historial) con filtros
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onCreateSale: () -> Unit,
    onSaleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSale,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva venta"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de filtros
            SalesFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                filterMetodoPago = uiState.filterMetodoPago,
                onFilterMetodoPagoChange = { viewModel.setFilterMetodoPago(it) },
                filterTipoCliente = uiState.filterTipoCliente,
                onFilterTipoClienteChange = { viewModel.setFilterTipoCliente(it) },
                hasDateFilter = uiState.filterFechaInicio != null || uiState.filterFechaFin != null,
                onDateFilterClick = { showDatePicker = true },
                onClearFilters = { viewModel.clearFilters() }
            )

            Divider()

            // Contenido
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
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
                    uiState.ventasFiltradas.isEmpty() -> {
                        EmptyState(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        SalesList(
                            ventas = uiState.ventasFiltradas,
                            onSaleClick = { ventaId ->
                                viewModel.showSaleDetails(ventaId)
                            }
                        )
                    }
                }
            }
        }

        // Dialog de detalles
        if (uiState.showDetailDialog && uiState.selectedVenta != null) {
            SaleDetailDialog(
                venta = uiState.selectedVenta!!,
                detalles = uiState.selectedVentaDetalles,
                onDismiss = { viewModel.dismissDetailDialog() },
                onDeleteVenta = { venta -> viewModel.deleteSale(venta) }
            )
        }

        // Selector de rango de fechas
        if (showDatePicker) {
            val dateRangePickerState = rememberDateRangePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val startDate = dateRangePickerState.selectedStartDateMillis
                            val endDate = dateRangePickerState.selectedEndDateMillis
                            if (startDate != null && endDate != null) {
                                viewModel.setFilterFechaInicio(startDate)
                                viewModel.setFilterFechaFin(endDate)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = { Text("Seleccionar rango", modifier = Modifier.padding(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SalesList(
    ventas: List<com.manrique.trailerstock.data.local.entities.Venta>,
    onSaleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = ventas,
            key = { it.id }
        ) { venta ->
            SaleListItem(
                venta = venta,
                onClick = { onSaleClick(venta.id) }
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
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay ventas registradas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea tu primera venta pulsando el botón +",
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
