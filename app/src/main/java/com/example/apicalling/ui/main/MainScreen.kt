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
import androidx.compose.runtime.remember
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
import com.example.apicalling.ui.cart.CartScreen
import com.example.apicalling.ui.cart.CartViewModel
import com.example.apicalling.ui.category.CategoryDetailScreen
import com.example.apicalling.ui.category.CategoryDetailViewModel
import com.example.apicalling.ui.search.SearchScreen
import com.example.apicalling.ui.search.SearchViewModel
import com.example.apicalling.ui.favorites.FavoriteScreen
import com.example.apicalling.ui.favorites.FavoriteViewModel
import com.example.apicalling.ui.home.HomeScreen
import com.example.apicalling.ui.navigation.Screen
import com.example.apicalling.ui.product.ProductViewModel
import com.example.apicalling.ui.product.detail.ProductDetailScreen
import com.example.apicalling.ui.product.detail.ProductDetailViewModel
import com.example.apicalling.ui.profile.ProfileScreen
import com.example.apicalling.ui.profile.ProfileViewModel
import com.example.apicalling.domain.repository.FavoriteRepository

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Ana Sayfa", Icons.Default.Home)
    object Favorites : BottomNavItem(Screen.Favorites.route, "Favorilerim", Icons.Default.FavoriteBorder)
    object Cart : BottomNavItem(Screen.Cart.route, "Sepetim", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem(Screen.Profile.route, "Hesabım", Icons.Default.Person)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    favoriteRepository: FavoriteRepository // MainActivity'den enjekte edilen repo
) {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = hiltViewModel()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()

    val cartItems by cartViewModel.cartItems.collectAsState()
    val suggestedProducts by cartViewModel.suggestedProducts.collectAsState()
    
    // Favori ID'lerini anlık olarak takip ediyoruz
    val favoriteIds by favoriteRepository.favoriteIds.collectAsState()

    // Sayfa ilk açıldığında favorileri yükle (Terminoloji: Initial Session Sync)
    LaunchedEffect(Unit) {
        favoriteRepository.loadFavorites()
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
                                            if (item is BottomNavItem.Cart && cartItems.isNotEmpty()) {
                                            Badge {
                                                Text(text = cartItems.size.toString())
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
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Home.route) {
                val productViewModel: ProductViewModel = hiltViewModel()
                val productState by productViewModel.state.collectAsState()
                
                HomeScreen(
                    productState = productState,
                    favoriteIds = favoriteIds,
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
                    },
                    onSearch = { query ->
                        productViewModel.clearSuggestions() // Önerileri temizle
                        navController.navigate(Screen.Search.createRoute(query))
                    },
                    onSearchQueryChange = { query ->
                        productViewModel.onSearchQueryChanged(query)
                    },
                    onSuggestionClick = { selectedQuery ->
                        productViewModel.clearSuggestions()
                        navController.navigate(Screen.Search.createRoute(selectedQuery))
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
                    },
                    onSearch = { query ->
                        navController.navigate(Screen.Search.createRoute(query))
                    }
                )
            }
            composable(Screen.Search.route) {
                val searchViewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = searchViewModel,
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
                    },
                    onSearch = { query ->
                        // Mevcut arama sayfasındayken yeni arama yapılırsa
                        navController.navigate(Screen.Search.createRoute(query)) {
                            popUpTo(Screen.Search.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ProductDetail.route) {
                val detailViewModel: ProductDetailViewModel = hiltViewModel()
                val detailState by detailViewModel.state.collectAsState()
                val product = detailState.product
                
                ProductDetailScreen(
                    viewModel = detailViewModel,
                    cartItemCount = cartItems.size,
                    isFavorite = product?.let { favoriteIds.contains(it.id) } ?: false,
                    onBackClick = { navController.popBackStack() },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddToCart = { p -> cartViewModel.addToCart(p) },
                    onFavoriteClick = {
                        product?.let { favoriteViewModel.toggleFavorite(it.id) }
                    },
                    onSearch = { query ->
                        navController.navigate(Screen.Search.createRoute(query))
                    }
                )
            }
            composable(Screen.Cart.route) {
                val productViewModel: ProductViewModel = hiltViewModel()
                val products = productViewModel.state.collectAsState().value.products
                
                // Önerileri güncelle
                LaunchedEffect(products, cartItems) {
                    cartViewModel.updateSuggestedProducts(products)
                }

                CartScreen(
                    cartItems = cartItems,
                    favoriteProducts = favoriteViewModel.state.collectAsState().value.allFavoriteProducts,
                    suggestedProducts = suggestedProducts,
                    favoriteIds = favoriteIds,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onRemoveFromCart = { product ->
                        cartViewModel.removeFromCart(product)
                    },
                    onFavoriteClick = { productId ->
                        favoriteViewModel.toggleFavorite(productId)
                    },
                    onAddToCart = { product ->
                        cartViewModel.addToCart(product)
                    }
                )
            }
            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        favoriteRepository.clearData()
                        onLogout()
                    },
                    onFavoritesClick = {
                        // Terminoloji: Unified Tab Switch
                        // Profil sayfasından favorilere geçerken sanki alt bardaki ikona basılmış gibi davranıyoruz
                        navController.navigate(Screen.Favorites.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
