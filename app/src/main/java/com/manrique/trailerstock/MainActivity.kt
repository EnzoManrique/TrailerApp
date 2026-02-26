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
import androidx.compose.runtime.LaunchedEffect
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
import com.manrique.trailerstock.data.repository.UserPreferencesRepository
import com.manrique.trailerstock.data.backup.BackupManager
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
import com.manrique.trailerstock.ui.screens.sales.SalesScreen
import com.manrique.trailerstock.ui.screens.sales.SalesViewModel
import com.manrique.trailerstock.ui.screens.sales.CreateSaleScreen
import com.manrique.trailerstock.ui.screens.sales.CreateSaleViewModel
import com.manrique.trailerstock.ui.screens.settings.SettingsScreen
import com.manrique.trailerstock.ui.screens.about.AboutScreen
import com.manrique.trailerstock.utils.ExportManager
import com.manrique.trailerstock.ui.theme.TTMTheme

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
            database.ventaDetalleDao(),
            database.productoDao()
        )
        val categoriaRepository = CategoriaRepository(database.categoriaDao())
        val promocionRepository = PromocionRepository(
            database.promocionDao(),
            database.promocionProductoDao(),
            database.promocionMetodoPagoDao(),
            database.productoDao()
        )
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        val backupManager = BackupManager(applicationContext)
        val exportManager = ExportManager(applicationContext)
        
        // Crear ViewModelFactory
        viewModelFactory = ViewModelFactory(
            productoRepository,
            ventaRepository,
            categoriaRepository,
            promocionRepository,
            userPreferencesRepository,
            backupManager,
            database,
            exportManager
        )
        
        setContent {
            TTMTheme {
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
                
                // Helper para navegación unificada a nivel superior
                val navigateToTopLevel: (String) -> Unit = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                StatisticsScreen(
                    viewModel = statisticsViewModel,
                    onNavigateToProducts = {
                        navigateToTopLevel(Screen.Products.route)
                    },
                    onNavigateToSales = { range ->
                        navigateToTopLevel(Screen.Sales.createRoute(range))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Configuración
            composable(Screen.Settings.route) {
                val settingsViewModel: com.manrique.trailerstock.ui.screens.settings.SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }
            
            // Productos
            composable(route = Screen.Products.route) {
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
            
            // Ventas
            composable(
                route = Screen.Sales.route,
                arguments = listOf(
                    navArgument("range") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val range = backStackEntry.arguments?.getString("range")
                val salesViewModel: SalesViewModel = viewModel(factory = viewModelFactory)
                SalesScreen(
                    viewModel = salesViewModel,
                    initialRange = range,
                    onCreateSale = {
                        navController.navigate(Screen.CreateSale.route + "?isQuoteMode=false")
                    },
                    onCreateQuote = {
                        navController.navigate(Screen.CreateSale.route + "?isQuoteMode=true")
                    },
                    onSaleClick = { saleId ->
                        // TODO: Navegar a detalle de venta
                    }
                )
            }
            
            // Crear Venta (POS)
            composable(
                route = Screen.CreateSale.route + "?isQuoteMode={isQuoteMode}",
                arguments = listOf(
                    navArgument("isQuoteMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isQuoteMode = backStackEntry.arguments?.getBoolean("isQuoteMode") ?: false
                val createSaleViewModel: CreateSaleViewModel = viewModel(factory = viewModelFactory)
                
                // Configurar modo presupuesto
                LaunchedEffect(isQuoteMode) {
                    createSaleViewModel.setQuoteMode(isQuoteMode)
                }

                CreateSaleScreen(
                    viewModel = createSaleViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
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

            // Acerca de
            composable(Screen.About.route) {
                AboutScreen(
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

