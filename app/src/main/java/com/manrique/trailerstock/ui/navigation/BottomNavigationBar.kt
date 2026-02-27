package com.manrique.trailerstock.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Bottom navigation bar con las 5 pantallas principales de la app.
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 12.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            navigationItems.forEach { item ->
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = item.icon, 
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    selected = currentRoute == item.route,
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        indicatorColor = Color.Transparent
                    ),
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
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
