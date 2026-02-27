package com.manrique.trailerstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    
    // Rutas que deben mostrar el top bar
    val routesWithTopBar = listOf(
        Screen.Statistics.route,
        Screen.Products.route,
        Screen.Sales.route,
        Screen.Promotions.route,
        Screen.Categories.route,
        Screen.Settings.route,
        Screen.About.route,
        Screen.AddEditProduct.route,
        Screen.AddEditCategory.route,
        Screen.AddEditPromotion.route,
        Screen.CreateSale.route + "?isQuoteMode={isQuoteMode}"
    )
    
    // Rutas con Bottom Bar (solo las principales)
    val routesWithBottomBar = listOf(
        Screen.Statistics.route,
        Screen.Products.route,
        Screen.Sales.route,
        Screen.Promotions.route,
        Screen.Categories.route
    )
    
    val showTopBar = routesWithTopBar.any { route -> 
        currentRoute?.startsWith(route.split("/")[0].split("?")[0]) == true 
    }
    val showBottomBar = currentRoute in routesWithBottomBar
    
    // Rutas que necesitan botón de atrás
    val needsBackButton = currentRoute !in routesWithBottomBar && currentRoute != null

    // Obtener título dinámico para la TopBar
    val screenTitle = when {
        currentRoute == Screen.Statistics.route -> stringResource(R.string.label_business_summary)
        currentRoute == Screen.Products.route -> stringResource(R.string.menu_inventory)
        currentRoute?.startsWith("sales") == true -> stringResource(R.string.menu_sales)
        currentRoute == Screen.Promotions.route -> stringResource(R.string.menu_promotions)
        currentRoute == Screen.Categories.route -> stringResource(R.string.menu_categories)
        currentRoute == Screen.Settings.route -> stringResource(R.string.menu_settings)
        currentRoute == Screen.About.route -> stringResource(R.string.label_about)
        currentRoute?.startsWith("add_edit_product") == true -> {
            val productId = navBackStackEntry?.arguments?.getInt("productId") ?: 0
            if (productId != 0) stringResource(R.string.edit_product) else stringResource(R.string.label_new_product)
        }
        currentRoute?.startsWith("add_edit_category") == true -> {
            val categoryId = navBackStackEntry?.arguments?.getInt("categoryId") ?: 0
            if (categoryId != 0) "Editar Categoría" else "Nueva Categoría"
        }
        currentRoute?.startsWith("add_edit_promotion") == true -> {
            val promotionId = navBackStackEntry?.arguments?.getInt("promotionId") ?: 0
            if (promotionId != 0) "Editar Promoción" else "Nueva Promoción"
        }
        currentRoute?.startsWith("create_sale") == true -> {
            val isQuote = navBackStackEntry?.arguments?.getBoolean("isQuoteMode") ?: false
            if (isQuote) "Nuevo Presupuesto" else "Nueva Venta"
        }
        else -> ""
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    shadowElevation = 10.dp
                ) {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { Text(screenTitle) },
                        navigationIcon = {
                            if (needsBackButton) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = stringResource(R.string.action_back)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        actions = {
                            if (currentRoute == Screen.Statistics.route) {
                                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(R.string.menu_settings)
                                    )
                                }
                            }
                            
                            // Mostrar icono de eliminar si estamos editando y no es un nuevo elemento
                            val isEditing = when {
                                currentRoute?.startsWith("add_edit_product") == true -> (navBackStackEntry?.arguments?.getInt("productId") ?: 0) != 0
                                currentRoute?.startsWith("add_edit_category") == true -> (navBackStackEntry?.arguments?.getInt("categoryId") ?: 0) != 0
                                currentRoute?.startsWith("add_edit_promotion") == true -> (navBackStackEntry?.arguments?.getInt("promotionId") ?: 0) != 0
                                else -> false
                            }
                            
                            if (isEditing) {
                                IconButton(onClick = { 
                                    // Notificar a la pantalla actual que se pulsó eliminar
                                    // Esto requiere una forma de comunicación, por ahora solo mostramos el icono
                                    // Para que sea funcional, las pantallas deben escuchar este evento o usaremos un SharedViewModel
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.action_delete)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
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

