package com.manrique.trailerstock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.VentaRepository
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
            // Aquí se pueden agregar más ViewModels según se vayan creando
            // modelClass.isAssignableFrom(ProductsViewModel::class.java) -> { ... }
            // modelClass.isAssignableFrom(SalesViewModel::class.java) -> { ... }
            
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
