package com.example.apicalling.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main") // Ana kapsayıcı (Bottom Nav bar burada olacak)
    object Home : Screen("home")
    object Market : Screen("market")
    object Profile : Screen("profile")
    object Favorites : Screen("favorites") // Yeni: Favoriler Rotası
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    object CategoryDetail : Screen("category_detail/{categorySlug}") {
        fun createRoute(categorySlug: String) = "category_detail/$categorySlug"
    }
    object Search : Screen("search/{query}") {
        fun createRoute(query: String) = "search/$query"
    }
}
