package com.manrique.trailerstock.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.DetalleVenta
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla principal de ventas (historial)
 */
class SalesViewModel(
    private val ventaRepository: VentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadVentas()
    }

    private fun loadVentas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                ventaRepository.allVentas.collect { ventas ->
                    _uiState.value = _uiState.value.copy(
                        ventas = ventas,
                        ventasFiltradas = applyFilters(ventas),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyAllFilters()
    }

    fun setFilterMetodoPago(metodo: MetodoPago?) {
        _uiState.value = _uiState.value.copy(filterMetodoPago = metodo)
        applyAllFilters()
    }

    fun setFilterTipoCliente(tipo: String?) {
        _uiState.value = _uiState.value.copy(filterTipoCliente = tipo)
        applyAllFilters()
    }

    fun setFilterFechaInicio(fecha: Long?) {
        _uiState.value = _uiState.value.copy(filterFechaInicio = fecha)
        applyAllFilters()
    }

    fun setFilterFechaFin(fecha: Long?) {
        _uiState.value = _uiState.value.copy(filterFechaFin = fecha)
        applyAllFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filterMetodoPago = null,
            filterTipoCliente = null,
            filterFechaInicio = null,
            filterFechaFin = null
        )
        applyAllFilters()
    }
    
    /**
     * Muestra el dialog de detalles de una venta
     */
    fun showSaleDetails(ventaId: Int) {
        viewModelScope.launch {
            try {
                val venta = ventaRepository.getById(ventaId)
                val detalles = ventaRepository.getDetallesById(ventaId)
                
                if (venta != null) {
                    _uiState.value = _uiState.value.copy(
                        selectedVenta = venta,
                        selectedVentaDetalles = detalles,
                        showDetailDialog = true
                    )
                }
            } catch (e: Exception) {
                // Handle error si es necesario
            }
        }
    }
    
    /**
     * Cierra el dialog de detalles
     */
    fun dismissDetailDialog() {
        _uiState.value = _uiState.value.copy(
            showDetailDialog = false,
            selectedVenta = null,
            selectedVentaDetalles = emptyList()
        )
    }

    private fun applyAllFilters() {
        val state = _uiState.value
        _uiState.value = state.copy(
            ventasFiltradas = applyFilters(state.ventas)
        )
    }

    private fun applyFilters(ventas: List<Venta>): List<Venta> {
        val state = _uiState.value
        var filtered = ventas

        // Filtro por método de pago
        state.filterMetodoPago?.let { metodo ->
            filtered = filtered.filter { it.metodoPago == metodo }
        }

        // Filtro por tipo de cliente
        state.filterTipoCliente?.let { tipo ->
            filtered = filtered.filter { it.tipoCliente == tipo }
        }

        // Filtro por rango de fechas
        if (state.filterFechaInicio != null || state.filterFechaFin != null) {
            filtered = filtered.filter { venta ->
                val afterStart = state.filterFechaInicio?.let { venta.fecha >= it } ?: true
                // Ajustar fin para incluir todo el día seleccionado
                val beforeEnd = state.filterFechaFin?.let { 
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = it
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    calendar.set(java.util.Calendar.MINUTE, 59)
                    calendar.set(java.util.Calendar.SECOND, 59)
                    calendar.set(java.util.Calendar.MILLISECOND, 999)
                    venta.fecha <= calendar.timeInMillis
                } ?: true
                afterStart && beforeEnd
            }
        }

        // Búsqueda por número de venta o notas
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { venta ->
                venta.numeroVenta?.contains(state.searchQuery, ignoreCase = true) == true ||
                venta.notas?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }

        return filtered
    }

    /**
     * Elimina una venta y restaura el stock de los productos
     */
    fun deleteSale(venta: Venta, restaurarStock: Boolean) {
        viewModelScope.launch {
            try {
                ventaRepository.delete(venta, restaurarStock)
                // El flow se encargará de refrescar la lista
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al anular venta: ${e.message}")
            }
        }
    }
}

/**
 * Estado de UI para la pantalla de ventas
 */
data class SalesUiState(
    val isLoading: Boolean = true,
    val ventas: List<Venta> = emptyList(),
    val ventasFiltradas: List<Venta> = emptyList(),
    val searchQuery: String = "",
    val filterMetodoPago: MetodoPago? = null,
    val filterTipoCliente: String? = null,
    val filterFechaInicio: Long? = null,
    val filterFechaFin: Long? = null,
    val error: String? = null,
    val showDetailDialog: Boolean = false,
    val selectedVenta: Venta? = null,
    val selectedVentaDetalles: List<DetalleVenta> = emptyList()
)
