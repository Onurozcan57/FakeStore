package com.example.apicalling.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Profile : Screen("profile")
    object ProductList : Screen("products")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
}
