package com.example.apicalling

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.apicalling.domain.repository.FavoriteRepository
import com.example.apicalling.domain.repository.SessionRepository
import com.example.apicalling.ui.cart.CartScreen
import com.example.apicalling.ui.cart.CartItem
import com.example.apicalling.ui.cart.CartViewModel
import com.example.apicalling.ui.category.CategoryDetailScreen
import com.example.apicalling.ui.category.CategoryDetailViewModel
import com.example.apicalling.ui.checkout.CheckoutScreen
import com.example.apicalling.ui.checkout.CheckoutViewModel
import com.example.apicalling.ui.coupon.CouponScreen
import com.example.apicalling.ui.coupon.CouponViewModel
import com.example.apicalling.ui.favorites.FavoriteScreen
import com.example.apicalling.ui.favorites.FavoriteViewModel
import com.example.apicalling.ui.home.HomeScreen
import com.example.apicalling.ui.login.LoginScreen
import com.example.apicalling.ui.login.LoginViewModel
import com.example.apicalling.ui.navigation.Screen
import com.example.apicalling.ui.product.ProductViewModel
import com.example.apicalling.ui.product.detail.ProductDetailScreen
import com.example.apicalling.ui.product.detail.ProductDetailViewModel
import com.example.apicalling.ui.profile.ProfileScreen
import com.example.apicalling.ui.profile.ProfileViewModel
import com.example.apicalling.ui.search.SearchScreen
import com.example.apicalling.ui.search.SearchViewModel
import com.example.apicalling.ui.theme.APIcallingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Ana Sayfa", Icons.Outlined.Home)
    object Favorites : BottomNavItem(Screen.Favorites.route, "Favorilerim", Icons.Default.FavoriteBorder)
    object Cart : BottomNavItem(Screen.Cart.route, "Sepetim", Icons.Outlined.ShoppingCart)
    object Profile : BottomNavItem(Screen.Profile.route, "Hesabım", Icons.Outlined.Person)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var favoriteRepository: FavoriteRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIcallingTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                val cartViewModel: CartViewModel = hiltViewModel()
                val favoriteViewModel: FavoriteViewModel = hiltViewModel()
                
                val favoriteIds by favoriteRepository.favoriteIds.collectAsState()

                LaunchedEffect(Unit) {
                    favoriteRepository.loadFavorites()
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val showBottomBar = currentDestination?.route in listOf(
                    Screen.Home.route,
                    Screen.Favorites.route,
                    Screen.Cart.route,
                    Screen.Profile.route,
                    Screen.CategoryDetail.route,
                    Screen.Search.route
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = if (sessionRepository.isLoggedIn()) Screen.Home.route else Screen.Login.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                viewModel = hiltViewModel(),
                                onLoginSuccess = { name ->
                                    Toast.makeText(context, "Hoş geldin $name!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Home.route) {
                            val productViewModel: ProductViewModel = hiltViewModel()
                            val productState by productViewModel.state.collectAsState()
                            HomeScreen(
                                productState = productState,
                                favoriteIds = favoriteIds,
                                onAddToCart = { cartViewModel.addToCart(it) },
                                onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                                onCategoryClick = { navController.navigate(Screen.CategoryDetail.createRoute(it)) },
                                onFavoriteClick = { favoriteViewModel.toggleFavorite(it) },
                                onSearch = { navController.navigate(Screen.Search.createRoute(it)) },
                                onSearchQueryChange = { productViewModel.onSearchQueryChanged(it) },
                                onSuggestionClick = { 
                                    productViewModel.clearSuggestions()
                                    navController.navigate(Screen.Search.createRoute(it))
                                }
                            )
                        }

                        composable(Screen.Favorites.route) {
                            FavoriteScreen(
                                viewModel = favoriteViewModel,
                                onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                                onAddToCart = { cartViewModel.addToCart(it) }
                            )
                        }

                        composable(Screen.Cart.route) {
                            val productViewModel: ProductViewModel = hiltViewModel()
                            val products = productViewModel.state.collectAsState().value.products
                            val couponViewModel: CouponViewModel = hiltViewModel()
                            val couponState by couponViewModel.state.collectAsState()
                            
                            val cartItems by cartViewModel.cartItems.collectAsState()
                            val appliedCoupon by cartViewModel.appliedCoupon.collectAsState()
                            val discount by cartViewModel.discount.collectAsState()
                            val couponError by cartViewModel.couponError.collectAsState()
                            val suggestedProducts by cartViewModel.suggestedProducts.collectAsState()
                            val isPriceDroppedFilterActive by cartViewModel.isPriceDroppedFilterActive.collectAsState()
                            
                            LaunchedEffect(products) {
                                cartViewModel.updateSuggestedProducts(products)
                            }

                            CartScreen(
                                cartItems = cartItems,
                                favoriteProducts = favoriteViewModel.state.collectAsState().value.allFavoriteProducts,
                                suggestedProducts = suggestedProducts,
                                favoriteIds = favoriteIds,
                                appliedCoupon = appliedCoupon,
                                availableCoupons = couponState.coupons,
                                discount = discount,
                                couponError = couponError,
                                onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                                onRemoveFromCart = { cartViewModel.removeFromCart(it) },
                                onUpdateQuantity = { id, delta -> cartViewModel.updateQuantity(id, delta) },
                                onToggleSelection = { cartViewModel.toggleSelection(it) },
                                onFavoriteClick = { favoriteViewModel.toggleFavorite(it) },
                                onAddToCart = { cartViewModel.addToCart(it) },
                                onApplyCoupon = { cartViewModel.applyCoupon(it) },
                                onRemoveCoupon = { cartViewModel.removeCoupon() },
                                onClearError = { cartViewModel.clearCouponError() },
                                isPriceDroppedFilterActive = isPriceDroppedFilterActive,
                                onTogglePriceDroppedFilter = { cartViewModel.togglePriceDroppedFilter() },
                                onCheckoutClick = { navController.navigate(Screen.Checkout.route) }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = hiltViewModel(),
                                onLogout = {
                                    sessionRepository.clearSession()
                                    favoriteRepository.clearData()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                                onCouponsClick = { navController.navigate(Screen.Coupons.route) }
                            )
                        }

                        composable(Screen.ProductDetail.route) {
                            val detailViewModel: ProductDetailViewModel = hiltViewModel()
                            val detailState by detailViewModel.state.collectAsState()
                            ProductDetailScreen(
                                viewModel = detailViewModel,
                                cartItemCount = cartViewModel.cartItems.collectAsState().value.size,
                                isFavorite = detailState.product?.let { favoriteIds.contains(it.id) } ?: false,
                                onBackClick = { navController.popBackStack() },
                                onCartClick = { navController.navigate(Screen.Cart.route) },
                                onAddToCart = { cartViewModel.addToCart(it) },
                                onFavoriteClick = { detailState.product?.let { favoriteViewModel.toggleFavorite(it.id) } },
                                onSearch = { navController.navigate(Screen.Search.createRoute(it)) }
                            )
                        }

                        composable(Screen.CategoryDetail.route) {
                            val categoryViewModel: CategoryDetailViewModel = hiltViewModel()
                            CategoryDetailScreen(
                                viewModel = categoryViewModel,
                                favoriteIds = favoriteIds,
                                onBackClick = { navController.popBackStack() },
                                onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                                onAddToCart = { cartViewModel.addToCart(it) },
                                onFavoriteClick = { favoriteViewModel.toggleFavorite(it) },
                                onSearch = { navController.navigate(Screen.Search.createRoute(it)) }
                            )
                        }

                        composable(Screen.Search.route) {
                            SearchScreen(
                                viewModel = hiltViewModel(),
                                favoriteIds = favoriteIds,
                                onBackClick = { navController.popBackStack() },
                                onProductClick = { navController.navigate(Screen.ProductDetail.createRoute(it)) },
                                onAddToCart = { cartViewModel.addToCart(it) },
                                onFavoriteClick = { favoriteViewModel.toggleFavorite(it) },
                                onSearch = { navController.navigate(Screen.Search.createRoute(it)) }
                            )
                        }

                        composable(Screen.Coupons.route) {
                            CouponScreen(
                                viewModel = hiltViewModel(),
                                onBackClick = { navController.popBackStack() },
                                onUseCoupon = { 
                                    cartViewModel.applyCoupon(it)
                                    navController.navigate(Screen.Cart.route) 
                                }
                            )
                        }

                        composable(Screen.Checkout.route) {
                            val discount by cartViewModel.discount.collectAsState()
                            val appliedCoupon by cartViewModel.appliedCoupon.collectAsState()
                            CheckoutScreen(
                                viewModel = hiltViewModel(),
                                cartItems = cartViewModel.cartItems.collectAsState().value,
                                discount = discount,
                                appliedCoupon = appliedCoupon,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }

                    if (showBottomBar) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                            val cartItemsByState by cartViewModel.cartItems.collectAsState()
                            AppBottomBar(navController, currentDestination, cartItemsByState.size)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomBar(navController: androidx.navigation.NavHostController, currentDestination: androidx.navigation.NavDestination?, cartSize: Int) {
    val items = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Favorites,
            BottomNavItem.Cart,
            BottomNavItem.Profile
        )
    }
    
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
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true ||
                        (item is BottomNavItem.Home && currentDestination?.route == Screen.CategoryDetail.route)

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
                                if (item is BottomNavItem.Cart && cartSize > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) { 
                                        Text(text = cartSize.toString()) 
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = item.icon, contentDescription = item.title)
                        }
                    },
                    label = { Text(text = item.title) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.White,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}
