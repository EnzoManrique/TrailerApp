package com.manrique.trailerstock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.PromocionRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.ui.screens.categories.CategoriesViewModel
import com.manrique.trailerstock.ui.screens.products.ProductsViewModel
import com.manrique.trailerstock.ui.screens.promotions.PromotionsViewModel
import com.manrique.trailerstock.ui.screens.statistics.StatisticsViewModel
import com.manrique.trailerstock.ui.screens.sales.SalesViewModel
import com.manrique.trailerstock.ui.screens.sales.CreateSaleViewModel
import com.manrique.trailerstock.ui.screens.settings.SettingsViewModel
import com.manrique.trailerstock.data.repository.UserPreferencesRepository
import com.manrique.trailerstock.data.backup.BackupManager
import com.manrique.trailerstock.data.local.AppDatabase
import com.manrique.trailerstock.utils.ExportManager

/**
 * Factory para crear ViewModels con dependencias inyectadas.
 * 
 * Esta factory permite pasar repositorios a los ViewModels
 * sin violar el principio de inyección de dependencias.
 */
class ViewModelFactory(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val promocionRepository: PromocionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val database: AppDatabase,
    private val exportManager: ExportManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                StatisticsViewModel(
                    productoRepository = productoRepository,
                    ventaRepository = ventaRepository,
                    categoriaRepository = categoriaRepository,
                    userPreferencesRepository = userPreferencesRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    userPreferencesRepository = userPreferencesRepository,
                    backupManager = backupManager,
                    database = database,
                    exportManager = exportManager,
                    ventaRepository = ventaRepository,
                    productoRepository = productoRepository
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
            modelClass.isAssignableFrom(PromotionsViewModel::class.java) -> {
                PromotionsViewModel(
                    promocionRepository = promocionRepository,
                    productoRepository = productoRepository
                ) as T
            }
            modelClass.isAssignableFrom(SalesViewModel::class.java) -> {
                SalesViewModel(
                    ventaRepository = ventaRepository
                ) as T
            }
            modelClass.isAssignableFrom(CreateSaleViewModel::class.java) -> {
                CreateSaleViewModel(
                    ventaRepository = ventaRepository,
                    productoRepository = productoRepository,
                    promocionRepository = promocionRepository,
                    categoriaRepository = categoriaRepository,
                    exportManager = exportManager
                ) as T
            }
            
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

