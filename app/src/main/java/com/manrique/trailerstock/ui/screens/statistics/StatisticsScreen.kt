package com.manrique.trailerstock.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer

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
    onNavigateToSales: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

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
                is StatisticsUiEvent.NavigateToProducts -> onNavigateToProducts(event.lowStockOnly)
                is StatisticsUiEvent.NavigateToSales -> onNavigateToSales()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Negocio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Selectores de Tiempo
                TimeRangeSelection(
                    selectedRange = uiState.selectedRange,
                    onRangeSelected = { viewModel.onTimeRangeSelected(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1.0f)) {
                    when {
                        uiState.isLoading && !pullToRefreshState.isRefreshing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiState.error != null -> {
                            ErrorMessage(
                                message = uiState.error ?: "Error desconocido",
                                onRetry = { viewModel.refresh() },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            StatisticsGrid(
                                uiState = uiState,
                                onLowStockClick = { viewModel.onLowStockClick() },
                                onSalesClick = { viewModel.onSalesClick() }
                            )
                        }
                    }
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
        StatisticsTimeRange.values().forEach { range ->
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
    modifier: Modifier = Modifier
) {
    val prefs = uiState.preferences ?: return

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Tarjeta Principal (Ancho completo): Ganancias del Periodo
        if (prefs.showEarnings) {
            item(span = { GridItemSpan(2) }) {
                MainMetricsCard(
                    valor = uiState.getGananciaPeriodoFormatted(),
                    titulo = "Ganancia Estimada (${uiState.selectedRange.label})",
                    subtexto = "Basado en ${uiState.ventasPeriodo} ventas",
                    onClick = onSalesClick
                )
            }
        }

        // Tarjetas Secundarias (Medio ancho)
        if (prefs.showSales) {
            item {
                StatisticCardItem(
                    title = "Ventas",
                    value = uiState.getTotalVentasFormatted(),
                    subtitle = "${uiState.ventasPeriodo} operaciones",
                    icon = Icons.Outlined.AttachMoney,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onSalesClick
                )
            }
        }

        if (prefs.showLowStock) {
            item {
                StatisticCardItem(
                    title = "Stock Bajo",
                    value = "${uiState.productosStockBajo}",
                    subtitle = "Productos críticos",
                    icon = Icons.Outlined.WarningAmber,
                    color = MaterialTheme.colorScheme.error,
                    onClick = onLowStockClick
                )
            }
        }

        if (prefs.showTicket) {
            item {
                StatisticCardItem(
                    title = "Ticket Promedio",
                    value = uiState.getTicketPromedioFormatted(),
                    subtitle = "Por venta",
                    icon = Icons.Outlined.Analytics,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (prefs.showCapital) {
            item {
                StatisticCardItem(
                    title = "Capital",
                    value = uiState.getValorInventarioFormatted(),
                    subtitle = "Valor en stock",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // --- Nuevas Estadísticas ---

        // Ventas por Categoría (Ancho completo)
        if (prefs.showSalesByCategory && uiState.ventasPorCategoria.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                CategorySalesCard(ventas = uiState.ventasPorCategoria)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ventas por Categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            ventas.forEach { venta ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = venta.nombre, style = MaterialTheme.typography.bodyMedium)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Productos más Rentables",
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Productos Estancados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No se han vendido en el tiempo establecido.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            productos.take(5).forEach { producto ->
                Text(
                    text = "• ${producto.nombre} (${producto.stockActual} en stock)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            if (productos.size > 5) {
                Text(
                    text = "Y ${productos.size - 5} más...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Productos Estrella (Últimos 30 días)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (productos.isEmpty()) {
                Text(
                    text = "No hay datos de ventas recientes",
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
            .height(140.dp),
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        contentDescription = "Ver más",
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
                    maxLines = 1,
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
            text = "Error",
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
            Text("Reintentar")
        }
    }
}

