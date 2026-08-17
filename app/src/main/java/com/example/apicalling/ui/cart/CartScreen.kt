package com.example.apicalling.ui.cart

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.ui.components.ProductCardV1
import com.example.apicalling.ui.components.ProductCardV2
import com.example.apicalling.util.PriceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<ProductDto>,
    favoriteProducts: List<ProductDto>,
    suggestedProducts: List<ProductDto>,
    favoriteIds: Set<Int>,
    appliedCoupon: Coupon?,
    availableCoupons: List<Coupon>, // Yeni: Kullanıcının sahip olduğu kuponlar
    discount: Double,
    couponError: String?,
    onProductClick: (Int) -> Unit,
    onRemoveFromCart: (ProductDto) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onAddToCart: (ProductDto) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onClearError: () -> Unit,
    onCheckoutClick: () -> Unit // Yeni: Ödemeye geçiş aksiyonu
) {
    var isDetailExpanded by remember { mutableStateOf(false) }
    var showCouponSheet by remember { mutableStateOf(false) }
    val couponSheetState = rememberModalBottomSheetState()
    var isAddingCoupon by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(couponError) {
        couponError?.let {
            snackbarHostState.showSnackbar(it)
            onClearError() // Terminoloji: Consuming the Event
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            snackbarHost = { 
                // Terminoloji: Offset Snackbar Host
                // Uyarı mesajlarını ödeme barının üstünde göstermek için padding ekledik
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 100.dp) 
                ) 
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 0.5.dp
                ) {
                    Text(
                        text = "Sepetim",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cartItems.isEmpty()) {
                    EmptyCartView()
                } else {
                    CartContentView(
                        cartItems = cartItems, 
                        onRemove = onRemoveFromCart,
                        onCouponClick = { showCouponSheet = true },
                        appliedCoupon = appliedCoupon,
                        onRemoveCoupon = onRemoveCoupon
                    )
                }

                FavoritesSection(
                    favoriteProducts = favoriteProducts,
                    favoriteIds = favoriteIds,
                    onProductClick = onProductClick,
                    onFavoriteClick = onFavoriteClick,
                    onAddToCart = onAddToCart
                )

                SuggestionsSection(
                    suggestedProducts = suggestedProducts,
                    favoriteIds = favoriteIds,
                    onProductClick = onProductClick,
                    onFavoriteClick = onFavoriteClick,
                    onAddToCart = onAddToCart
                )

                Spacer(modifier = Modifier.height(180.dp))
            }
        }

        if (cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CartBottomBar(
                    totalPrice = cartItems.sumOf { it.price },
                    discount = discount,
                    isExpanded = isDetailExpanded,
                    onExpandClick = { isDetailExpanded = !isDetailExpanded },
                    onCheckoutClick = onCheckoutClick
                )
            }
        }
    }

    if (showCouponSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showCouponSheet = false
                isAddingCoupon = false 
            },
            sheetState = couponSheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tüm kuponlarım",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { isAddingCoupon = !isAddingCoupon }) {
                        Icon(if (isAddingCoupon) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (isAddingCoupon) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            label = { Text("Kupon Kodu Giriniz") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                if (couponInput.isNotBlank()) {
                                    onApplyCoupon(couponInput)
                                    showCouponSheet = false
                                    isAddingCoupon = false
                                    couponInput = ""
                                }
                            }, 
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ekle", fontWeight = FontWeight.Bold)
                        }
                    } else if (availableCoupons.isNotEmpty()) {
                        // Var olan kuponların listesi
                        availableCoupons.filter { !it.isUsed }.forEach { coupon ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        onApplyCoupon(coupon.code)
                                        showCouponSheet = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                                color = Color(0xFFF9F9F9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = coupon.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = coupon.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("Aktif kuponunuz bulunmamaktadır.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView() {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cartHeight = screenHeight * 0.45f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(cartHeight)
            .background(Color(0xFFF9F9F9)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Sepetin şu an boş", fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.6f), fontSize = 18.sp)
        Text("Sepetini FakeStore ile doldur fırsatları yakala", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun CartContentView(
    cartItems: List<ProductDto>, 
    onRemove: (ProductDto) -> Unit, 
    onCouponClick: () -> Unit,
    appliedCoupon: Coupon?,
    onRemoveCoupon: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onCouponClick() },
            shape = RoundedCornerShape(12.dp),
            color = if (appliedCoupon != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color(0xFFF9F9F9),
            border = BorderStroke(1.dp, if (appliedCoupon != null) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.weight(1f), // Metin alanına esneklik verdik (Terminoloji: Flexible Layout)
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ConfirmationNumber, 
                        contentDescription = null, 
                        tint = if (appliedCoupon != null) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = appliedCoupon?.let { "Uygulanan: ${it.code}" } ?: "Kuponlarım", 
                        fontWeight = FontWeight.Bold,
                        color = if (appliedCoupon != null) MaterialTheme.colorScheme.primary else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // Uzun kodlarda taşmayı önler
                    )
                }
                
                if (appliedCoupon != null) {
                    Text(
                        text = "Kaldır", 
                        color = Color.Red, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { onRemoveCoupon() }
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        cartItems.forEach { product ->
            CartItemCard(product = product, onRemove = { onRemove(product) })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CartItemCard(product: ProductDto, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = true, onCheckedChange = { }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                AsyncImage(model = product.thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(PriceUtils.formatUsdAsTry(product.price), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.LightGray) }
        }
    }
}

@Composable
fun FavoritesSection(favoriteProducts: List<ProductDto>, favoriteIds: Set<Int>, onProductClick: (Int) -> Unit, onFavoriteClick: (Int) -> Unit, onAddToCart: (ProductDto) -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.Start) {
            Text("Beğendiklerim", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.width(80.dp).height(2.dp).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (favoriteProducts.isEmpty()) {
            Text("Favori ürününüz bulunmamaktadır.", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = Color.Gray)
        } else {
            favoriteProducts.chunked(2).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (product in rowItems) {
                        ProductCardV1(product = product, onAddToCart = { onAddToCart(product) }, isFavorite = favoriteIds.contains(product.id), onFavoriteClick = { onFavoriteClick(product.id) }, onProductClick = onProductClick, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SuggestionsSection(suggestedProducts: List<ProductDto>, favoriteIds: Set<Int>, onProductClick: (Int) -> Unit, onFavoriteClick: (Int) -> Unit, onAddToCart: (ProductDto) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Text("Bunlar da ilgini çekebilir", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        suggestedProducts.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (product in rowItems) {
                    ProductCardV2(product = product, onAddToCart = { onAddToCart(product) }, isFavorite = favoriteIds.contains(product.id), onFavoriteClick = { onFavoriteClick(product.id) }, onProductClick = onProductClick, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CartBottomBar(
    totalPrice: Double, 
    discount: Double, 
    isExpanded: Boolean, 
    onExpandClick: () -> Unit,
    buttonText: String = "Ödemeye Geç",
    isEnabled: Boolean = true, // Yeni: Buton aktiflik durumu
    onCheckoutClick: () -> Unit = {}
) {
    val shippingLimit = 50.0 * PriceUtils.USD_TO_TRY_RATE
    val shippingFee = 10.0 * PriceUtils.USD_TO_TRY_RATE
    
    val subtotalInTry = totalPrice * PriceUtils.USD_TO_TRY_RATE
    val discountInTry = discount * PriceUtils.USD_TO_TRY_RATE
    val isFreeShipping = subtotalInTry >= shippingLimit
    val actualShipping = if (isFreeShipping || subtotalInTry == 0.0) 0.0 else shippingFee
    val finalTotal = subtotalInTry - discountInTry + actualShipping

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = isExpanded, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
            Surface(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(), color = Color.White, shadowElevation = 12.dp, shape = RoundedCornerShape(24.dp), border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.2f))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ara Toplam", color = Color.Gray, fontSize = 14.sp)
                        Text(PriceUtils.formatUsdAsTry(totalPrice), fontWeight = FontWeight.Bold)
                    }
                    if (discountInTry > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kupon İndirimi", color = Color.Gray, fontSize = 14.sp)
                            Text("-${PriceUtils.formatUsdAsTry(discount)}", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kargo", color = Color.Gray, fontSize = 14.sp)
                        if (isFreeShipping) Text("Ücretsiz", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        else Text(PriceUtils.formatTry(shippingFee), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Surface(shadowElevation = 16.dp, color = Color.White, shape = RoundedCornerShape(32.dp), border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.1f))) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = PriceUtils.formatTry(if (finalTotal < 0) 0.0 else finalTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = onExpandClick) { Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                }
                Button(
                    onClick = onCheckoutClick, 
                    enabled = isEnabled, // Buraya ekledik
                    modifier = Modifier.height(50.dp).width(180.dp), 
                    shape = RoundedCornerShape(25.dp), 
                    contentPadding = PaddingValues(horizontal = 8.dp), 
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f) // Pasif renk
                    )
                ) {
                    Text(text = buttonText, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp, maxLines = 1)
                }
            }
        }
    }
}
