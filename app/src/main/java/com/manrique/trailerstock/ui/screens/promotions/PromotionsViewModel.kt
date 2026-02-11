package com.manrique.trailerstock.ui.screens.promotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.PromocionProducto
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

    /**
     * Carga promociones y productos
     */
    private fun loadData() {
        viewModelScope.launch {
            // Cargar promociones
            promocionRepository.allPromociones.collect { promociones ->
                _uiState.value = _uiState.value.copy(
                    promociones = promociones,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Inserta o actualiza una promoción con sus productos
     */
    suspend fun savePromotion(
        promocion: Promocion,
        productosConCantidades: List<ProductoEnPromocion>
    ): Result<Long> {
        return try {
            val promocionId = if (promocion.id == 0) {
                // Insertar nueva promoción
                promocionRepository.insert(promocion).toInt()
            } else {
                // Actualizar promoción existente
                promocionRepository.update(promocion)
                // Eliminar productos antiguos
                promocionRepository.eliminarProductosDePromocion(promocion.id)
                promocion.id
            }

            // Insertar productos de la promoción
            val promocionProductos = productosConCantidades.map {
                PromocionProducto(
                    promocionId = promocionId,
                    productoId = it.producto.id,
                    cantidadRequerida = it.cantidadRequerida
                )
            }
            promocionRepository.insertProductosPromocion(promocionProductos)

            Result.success(promocionId.toLong())
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
     * Obtiene una promoción por ID con sus productos
     */
    suspend fun getPromotionWithProductsById(id: Int): PromocionConProductos? {
        val promocion = promocionRepository.getById(id) ?: return null
        val promocionProductos = promocionRepository.getProductosDePromocion(id)
        
        val productosEnPromocion = promocionProductos.mapNotNull { pp ->
            productoRepository.getById(pp.productoId)?.let { producto ->
                ProductoEnPromocion(
                    producto = producto,
                    cantidadRequerida = pp.cantidadRequerida
                )
            }
        }
        
        return PromocionConProductos(
            promocion = promocion,
            productos = productosEnPromocion
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
 * Estado de la UI para la pantalla de promociones
 */
data class PromotionsUiState(
    val isLoading: Boolean = true,
    val promociones: List<Promocion> = emptyList(),
    val error: String? = null
)
