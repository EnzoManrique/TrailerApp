package com.manrique.trailerstock.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.Categoria
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _filterCategoryId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ProductsUiState> = combine(
        productoRepository.allProductos,
        categoriaRepository.allCategorias,
        _filterCategoryId
    ) { productos, categorias, filterId ->
        val baseProductos = productos.filter { !it.eliminado }
        val filtered = if (filterId != null) {
            baseProductos.filter { it.categoriaId == filterId }
        } else baseProductos
        
        ProductsUiState(
            productos = baseProductos,
            productosFiltrados = filtered,
            categorias = categorias,
            filterCategoryId = filterId,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductsUiState())

    init {
        // loadData ya no es necesario por el combine arriba
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
     * Incrementa el stock de un producto
     */
    fun restockProduct(producto: Producto, cantidad: Int) {
        viewModelScope.launch {
            productoRepository.restock(producto.id, cantidad)
        }
    }

    fun setFilterCategory(id: Int?) {
        _filterCategoryId.value = id
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
    fun getCategory(categoriaId: Int): Categoria? {
        return uiState.value.categorias.find { it.id == categoriaId }
    }

    fun getCategoryName(categoriaId: Int): String {
        return getCategory(categoriaId)?.nombre ?: "Sin categoría"
    }
}

/**
 * Estado de la UI para la pantalla de productos
 */
data class ProductsUiState(
    val isLoading: Boolean = true,
    val productos: List<Producto> = emptyList(),
    val productosFiltrados: List<Producto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val filterCategoryId: Int? = null,
    val error: String? = null
)
