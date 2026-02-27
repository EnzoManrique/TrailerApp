package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Description
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.manrique.trailerstock.R
import androidx.compose.ui.unit.dp

import com.manrique.trailerstock.model.StatisticsTimeRange

/**
 * Pantalla principal de ventas (historial) con filtros
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onCreateSale: () -> Unit,
    onCreateQuote: () -> Unit,
    onSaleClick: (Int) -> Unit,
    initialRange: String? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Aplicar filtro inicial si se navega desde el dashboard
    LaunchedEffect(initialRange) {
        initialRange?.let { rangeName ->
            try {
                val range = StatisticsTimeRange.valueOf(rangeName)
                viewModel.setFilterRange(range)
            } catch (e: Exception) {
                // Rango inválido, ignorar
            }
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Presupuesto (Secundario - Derecha)
                FloatingActionButton(
                    onClick = onCreateQuote,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Generar Presupuesto"
                    )
                }
                
                // Botón Nueva Venta (Principal - Derecha)
                FloatingActionButton(
                    onClick = onCreateSale,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.label_new_sale_title)
                    )
                }
            }
        }
    ) { paddingValues ->
        // Contenido principal en un Box para permitir superposición
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido de la lista (va debajo)
            Box(
                modifier = Modifier.fillMaxSize()
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
                    uiState.ventasFiltradas.isEmpty() -> {
                        EmptyState(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 64.dp)
                        )
                    }
                    else -> {
                        SalesList(
                            ventas = uiState.ventasFiltradas,
                            onSaleClick = { ventaId ->
                                viewModel.showSaleDetails(ventaId)
                            },
                            modifier = Modifier.fillMaxSize(),
                            headerPadding = 56.dp
                        )
                    }
                }
            }

            // Barra de filtros flotante con efecto cristal (Glassmorphism)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shadowElevation = 8.dp
            ) {
                Column {
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
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }

        // Dialog de detalles
        if (uiState.showDetailDialog && uiState.selectedVenta != null) {
            SaleDetailDialog(
                venta = uiState.selectedVenta!!,
                detalles = uiState.selectedVentaDetalles,
                onDismiss = { viewModel.dismissDetailDialog() },
                onDeleteVenta = { venta, restaurar ->
                    viewModel.deleteSale(venta, restaurar)
                }
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
                        Text(stringResource(R.string.action_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = { Text(stringResource(R.string.label_select_range), modifier = Modifier.padding(16.dp)) },
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
            text = stringResource(R.string.msg_empty_sales),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.msg_empty_sales_hint),
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
