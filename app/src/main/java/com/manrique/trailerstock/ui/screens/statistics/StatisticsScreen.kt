package com.manrique.trailerstock.ui.screens.statistics

import com.manrique.trailerstock.model.StatisticsTimeRange

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.manrique.trailerstock.R
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

/**
 * Pantalla de estadísticas con diseño Bento Grid.
 * 
 * Muestra tarjetas con métricas clave del negocio:
 * - Ventas de hoy
 * - Total de productos
 * - Productos con stock bajo
 * - Total de ventas históricas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateToProducts: (Boolean) -> Unit,
    onNavigateToSales: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var showLowStockBottomSheet by remember { mutableStateOf(false) }
    var showSalesBottomSheet by remember { mutableStateOf(false) }
    var showEarningsBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val salesSheetState = rememberModalBottomSheetState()
    val earningsSheetState = rememberModalBottomSheetState()

    // Manejo de refresco por gesto
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }

    // Sincronizar estado de carga con pullToRefresh
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            pullToRefreshState.endRefresh()
        }
    }

    // Manejo de navegación desde eventos del ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event: StatisticsUiEvent ->
            when (event) {
                is StatisticsUiEvent.NavigateToSales -> onNavigateToSales(event.range?.name)
            }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            // Grilla de estadísticas (va debajo)
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && !pullToRefreshState.isRefreshing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        ErrorMessage(
                            message = uiState.error ?: stringResource(R.string.msg_error_unknown),
                            onRetry = { viewModel.refresh() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        StatisticsGrid(
                            uiState = uiState,
                            onLowStockClick = { showLowStockBottomSheet = true },
                            onSalesClick = { showSalesBottomSheet = true },
                            onEarningsClick = { showEarningsBottomSheet = true },
                            onNavigateToProducts = onNavigateToProducts,
                            modifier = Modifier.fillMaxSize(),
                            headerPadding = 64.dp // Espacio para el header flotante
                        )
                    }
                }
            }

            // Cabecera flotante con efecto cristal (Glassmorphism)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f), // Más opaco para simular mejor el blur
                shadowElevation = 8.dp
            ) {
                Column {
                    TimeRangeSelection(
                        selectedRange = uiState.selectedRange,
                        onRangeSelected = { viewModel.onTimeRangeSelected(it) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    )
                    // Línea sutil de separación con brillo glass
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    if (showLowStockBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLowStockBottomSheet = false },
            sheetState = sheetState
        ) {
            LowStockListSheet(
                productos = uiState.listaProductosStockBajo,
                onDismiss = { showLowStockBottomSheet = false }
            )
        }
    }

    if (showSalesBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSalesBottomSheet = false },
            sheetState = salesSheetState
        ) {
            SalesListSheet(
                ventas = uiState.listaVentasPeriodo,
                onDismiss = { showSalesBottomSheet = false }
            )
        }
    }

    if (showEarningsBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEarningsBottomSheet = false },
            sheetState = earningsSheetState
        ) {
            EarningsDetailsSheet(
                uiState = uiState,
                onDismiss = { showEarningsBottomSheet = false }
            )
        }
    }
}

@Composable
private fun SalesListSheet(
    ventas: List<com.manrique.trailerstock.data.local.entities.Venta>,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .heightIn(max = 500.dp)
    ) {
        Text(
            text = stringResource(R.string.menu_sales),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        if (ventas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.msg_no_recent_sales),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ventas) { venta ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Venta #${venta.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(venta.fecha)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = venta.metodoPago.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "AR")).format(venta.total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = venta.estado.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (venta.estado == com.manrique.trailerstock.data.local.entities.EstadoVenta.ACTIVA) 
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.action_accept))
        }
    }
}

@Composable
private fun LowStockListSheet(
    productos: List<com.manrique.trailerstock.data.local.entities.Producto>,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .heightIn(max = 500.dp)
    ) {
        Text(
            text = stringResource(R.string.label_critical_products),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        if (productos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.msg_no_low_stock),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos) { producto ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.label_min_stock_fmt, producto.stockMinimo),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${producto.stockActual}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.label_stock),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.action_accept))
        }
    }
}

@Composable
private fun TimeRangeSelection(
    selectedRange: StatisticsTimeRange,
    onRangeSelected: (StatisticsTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (range in StatisticsTimeRange.values()) {
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun StatisticsGrid(
    uiState: StatisticsUiState,
    onLowStockClick: () -> Unit,
    onSalesClick: () -> Unit,
    onEarningsClick: () -> Unit,
    onNavigateToProducts: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    headerPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val prefs = uiState.preferences ?: return

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp, 
            top = 16.dp + headerPadding, 
            end = 16.dp, 
            bottom = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Tarjeta Principal (Ancho completo): Ganancias del Periodo
        if (prefs.showEarnings) {
            item(span = { GridItemSpan(2) }) {
                MainMetricsCard(
                    valor = uiState.getGananciaPeriodoFormatted(),
                    titulo = "${stringResource(R.string.label_estimated_earnings)} (${uiState.selectedRange.label})",
                    subtexto = stringResource(R.string.label_num_sales_fmt, uiState.ventasPeriodo),
                    onClick = onEarningsClick,
                    elevation = 10.dp
                )
            }
        }

        // Tarjetas Secundarias (Medio ancho)
        if (prefs.showSales) {
            item {
                StatisticCardItem(
                    title = stringResource(R.string.menu_sales),
                    value = uiState.getTotalVentasFormatted(),
                    subtitle = stringResource(R.string.label_num_operaciones_fmt, uiState.ventasPeriodo),
                    icon = Icons.Outlined.AttachMoney,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onSalesClick
                )
            }
        }

        if (prefs.showLowStock) {
            item {
                StatisticCardItem(
                    title = stringResource(R.string.label_low_stock),
                    value = "${uiState.productosStockBajo}",
                    subtitle = stringResource(R.string.label_critical_products),
                    icon = Icons.Outlined.WarningAmber,
                    color = MaterialTheme.colorScheme.error,
                    onClick = onLowStockClick
                )
            }
        }

        if (prefs.showTicket) {
            item {
                StatisticCardItem(
                    title = stringResource(R.string.label_average_ticket),
                    value = uiState.getTicketPromedioFormatted(),
                    subtitle = stringResource(R.string.label_by_sale),
                    icon = Icons.Outlined.Analytics,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (prefs.showCapital) {
            item {
                StatisticCardItem(
                    title = stringResource(R.string.label_capital_stock),
                    value = uiState.getValorInventarioFormatted(),
                    subtitle = stringResource(R.string.label_value_in_stock),
                    icon = Icons.Outlined.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // --- Nuevas Estadísticas ---

        // Ventas por Categoría (Ancho completo)
        if (prefs.showSalesByCategory && uiState.ventasPorCategoria.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                CategorySalesCard(
                    ventas = uiState.ventasPorCategoria,
                    categorias = uiState.categoriasList
                )
            }
        }

        // Productos más Rentables (Ancho completo)
        if (prefs.showMostProfitable && uiState.productosMasRentables.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                ProfitableProductsCard(productos = uiState.productosMasRentables)
            }
        }

        // Productos Estancados (Ancho completo)
        if (prefs.showStagnantProducts && uiState.productosEstancados.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                StagnantProductsCard(productos = uiState.productosEstancados)
            }
        }

        // Tarjeta de Productos Estrella (Ancho completo)
        if (prefs.showTopProducts) {
            item(span = { GridItemSpan(2) }) {
                TopProductsCard(productos = uiState.productosEstrella)
            }
        }
    }
}

@Composable
private fun CategorySalesCard(
    ventas: List<com.manrique.trailerstock.data.local.dao.CategoriaVenta>,
    categorias: List<com.manrique.trailerstock.data.local.entities.Categoria>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.label_sales_by_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            ventas.forEach { venta ->
                val categoriaColor = categorias.find { it.nombre == venta.nombre }?.color
                val textColor = if (categoriaColor != null) {
                    com.manrique.trailerstock.utils.ColorUtils.parseHexColor(categoriaColor)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = venta.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "AR")).format(venta.totalVendido),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfitableProductsCard(
    productos: List<com.manrique.trailerstock.data.local.dao.ProductoRentable>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.label_most_profitable),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            productos.forEach { producto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = producto.nombre, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "+ ${java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "AR")).format(producto.gananciaTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StagnantProductsCard(
    productos: List<com.manrique.trailerstock.data.local.entities.Producto>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_stagnant_products),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Badge de cantidad total
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${productos.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.msg_stagnant_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (productos.isEmpty()) {
                Text(
                    text = "No hay productos estancados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                productos.take(4).forEachIndexed { index, producto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${producto.stockActual} u.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (index < productos.take(4).size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                
                if (productos.size > 4) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { /* Podría navegar a una lista completa */ },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.msg_and_more_fmt, productos.size - 4),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainMetricsCard(
    valor: String,
    titulo: String,
    subtexto: String,
    onClick: () -> Unit,
    elevation: androidx.compose.ui.unit.Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = valor,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subtexto,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TopProductsCard(
    productos: List<com.manrique.trailerstock.model.ProductoEstrella>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.label_top_products_30d),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (productos.isEmpty()) {
                Text(
                    text = stringResource(R.string.msg_no_recent_sales),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                productos.take(5).forEach { producto ->
                    TopProductItem(producto = producto)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TopProductItem(producto: com.manrique.trailerstock.model.ProductoEstrella) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${producto.cantidadVendida} u.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = producto.porcentajeRotacion,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticCardItem(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.label_view_more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_error_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun EarningsDetailsSheet(
    uiState: StatisticsUiState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.label_earnings_breakdown),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Tarjeta de Eficiencia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.label_efficiency_summary),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                EfficiencyItem(
                    label = stringResource(R.string.label_total_sales),
                    value = uiState.getTotalVentasFormatted(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                EfficiencyItem(
                    label = stringResource(R.string.label_total_cost),
                    value = formatCurrency(uiState.totalVentasPeriodo - uiState.gananciaPeriodo),
                    color = MaterialTheme.colorScheme.error
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
                EfficiencyItem(
                    label = stringResource(R.string.label_net_profit),
                    value = uiState.getGananciaPeriodoFormatted(),
                    color = MaterialTheme.colorScheme.primary,
                    isBold = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rentabilidad por Categoría
        if (uiState.gananciaPorCategoria.isNotEmpty()) {
            Text(
                text = stringResource(R.string.label_profit_by_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.gananciaPorCategoria.forEach { item ->
                val color = com.manrique.trailerstock.utils.ColorUtils.parseHexColor(
                    uiState.categoriasList.find { it.nombre == item.nombre }?.color
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.nombre, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = formatCurrency(item.gananciaReal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ranking de Productos más Rentables
        if (uiState.productosMasRentables.isNotEmpty()) {
            Text(
                text = stringResource(R.string.label_most_profitable),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            uiState.productosMasRentables.forEach { producto ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = producto.nombre,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "+ ${formatCurrency(producto.gananciaTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        ) {
            Text(stringResource(R.string.action_accept))
        }
    }
}

@Composable
private fun EfficiencyItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            color = color
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "AR"))
    return format.format(amount)
}
