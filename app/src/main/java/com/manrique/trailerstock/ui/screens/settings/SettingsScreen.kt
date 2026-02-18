package com.manrique.trailerstock.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.userPreferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás"
                        )
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
        if (preferences == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val prefs = preferences!!
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    SettingsSectionTitle("Personalización de Dashboard")
                }
                
                item {
                    SettingsToggleItem(
                        title = "Ganancia Estimada",
                        subtitle = "Muestra las ganancias del periodo",
                        checked = prefs.showEarnings,
                        onCheckedChange = { viewModel.toggleVisibility("earnings", it) }
                    )
                }
                
                item {
                    SettingsToggleItem(
                        title = "Ventas Totales",
                        subtitle = "Resumen de ventas y operaciones",
                        checked = prefs.showSales,
                        onCheckedChange = { viewModel.toggleVisibility("sales", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = "Stock Bajo",
                        subtitle = "Alerta de productos críticos",
                        checked = prefs.showLowStock,
                        onCheckedChange = { viewModel.toggleVisibility("low_stock", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = "Ticket Promedio",
                        subtitle = "Valor promedio por venta",
                        checked = prefs.showTicket,
                        onCheckedChange = { viewModel.toggleVisibility("ticket", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = "Capital en Stock",
                        subtitle = "Valor total del inventario",
                        checked = prefs.showCapital,
                        onCheckedChange = { viewModel.toggleVisibility("capital", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = "Productos Estrella",
                        subtitle = "Los 5 productos más vendidos",
                        checked = prefs.showTopProducts,
                        onCheckedChange = { viewModel.toggleVisibility("top_products", it) }
                    )
                }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }

                item {
                    SettingsSectionTitle("Nuevas Estadísticas")
                }

                item {
                    SettingsToggleItem(
                        title = "Ventas por Categoría",
                        subtitle = "Desglose de ingresos por rubro",
                        checked = prefs.showSalesByCategory,
                        onCheckedChange = { viewModel.toggleVisibility("category_sales", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = "Productos más Rentables",
                        subtitle = "Productos con mayor margen de ganancia",
                        checked = prefs.showMostProfitable,
                        onCheckedChange = { viewModel.toggleVisibility("profitable", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = "Productos Estancados",
                        subtitle = "Productos sin ventas recientes",
                        checked = prefs.showStagnantProducts,
                        onCheckedChange = { viewModel.toggleVisibility("stagnant", it) }
                    )
                }

                if (prefs.showStagnantProducts) {
                    item {
                        ThresholdSlider(
                            days = prefs.stagnantThresholdDays,
                            onValueChange = { viewModel.updateStagnantThreshold(it) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun ThresholdSlider(
    days: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Umbral de Productos Estancados: $days días",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = days.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 7f..180f,
            steps = 24 // aprox cada semana
        )
        Text(
            text = "Se consideran estancados si no tuvieron ventas en este tiempo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
