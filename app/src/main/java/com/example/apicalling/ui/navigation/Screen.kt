package com.example.apicalling.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main") // Ana kapsayıcı (Bottom Nav bar burada olacak)
    object Home : Screen("home")
    object Market : Screen("market")
    object Profile : Screen("profile")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
}
