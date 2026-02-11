package com.manrique.trailerstock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.ui.screens.categories.CategoriesViewModel
import com.manrique.trailerstock.ui.screens.products.ProductsViewModel
import com.manrique.trailerstock.ui.screens.statistics.StatisticsViewModel

/**
 * Factory para crear ViewModels con dependencias inyectadas.
 * 
 * Esta factory permite pasar repositorios a los ViewModels
 * sin violar el principio de inyección de dependencias.
 */
class ViewModelFactory(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                StatisticsViewModel(
                    productoRepository = productoRepository,
                    ventaRepository = ventaRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProductsViewModel::class.java) -> {
                ProductsViewModel(
                    productoRepository = productoRepository,
                    categoriaRepository = categoriaRepository
                ) as T
            }
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> {
                CategoriesViewModel(
                    categoriaRepository = categoriaRepository
                ) as T
            }
            
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

