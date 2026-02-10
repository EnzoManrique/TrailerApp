package com.manrique.trailerstock.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

/**
 * Bottom navigation bar con las 5 pantallas principales de la app.
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.label
                    ) 
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building large back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Data class para los items del bottom navigation
 */
private data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Lista de items del bottom navigation
 */
private val navigationItems = listOf(
    NavigationItem(
        route = Screen.Statistics.route,
        icon = Icons.Default.TrendingUp,
        label = "Estadísticas"
    ),
    NavigationItem(
        route = Screen.Products.route,
        icon = Icons.Default.Inventory,
        label = "Productos"
    ),
    NavigationItem(
        route = Screen.Sales.route,
        icon = Icons.Default.ShoppingCart,
        label = "Ventas"
    ),
    NavigationItem(
        route = Screen.Promotions.route,
        icon = Icons.Default.LocalOffer,
        label = "Promociones"
    ),
    NavigationItem(
        route = Screen.Categories.route,
        icon = Icons.Default.Category,
        label = "Categorías"
    )
)
