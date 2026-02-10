package com.manrique.trailerstock.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.Categoria
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de productos.
 * 
 * Gestiona la lista de productos, categorías y operaciones CRUD.
 */
class ProductsViewModel(
    private val productoRepository: ProductoRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Carga productos y categorías
     */
    private fun loadData() {
        viewModelScope.launch {
            combine(
                productoRepository.allProductos,
                categoriaRepository.allCategorias
            ) { productos, categorias ->
                ProductsUiState(
                    productos = productos.filter { !it.eliminado },
                    categorias = categorias,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Inserta o actualiza un producto
     */
    suspend fun saveProduct(producto: Producto): Result<Long> {
        return try {
            val id = if (producto.id == 0) {
                productoRepository.insert(producto)
            } else {
                productoRepository.update(producto)
                producto.id.toLong()
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un producto (soft delete)
     */
    fun deleteProduct(producto: Producto) {
        viewModelScope.launch {
            productoRepository.softDelete(producto)
        }
    }

    /**
     * Obtiene un producto por ID
     */
    suspend fun getProductById(id: Int): Producto? {
        return productoRepository.getById(id)
    }

    /**
     * Obtiene una categoría por ID
     */
    fun getCategoryName(categoriaId: Int): String {
        return _uiState.value.categorias
            .find { it.id == categoriaId }
            ?.nombre ?: "Sin categoría"
    }
}

/**
 * Estado de la UI para la pantalla de productos
 */
data class ProductsUiState(
    val isLoading: Boolean = true,
    val productos: List<Producto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val error: String? = null
)
