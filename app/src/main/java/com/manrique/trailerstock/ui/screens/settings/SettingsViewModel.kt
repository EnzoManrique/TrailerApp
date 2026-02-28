package com.manrique.trailerstock.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.backup.BackupManager
import com.manrique.trailerstock.data.local.AppDatabase
import com.manrique.trailerstock.data.repository.UserPreferencesRepository
import com.manrique.trailerstock.data.repository.UserPreferences
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.utils.ExportManager
import com.manrique.trailerstock.model.VentaConDetalles
import androidx.core.content.FileProvider
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class BackupUiState {
    object Idle : BackupUiState()
    object Loading : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
    data class ReadyToShare(val uri: Uri) : BackupUiState()
    object RequireRestart : BackupUiState()
}

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val database: AppDatabase,
    private val exportManager: ExportManager,
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _backupState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleVisibility(key: String, visible: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateVisibility(key, visible)
        }
    }

    fun updateStagnantThreshold(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateStagnantThreshold(days)
        }
    }

    fun shareBackup() {
        viewModelScope.launch {
            _backupState.value = BackupUiState.Loading
            backupManager.getBackupUriForSharing(database).onSuccess { (uri, info) ->
                _backupState.value = BackupUiState.ReadyToShare(uri)
                // Opcional: Mostrar info en un snackbar aparte o integrarlo
                _backupState.value = BackupUiState.Success("Backup preparado: $info")
                _backupState.value = BackupUiState.ReadyToShare(uri) 
            }.onFailure {
                _backupState.value = BackupUiState.Error("Error al preparar backup: ${it.message}")
            }
        }
    }


    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupUiState.Loading
            backupManager.exportDatabase(uri, database).onSuccess {
                _backupState.value = BackupUiState.Success("Copia de seguridad creada correctamente")
            }.onFailure {
                _backupState.value = BackupUiState.Error("Error al exportar: ${it.message}")
            }
        }
    }

    fun exportSalesReport(days: Int) {
        viewModelScope.launch {
            _backupState.value = BackupUiState.Loading
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val inicio = if (days == 0) {
                // Caso hoy: desde medianoche
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
            } else {
                calendar.timeInMillis
            }
            
            val fin = System.currentTimeMillis()
            
            // Recolectar ventas
            ventaRepository.getVentasCompletasByDateRange(inicio, fin).collect { ventas ->
                val file = exportManager.generateSalesReportPdf(ventas, exportManager.context.getString(com.manrique.trailerstock.R.string.report_sales_title))
                if (file != null) {
                    val uri = FileProvider.getUriForFile(
                        exportManager.context, // Error: needs context access, maybe I should pass context if needed or use backupManager logic
                        "${exportManager.context.packageName}.fileprovider",
                        file
                    )
                    _backupState.value = BackupUiState.ReadyToShare(uri)
                } else {
                    _backupState.value = BackupUiState.Error("Error al generar PDF")
                }
            }
        }
    }

    fun exportInventory() {
        viewModelScope.launch {
            _backupState.value = BackupUiState.Loading
            productoRepository.allProductos.collect { productos ->
                val file = exportManager.generateInventoryExcel(productos)
                if (file != null) {
                    val uri = FileProvider.getUriForFile(
                        exportManager.context,
                        "${exportManager.context.packageName}.fileprovider",
                        file
                    )
                    _backupState.value = BackupUiState.ReadyToShare(uri)
                } else {
                    _backupState.value = BackupUiState.Error("Error al generar Excel")
                }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupUiState.Loading
            backupManager.importDatabase(uri, database).onSuccess {
                _backupState.value = BackupUiState.RequireRestart
            }.onFailure {
                _backupState.value = BackupUiState.Error("Error al restaurar: ${it.message}")
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupUiState.Idle
    }
}
