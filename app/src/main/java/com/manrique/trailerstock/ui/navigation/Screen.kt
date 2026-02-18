package com.manrique.trailerstock.ui.navigation

/**
 * Sealed class que define las rutas de navegación de la aplicación.
 * 
 * Cada pantalla tiene su propia ruta única para la navegación.
 */
sealed class Screen(val route: String) {
    object Statistics : Screen("statistics")
    object Products : Screen("products")
    object Sales : Screen("sales")
    object Promotions : Screen("promotions")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
    object AddEditProduct : Screen("add_edit_product/{productId}") {
        fun createRoute(productId: Int? = null): String {
            return if (productId != null) {
                "add_edit_product/$productId"
            } else {
                "add_edit_product/0"
            }
        }
    }
    object AddEditCategory : Screen("add_edit_category/{categoryId}") {
        fun createRoute(categoryId: Int? = null): String {
            return if (categoryId != null) {
                "add_edit_category/$categoryId"
            } else {
                "add_edit_category/0"
            }
        }
    }
    object AddEditPromotion : Screen("add_edit_promotion/{promotionId}") {
        fun createRoute(promotionId: Int? = null): String {
            return if (promotionId != null) {
                "add_edit_promotion/$promotionId"
            } else {
                "add_edit_promotion/0"
            }
        }
    }
    object CreateSale : Screen("create_sale")
}
