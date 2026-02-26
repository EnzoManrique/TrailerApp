package com.manrique.trailerstock.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.data.repository.UserPreferencesRepository
import com.manrique.trailerstock.data.repository.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.manrique.trailerstock.model.StatisticsTimeRange

/**
 * ViewModel para la pantalla de estadísticas.
 * 
 * Gestiona la lógica de negocio y el estado de la UI
 * para mostrar estadísticas de ventas y productos.
 */

/**
 * Eventos de UI para navegación
 */
sealed class StatisticsUiEvent {
    data class NavigateToSales(val range: StatisticsTimeRange? = null) : StatisticsUiEvent()
}

class StatisticsViewModel(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<StatisticsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                _uiState.value = _uiState.value.copy(preferences = preferences)
                loadStatistics()
            }
        }
    }

    fun refresh() {
        loadStatistics()
    }

    fun onTimeRangeSelected(range: StatisticsTimeRange) {
        _uiState.value = _uiState.value.copy(selectedRange = range)
        loadStatistics()
    }

    fun onLowStockClick() {
        // Ahora se maneja localmente en la UI abriendo un BottomSheet
    }

    fun onSalesClick() {
        viewModelScope.launch {
            _uiEvent.emit(StatisticsUiEvent.NavigateToSales(range = _uiState.value.selectedRange))
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val range = _uiState.value.selectedRange
                val inicioTimestamp = when (range) {
                    StatisticsTimeRange.HOY -> {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }
                    StatisticsTimeRange.SEMANA -> ventaRepository.getInicioSemanaTimestamp()
                    StatisticsTimeRange.MES -> ventaRepository.getInicioMesTimestamp()
                }

                // Observar cambios en productos (solo una vez para el estado inicial)
                // Nota: Idealmente esto sería un combine de flows, pero por simplicidad
                // recolectamos los datos suspendidos para el dashboard.
                
                val lowStockProducts = productoRepository.getLowStockProducts()
                val stockBajo = lowStockProducts.size
                val totalVentas = ventaRepository.getTotalVentasPeriodo(inicioTimestamp)
                val cantidadVentas = ventaRepository.getCantidadVentasPeriodo(inicioTimestamp)
                val totalProductos = productoRepository.countProducts()
                
                val ticketPromedio = if (cantidadVentas > 0) totalVentas / cantidadVentas else 0.0
                val ganancia = ventaRepository.getGananciaPeriodo(inicioTimestamp)
                val valorInventario = productoRepository.getValorInventario()
                
                val topProductos = ventaRepository.getTopProductos(inicioTimestamp)
                val maxVendido = topProductos.firstOrNull()?.cantidadVendida ?: 1
                
                val productosEstrella = topProductos.map { 
                    com.manrique.trailerstock.model.ProductoEstrella(
                        nombre = it.nombre,
                        cantidadVendida = it.cantidadVendida,
                        porcentajeRotacion = it.cantidadVendida.toFloat() / maxVendido
                    )
                }

                // Nuevas métricas
                val prefs = _uiState.value.preferences
                val ventasPorCategoria = if (prefs?.showSalesByCategory == true) {
                    ventaRepository.getVentasPorCategoria(inicioTimestamp)
                } else emptyList()

                val productosMasRentables = if (prefs?.showMostProfitable == true) {
                    ventaRepository.getProductosMasRentables(inicioTimestamp)
                } else emptyList()

                val productosEstancados = if (prefs?.showStagnantProducts == true) {
                    productoRepository.getProductosEstancados(prefs.stagnantThresholdDays)
                } else emptyList()

                // Obtener lista de ventas del periodo actual ( snapshot )
                val calendar = Calendar.getInstance()
                val finTimestamp = calendar.timeInMillis
                val listaVentasPeriodo = try {
                    ventaRepository.getVentasByRangoFechas(inicioTimestamp, finTimestamp).first()
                } catch (e: Exception) {
                    emptyList()
                }
                
                _uiState.value = _uiState.value.copy(
                    totalProductos = totalProductos,
                    productosStockBajo = stockBajo,
                    listaProductosStockBajo = lowStockProducts,
                    ventasPeriodo = cantidadVentas,
                    totalVentasPeriodo = totalVentas,
                    ticketPromedio = ticketPromedio,
                    gananciaPeriodo = ganancia,
                    valorInventario = valorInventario,
                    productosEstrella = productosEstrella,
                    ventasPorCategoria = ventasPorCategoria,
                    productosMasRentables = productosMasRentables,
                    productosEstancados = productosEstancados,
                    listaVentasPeriodo = listaVentasPeriodo,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar estadísticas"
                )
            }
        }
    }
}

/**
 * Estado de la UI para la pantalla de estadísticas
 */
data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Filtro
    val selectedRange: StatisticsTimeRange = StatisticsTimeRange.MES,
    
    // Productos
    val totalProductos: Int = 0,
    val productosStockBajo: Int = 0,
    val listaProductosStockBajo: List<com.manrique.trailerstock.data.local.entities.Producto> = emptyList(),
    val valorInventario: Double = 0.0,
    val productosEstrella: List<com.manrique.trailerstock.model.ProductoEstrella> = emptyList(),
    
    // Ventas del periodo seleccionado
    val ventasPeriodo: Int = 0,
    val totalVentasPeriodo: Double = 0.0,
    val ticketPromedio: Double = 0.0,
    val gananciaPeriodo: Double = 0.0,

    // Nuevas estadísticas
    val ventasPorCategoria: List<com.manrique.trailerstock.data.local.dao.CategoriaVenta> = emptyList(),
    val productosMasRentables: List<com.manrique.trailerstock.data.local.dao.ProductoRentable> = emptyList(),
    val productosEstancados: List<com.manrique.trailerstock.data.local.entities.Producto> = emptyList(),
    val listaVentasPeriodo: List<com.manrique.trailerstock.data.local.entities.Venta> = emptyList(),

    // Preferencias
    val preferences: UserPreferences? = null
) {
    fun getTotalVentasFormatted(): String {
        return formatCurrency(totalVentasPeriodo)
    }

    fun getTicketPromedioFormatted(): String {
        return formatCurrency(ticketPromedio)
    }

    fun getGananciaPeriodoFormatted(): String {
        return formatCurrency(gananciaPeriodo)
    }

    fun getValorInventarioFormatted(): String {
        return formatCurrency(valorInventario)
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        return format.format(amount)
    }
}

