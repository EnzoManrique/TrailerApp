package com.manrique.trailerstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.manrique.trailerstock.data.local.AppDatabase
import com.manrique.trailerstock.data.repository.CategoriaRepository
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.PromocionRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.ui.ViewModelFactory
import com.manrique.trailerstock.ui.navigation.BottomNavigationBar
import com.manrique.trailerstock.ui.navigation.Screen
import com.manrique.trailerstock.ui.screens.categories.AddEditCategoryScreen
import com.manrique.trailerstock.ui.screens.categories.CategoriesScreen
import com.manrique.trailerstock.ui.screens.categories.CategoriesViewModel
import com.manrique.trailerstock.ui.screens.products.AddEditProductScreen
import com.manrique.trailerstock.ui.screens.products.ProductsScreen
import com.manrique.trailerstock.ui.screens.products.ProductsViewModel
import com.manrique.trailerstock.ui.screens.promotions.AddEditPromotionScreen
import com.manrique.trailerstock.ui.screens.promotions.PromotionsScreen
import com.manrique.trailerstock.ui.screens.promotions.PromotionsViewModel
import com.manrique.trailerstock.ui.screens.statistics.StatisticsScreen
import com.manrique.trailerstock.ui.screens.statistics.StatisticsViewModel
import com.manrique.trailerstock.ui.theme.TrailerStockTheme

/**
 * MainActivity migrada a Jetpack Compose.
 * 
 * Implementa navegación completa con bottom navigation bar.
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
        val promocionRepository = PromocionRepository(
            database.promocionDao(),
            database.promocionProductoDao()
        )
        
        // Crear ViewModelFactory
        viewModelFactory = ViewModelFactory(
            productoRepository,
            ventaRepository,
            categoriaRepository,
            promocionRepository
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Rutas que deben mostrar el bottom navigation bar
    val routesWithBottomBar = listOf(
        Screen.Statistics.route,
        Screen.Products.route,
        Screen.Sales.route,
        Screen.Promotions.route,
        Screen.Categories.route
    )
    
    val showBottomBar = currentRoute in routesWithBottomBar
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Statistics.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Estadísticas
            composable(Screen.Statistics.route) {
                val statisticsViewModel: StatisticsViewModel = viewModel(factory = viewModelFactory)
                StatisticsScreen(viewModel = statisticsViewModel)
            }
            
            // Productos
            composable(Screen.Products.route) {
                val productsViewModel: ProductsViewModel = viewModel(factory = viewModelFactory)
                ProductsScreen(
                    viewModel = productsViewModel,
                    onAddProduct = {
                        navController.navigate(Screen.AddEditProduct.createRoute())
                    },
                    onEditProduct = { productId ->
                        navController.navigate(Screen.AddEditProduct.createRoute(productId))
                    }
                )
            }
            
            // Agregar/Editar Producto
            composable(
                route = Screen.AddEditProduct.route,
                arguments = listOf(
                    navArgument("productId") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                val productsViewModel: ProductsViewModel = viewModel(factory = viewModelFactory)
                AddEditProductScreen(
                    productId = productId,
                    viewModel = productsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Ventas (placeholder)
            composable(Screen.Sales.route) {
                PlaceholderScreen(title = "Ventas")
            }
            
            // Promociones
            composable(Screen.Promotions.route) {
                val promotionsViewModel: PromotionsViewModel = viewModel(factory = viewModelFactory)
                PromotionsScreen(
                    viewModel = promotionsViewModel,
                    onAddPromotion = {
                        navController.navigate(Screen.AddEditPromotion.createRoute())
                    },
                    onEditPromotion = { promotionId ->
                        navController.navigate(Screen.AddEditPromotion.createRoute(promotionId))
                    }
                )
            }
            
            // Agregar/Editar Promoción
            composable(
                route = Screen.AddEditPromotion.route,
                arguments = listOf(
                    navArgument("promotionId") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val promotionId = backStackEntry.arguments?.getInt("promotionId") ?: 0
                val promotionsViewModel: PromotionsViewModel = viewModel(factory = viewModelFactory)
                AddEditPromotionScreen(
                    promotionId = promotionId,
                    viewModel = promotionsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Categorías
            composable(Screen.Categories.route) {
                val categoriesViewModel: CategoriesViewModel = viewModel(factory = viewModelFactory)
                CategoriesScreen(
                    viewModel = categoriesViewModel,
                    onAddCategory = {
                        navController.navigate(Screen.AddEditCategory.createRoute())
                    },
                    onEditCategory = { categoryId ->
                        navController.navigate(Screen.AddEditCategory.createRoute(categoryId))
                    }
                )
            }
            
            // Agregar/Editar Categoría
            composable(
                route = Screen.AddEditCategory.route,
                arguments = listOf(
                    navArgument("categoryId") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
                val categoriesViewModel: CategoriesViewModel = viewModel(factory = viewModelFactory)
                AddEditCategoryScreen(
                    categoryId = categoryId,
                    viewModel = categoriesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = "Pantalla de $title (próximamente)",
            modifier = Modifier.padding(16.dp)
        )
    }
}

