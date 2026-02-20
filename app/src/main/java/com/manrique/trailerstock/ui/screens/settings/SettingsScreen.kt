package com.manrique.trailerstock.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.userPreferences.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    // Lanzador para ABRIR archivo (Importar)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    // Manejar feedback del backup
    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupUiState.ReadyToShare -> {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/x-sqlite3"
                    putExtra(android.content.Intent.EXTRA_STREAM, state.uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Guardar Copia de Seguridad"))
                viewModel.resetBackupState()
            }
            is BackupUiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetBackupState()
                }
            }
            is BackupUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Long
                    )
                    viewModel.resetBackupState()
                }
            }
            else -> {}
        }
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    SettingsSectionTitle("Copia de Seguridad")
                }

                item {
                    SettingsActionItem(
                        title = "Crear Copia de Seguridad",
                        subtitle = "Súbelo a Google Drive para no perder nada",
                        icon = Icons.Default.Backup,
                        onClick = { viewModel.shareBackup() }
                    )
                }

                item {
                    SettingsActionItem(
                        title = "Restaurar Copia de Seguridad",
                        subtitle = "Recuperar datos de un archivo anterior",
                        icon = Icons.Default.Restore,
                        onClick = {
                            importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*"))
                        }
                    )
                }

                item {
                    if (backupState is BackupUiState.Loading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }

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
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSectionTitle("Opciones de Desarrollo (Temporal)")
                }

                item {
                    SettingsActionItem(
                        title = "Resetear Base de Datos",
                        subtitle = "Borra todo y carga productos de prueba",
                        icon = Icons.Default.DeleteForever,
                        onClick = { showResetDialog = true }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("¿Resetear base de datos?") },
            text = { Text("Se borrarán todos los productos y ventas actuales. Se cargarán datos de prueba automáticamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDatabase()
                        showResetDialog = false
                    }
                ) {
                    Text("RESETEAR", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
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
