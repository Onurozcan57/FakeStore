package com.example.apicalling.ui.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.apicalling.ui.cart.CartViewModel
import com.example.apicalling.ui.navigation.Screen
import com.example.apicalling.ui.product.ProductScreen
import com.example.apicalling.ui.product.ProductViewModel
import com.example.apicalling.ui.profile.ProfileScreen
import com.example.apicalling.ui.profile.ProfileViewModel

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Ana Sayfa", Icons.Default.Home)
    object Market : BottomNavItem(Screen.Market.route, "Market", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem(Screen.Profile.route, "Profil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Market,
        BottomNavItem.Profile
    )

    Scaffold(
        containerColor = Color.Transparent, // Şeffaf arka plan
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp), // Biraz daha yukarı aldık
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Hafif şeffaflık kattık
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { item ->

                        val selected =
                            currentDestination?.hierarchy?.any {
                                it.route == item.route
                            } == true

                        val iconSize by animateDpAsState(
                            targetValue = if (selected) 28.dp else 22.dp,
                            label = "iconSize"
                        )

                        NavigationBarItem(

                            selected = selected,

                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },

                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(iconSize)
                                )
                            },

                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },

                            colors = NavigationBarItemDefaults.colors(

                                indicatorColor = MaterialTheme.colorScheme.primary,

                                selectedIconColor = Color.White,

                                selectedTextColor = MaterialTheme.colorScheme.primary,

                                unselectedIconColor = Color.Gray,

                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }

        }
    ) { innerPadding ->
        // innerPadding.calculateBottomPadding()'i kullanmayarak içeriği barın altına kadar uzatıyoruz
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Sadece üst boşluğu koruyoruz
        ) {
            composable(Screen.Home.route) {
                // Ana Sayfa İçeriği
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Ana Sayfa - Yakında burada kampanyalar olacak!")
                }
            }
            composable(Screen.Market.route) {
                val viewModel: ProductViewModel = hiltViewModel()
                ProductScreen(
                    viewModel = viewModel,
                    cartItemCount = cartViewModel.cartItems.size,
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                    }
                )
            }
            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
