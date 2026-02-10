package com.manrique.trailerstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.manrique.trailerstock.data.local.AppDatabase
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.ui.ViewModelFactory
import com.manrique.trailerstock.ui.screens.statistics.StatisticsScreen
import com.manrique.trailerstock.ui.screens.statistics.StatisticsViewModel
import com.manrique.trailerstock.ui.theme.TrailerStockTheme

/**
 * MainActivity migrada a Jetpack Compose.
 * 
 * Por ahora muestra solo la pantalla de estadísticas.
 * Próximamente se agregará navegación completa.
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var database: AppDatabase
    private lateinit var viewModelFactory: ViewModelFactory
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar base de datos
        database = AppDatabase.getDatabase(applicationContext)
        
        // Crear repositorios
        val productoRepository = ProductoRepository(database.productoDao())
        val ventaRepository = VentaRepository(
            database.ventaDao(),
            database.ventaDetalleDao()
        )
        val categoriaRepository = CategoriaRepository(database.categoriaDao())
        
        // Crear ViewModelFactory
        viewModelFactory = ViewModelFactory(
            productoRepository,
            ventaRepository,
            categoriaRepository
        )
        
        setContent {
            TrailerStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TrailerStockApp(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}

@Composable
fun TrailerStockApp(viewModelFactory: ViewModelFactory) {
    // Por ahora solo mostramos estadísticas
    // Próximamente agregaremos navegación completa con NavHost
    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = viewModelFactory
    )
    
    StatisticsScreen(viewModel = statisticsViewModel)
}
