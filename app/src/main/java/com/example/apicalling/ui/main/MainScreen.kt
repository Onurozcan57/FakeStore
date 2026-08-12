package com.example.apicalling.ui.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.apicalling.ui.category.CategoryDetailScreen
import com.example.apicalling.ui.category.CategoryDetailViewModel
import com.example.apicalling.ui.favorites.FavoriteScreen
import com.example.apicalling.ui.favorites.FavoriteViewModel
import com.example.apicalling.ui.home.HomeScreen
import com.example.apicalling.ui.navigation.Screen
import com.example.apicalling.ui.product.ProductViewModel
import com.example.apicalling.ui.product.detail.ProductDetailScreen
import com.example.apicalling.ui.product.detail.ProductDetailViewModel
import com.example.apicalling.ui.profile.ProfileScreen
import com.example.apicalling.ui.profile.ProfileViewModel
import com.example.apicalling.data.session.FavoriteManager

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Ana Sayfa", Icons.Default.Home)
    object Favorites : BottomNavItem(Screen.Favorites.route, "Favorilerim", Icons.Default.FavoriteBorder)
    object Cart : BottomNavItem(Screen.Cart.route, "Sepetim", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem(Screen.Profile.route, "Hesabım", Icons.Default.Person)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    favoriteManager: FavoriteManager // MainActivity'den enjekte edilen manager
) {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()

    // Favori ID'lerini anlık olarak takip ediyoruz
    val favoriteIds by favoriteManager.favoriteIds.collectAsState()

    // Sayfa ilk açıldığında favorileri yükle (Terminoloji: Initial Session Sync)
    LaunchedEffect(Unit) {
        favoriteManager.loadFavorites()
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Terminoloji: Dynamic Bottom Bar Visibility
    // Ürün detay sayfasındayken bottom bar'ı gizliyoruz.
    val showBottomBar = currentDestination?.route != Screen.ProductDetail.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Beyaz arka plan
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier.height(70.dp)
                    ) {
                        items.forEach { item ->
                            // Terminoloji: Navigation Hierarchy Mapping
                            // Kategori detay sayfası da Ana Sayfa akışının bir parçası olduğu için
                            // Home ikonunun seçili kalmasını sağlıyoruz.
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true ||
                                    (item is BottomNavItem.Home && currentDestination?.route == Screen.CategoryDetail.route)

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
                                    BadgedBox(
                                        badge = {
                                            if (item is BottomNavItem.Cart && cartViewModel.cartItems.isNotEmpty()) {
                                                Badge {
                                                    Text(text = cartViewModel.cartItems.size.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
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
        }
    ) { innerPadding ->
        // topPadding'i kaldırıyoruz, böylece içerik ekranın en tepesine kadar gidebilir.
        // Sadece alt padding'i (bottom bar için) her sayfanın kendi içinde yönetmesini sağlayacağız.
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                val productViewModel: ProductViewModel = hiltViewModel()
                HomeScreen(
                    productState = productViewModel.state.value,
                    favoriteIds = favoriteIds, // Artık anlık takip ediliyor
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                    },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                        println("butona tıklandı")
                    },
                    onCategoryClick = { slug ->
                        navController.navigate(Screen.CategoryDetail.createRoute(slug))
                    },
                    onFavoriteClick = { productId ->
                        favoriteViewModel.toggleFavorite(productId)
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoriteScreen(
                    viewModel = favoriteViewModel,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                    }
                )
            }
            composable(Screen.CategoryDetail.route) {
                val categoryViewModel: CategoryDetailViewModel = hiltViewModel()
                CategoryDetailScreen(
                    viewModel = categoryViewModel,
                    favoriteIds = favoriteIds,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                    },
                    onFavoriteClick = { productId ->
                        favoriteViewModel.toggleFavorite(productId)
                    }
                )
            }
            composable(Screen.ProductDetail.route) {
                val detailViewModel: ProductDetailViewModel = hiltViewModel()
                ProductDetailScreen(
                    viewModel = detailViewModel,
                    cartItemCount = cartViewModel.cartItems.size, // Terminoloji: Data Propagation
                    onBackClick = { navController.popBackStack() },
                    onCartClick = {
                            navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                                 }
                                launchSingleTop = true
                                  restoreState = true
                            }
                        println("butona tıklandı")
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                    }
                )
            }
            composable(Screen.Cart.route) {
                // Sepet İçeriği
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Sepet Sayfası - Yakında burada ürünlerinizi göreceksiniz!")
                }
            }
            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        favoriteManager.clearData() // Favori listesini temizle
                        onLogout()
                    }
                )
            }
        }
    }
}
