package com.manrique.trailerstock.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.manrique.trailerstock.R
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
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.userPreferences.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

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
                    type = context.contentResolver.getType(state.uri) ?: "application/octet-stream"
                    putExtra(android.content.Intent.EXTRA_STREAM, state.uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooserTitle = when {
                    state.uri.toString().contains(".pdf") -> "Compartir Reporte PDF"
                    state.uri.toString().contains(".xlsx") -> "Compartir Inventario Excel"
                    else -> "Compartir Archivo"
                }
                context.startActivity(android.content.Intent.createChooser(intent, chooserTitle))
                viewModel.resetBackupState()
            }
            is BackupUiState.RequireRestart -> {
                showRestartDialog = true
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

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (preferences == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val prefs = preferences!!
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
            ) {
                item {
                    SettingsSectionTitle(stringResource(R.string.settings_backup_title))
                }

                item {
                    SettingsActionItem(
                        title = stringResource(R.string.settings_create_backup),
                        subtitle = stringResource(R.string.settings_create_backup_sub),
                        icon = Icons.Default.Backup,
                        onClick = { viewModel.shareBackup() }
                    )
                }

                item {
                    SettingsActionItem(
                        title = stringResource(R.string.settings_restore_backup),
                        subtitle = stringResource(R.string.settings_restore_backup_sub),
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
                    SettingsSectionTitle("Reportes y Exportación")
                }

                item {
                    SettingsActionItem(
                        title = "Reporte de Ventas (Mensual)",
                        subtitle = "Generar PDF con las ventas de los últimos 30 días",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = { viewModel.exportSalesReport(30) }
                    )
                }

                item {
                    SettingsActionItem(
                        title = "Inventario a Excel",
                        subtitle = "Exportar lista completa de productos",
                        icon = Icons.Default.TableChart,
                        onClick = { viewModel.exportInventory() }
                    )
                }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }

                item {
                    SettingsSectionTitle(stringResource(R.string.settings_dashboard_custom))
                }
                
                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_estimated_earnings),
                        subtitle = stringResource(R.string.settings_earnings_sub),
                        checked = prefs.showEarnings,
                        onCheckedChange = { viewModel.toggleVisibility("earnings", it) }
                    )
                }
                
                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_total_sales),
                        subtitle = stringResource(R.string.settings_sales_sub),
                        checked = prefs.showSales,
                        onCheckedChange = { viewModel.toggleVisibility("sales", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_low_stock),
                        subtitle = stringResource(R.string.settings_low_stock_sub),
                        checked = prefs.showLowStock,
                        onCheckedChange = { viewModel.toggleVisibility("low_stock", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_average_ticket),
                        subtitle = stringResource(R.string.settings_ticket_sub),
                        checked = prefs.showTicket,
                        onCheckedChange = { viewModel.toggleVisibility("ticket", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_capital_stock),
                        subtitle = stringResource(R.string.settings_capital_sub),
                        checked = prefs.showCapital,
                        onCheckedChange = { viewModel.toggleVisibility("capital", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_top_products),
                        subtitle = stringResource(R.string.settings_top_products_sub),
                        checked = prefs.showTopProducts,
                        onCheckedChange = { viewModel.toggleVisibility("top_products", it) }
                    )
                }

                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }

                item {
                    SettingsSectionTitle(stringResource(R.string.settings_new_stats))
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_sales_by_category),
                        subtitle = stringResource(R.string.settings_cat_sales_sub),
                        checked = prefs.showSalesByCategory,
                        onCheckedChange = { viewModel.toggleVisibility("category_sales", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_most_profitable),
                        subtitle = stringResource(R.string.settings_profitable_sub),
                        checked = prefs.showMostProfitable,
                        onCheckedChange = { viewModel.toggleVisibility("profitable", it) }
                    )
                }

                item {
                    SettingsToggleItem(
                        title = stringResource(R.string.label_stagnant_products),
                        subtitle = stringResource(R.string.settings_stagnant_sub),
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
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }

                item {
                    SettingsActionItem(
                        title = stringResource(R.string.label_about),
                        subtitle = "Versión, autor y tutoriales",
                        icon = Icons.Default.Info,
                        onClick = onNavigateToAbout
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSectionTitle(stringResource(R.string.settings_dev_options))
                }

                item {
                    SettingsActionItem(
                        title = stringResource(R.string.settings_reset_db),
                        subtitle = stringResource(R.string.settings_reset_db_sub),
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
            title = { Text(stringResource(R.string.settings_reset_db_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_db_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDatabase()
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.settings_reset_db), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar sin reiniciar */ },
            title = { Text(stringResource(R.string.settings_restore_success_title)) },
            text = { Text(stringResource(R.string.settings_restore_success_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        val packageManager = context.packageManager
                        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                        val componentName = intent?.component
                        val restartIntent = android.content.Intent.makeRestartActivityTask(componentName)
                        context.startActivity(restartIntent)
                        Runtime.getRuntime().exit(0)
                    }
                ) {
                    Text(stringResource(R.string.settings_btn_restart))
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
            text = stringResource(R.string.label_threshold_days_fmt, days),
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = days.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 7f..180f,
            steps = 24 // aprox cada semana
        )
        Text(
            text = stringResource(R.string.msg_stagnant_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
