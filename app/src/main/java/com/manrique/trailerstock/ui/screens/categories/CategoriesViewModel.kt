package com.manrique.trailerstock.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.Categoria
import com.manrique.trailerstock.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de categorías.
 * 
 * Gestiona la lista de categorías y operaciones CRUD.
 */
class CategoriesViewModel(
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Carga categorías activas
     */
    private fun loadData() {
        viewModelScope.launch {
            categoriaRepository.allCategorias.collect { categorias ->
                _uiState.value = CategoriesUiState(
                    categorias = categorias,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Inserta o actualiza una categoría
     */
    suspend fun saveCategory(categoria: Categoria): Result<Long> {
        return try {
            val id = if (categoria.id == 0) {
                categoriaRepository.insert(categoria)
            } else {
                categoriaRepository.update(categoria)
                categoria.id.toLong()
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina una categoría (soft delete)
     */
    fun deleteCategory(categoria: Categoria) {
        viewModelScope.launch {
            categoriaRepository.softDelete(categoria)
        }
    }

    /**
     * Obtiene una categoría por ID
     */
    suspend fun getCategoryById(id: Int): Categoria? {
        return categoriaRepository.getById(id)
    }
}

/**
 * Estado de la UI para la pantalla de categorías
 */
data class CategoriesUiState(
    val isLoading: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val error: String? = null
)
