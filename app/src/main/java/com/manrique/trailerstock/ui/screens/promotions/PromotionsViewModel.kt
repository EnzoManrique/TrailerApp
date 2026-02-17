package com.manrique.trailerstock.ui.screens.promotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.PromocionProducto
import com.manrique.trailerstock.data.local.entities.PromocionMetodoPago
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.PromocionRepository
import com.manrique.trailerstock.model.ProductoEnPromocion
import com.manrique.trailerstock.model.PromocionConProductos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de promociones.
 */
class PromotionsViewModel(
    private val promocionRepository: PromocionRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState: StateFlow<PromotionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                promocionRepository.allPromocionesConProductos.collect { promociones ->
                    _uiState.value = _uiState.value.copy(
                        promociones = promociones,
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

    /**
     * Inserta o actualiza una promoción con sus productos y métodos de pago de forma atómica
     */
    suspend fun savePromotion(
        promocion: Promocion,
        productosConCantidades: List<ProductoEnPromocion>,
        metodosPago: List<MetodoPago>
    ): Result<Long> {
        return try {
            val promocionProductos = productosConCantidades.map {
                PromocionProducto(
                    promocionId = promocion.id,
                    productoId = it.producto.id,
                    cantidadRequerida = it.cantidadRequerida
                )
            }
            
            val promocionMetodosPago = metodosPago.map {
                PromocionMetodoPago(
                    promocionId = promocion.id,
                    metodoPago = it
                )
            }

            val promocionId = promocionRepository.savePromotionAtomic(
                promocion = promocion,
                productos = promocionProductos,
                metodosPago = promocionMetodosPago
            )

            Result.success(promocionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una promoción (soft delete)
     */
    fun deletePromotion(promocion: Promocion) {
        viewModelScope.launch {
            promocionRepository.softDelete(promocion)
        }
    }

    /**
     * Activa o desactiva una promoción
     */
    fun togglePromotionStatus(promocionId: Int, activa: Boolean) {
        viewModelScope.launch {
            promocionRepository.cambiarEstado(promocionId, activa)
        }
    }

    /**
     * Obtiene una promoción por ID con sus productos y métodos de pago
     */
    suspend fun getPromotionWithProductsById(id: Int): PromocionConProductos? {
        val promocion = promocionRepository.getById(id) ?: return null
        val promocionProductos = promocionRepository.getProductosDePromocion(id)
        val promocionMetodosPago = promocionRepository.getMetodosPagoDePromocion(id)
        
        val productosEnPromocion = promocionProductos.mapNotNull { pp ->
            productoRepository.getById(pp.productoId)?.let { producto ->
                ProductoEnPromocion(
                    producto = producto,
                    cantidadRequerida = pp.cantidadRequerida
                )
            }
        }
        
        val metodosPago = promocionMetodosPago.map { it.metodoPago }
        
        return PromocionConProductos(
            promocion = promocion,
            productos = productosEnPromocion,
            metodosPago = metodosPago
        )
    }

    /**
     * Obtiene todos los productos disponibles
     */
    suspend fun getAllProductos(): List<Producto> {
        return productoRepository.allProductos.first().filter { !it.eliminado }
    }
}

/**
 * Estado de UI para la pantalla de promociones
 */
data class PromotionsUiState(
    val isLoading: Boolean = true,
    val promociones: List<PromocionConProductos> = emptyList(),
    val error: String? = null
)
