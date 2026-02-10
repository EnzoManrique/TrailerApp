package com.manrique.trailerstock.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * ViewModel para la pantalla de estadísticas.
 * 
 * Gestiona la lógica de negocio y el estado de la UI
 * para mostrar estadísticas de ventas y productos.
 */
class StatisticsViewModel(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository
) : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    /**
     * Carga todas las estadísticas necesarias
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Observar cambios en productos
                productoRepository.allProductos.collect { productos ->
                    // Obtener estadísticas adicionales con suspend
                    val stockBajo = productoRepository.getLowStockProducts().size
                    val totalVentasHoy = ventaRepository.getTotalHoy()
                    val cantidadVentasHoy = ventaRepository.getCantidadVentasHoy()
                    val totalProductos = productoRepository.countProducts()
                    
                    _uiState.value = _uiState.value.copy(
                        totalProductos = totalProductos,
                        productosStockBajo = stockBajo,
                        ventasHoy = cantidadVentasHoy,
                        totalVentasHoy = totalVentasHoy,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Refresca las estadísticas
     */
    fun refresh() {
        loadStatistics()
    }
}

/**
 * Estado de la UI para la pantalla de estadísticas
 */
data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Productos
    val totalProductos: Int = 0,
    val productosStockBajo: Int = 0,
    
    // Ventas
    val ventasHoy: Int = 0,
    val totalVentasHoy: Double = 0.0
) {
    /**
     * Formatea el total de ventas de hoy como moneda
     */
    fun getTotalVentasHoyFormatted(): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        return format.format(totalVentasHoy)
    }
}

