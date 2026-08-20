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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.ui.components.ProductCardV1
import com.example.apicalling.ui.components.ProductCardV2
import com.example.apicalling.util.PriceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    favoriteProducts: List<ProductDto>,
    suggestedProducts: List<ProductDto>,
    favoriteIds: Set<Int>,
    appliedCoupon: Coupon?,
    availableCoupons: List<Coupon>, // Yeni: Kullanıcının sahip olduğu kuponlar
    discount: Double,
    couponError: String?,
    onProductClick: (Int) -> Unit,
    onRemoveFromCart: (ProductDto) -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit, // Yeni: Miktar güncelleme
    onToggleSelection: (Int) -> Unit, // Yeni: Seçim durumu
    onFavoriteClick: (Int) -> Unit,
    onAddToCart: (ProductDto) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onClearError: () -> Unit,
    isPriceDroppedFilterActive: Boolean, // Yeni
    onTogglePriceDroppedFilter: () -> Unit, // Yeni
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
                        onUpdateQuantity = onUpdateQuantity,
                        onToggleSelection = onToggleSelection,
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
                    isFilterActive = isPriceDroppedFilterActive,
                    onFilterClick = onTogglePriceDroppedFilter,
                    onProductClick = onProductClick,
                    onFavoriteClick = onFavoriteClick,
                    onAddToCart = onAddToCart
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Terminoloji: Top-Level Window Popup
        // Snackbar'ı bir Popup içine alarak, MainActivity'deki Navbar dahil tüm 
        // UI elemanlarının üzerinde (Overlay) görünmesini sağlıyoruz.
        if (snackbarHostState.currentSnackbarData != null) {
            Popup(
                alignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp), // Navbar'ın tam üzerine binmesi için
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SnackbarHost(hostState = snackbarHostState)
                }
            }
        }

        if (cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 92.dp) // Navbar yüksekliği kadar (80dp + orijinal 12dp) yukarı taşıdık
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CartBottomBar(
                    totalPrice = cartItems.sumOf { it.product.price * it.quantity },
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
    cartItems: List<CartItem>, 
    onRemove: (ProductDto) -> Unit, 
    onUpdateQuantity: (Int, Int) -> Unit,
    onToggleSelection: (Int) -> Unit,
    onCouponClick: () -> Unit,
    appliedCoupon: Coupon?,
    onRemoveCoupon: () -> Unit
) {
    val totalTry = cartItems.filter { it.isSelected }.sumOf { it.product.price * it.quantity } * PriceUtils.USD_TO_TRY_RATE
    val isFreeShipping = totalTry >= 300.0

    Column(modifier = Modifier.padding(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onCouponClick() },
            shape = RoundedCornerShape(12.dp),
            color = if (appliedCoupon != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color(0xFFF9F9F9),
            border = BorderStroke(1.dp, if (appliedCoupon != null) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.weight(1f),
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
                        overflow = TextOverflow.Ellipsis
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
        cartItems.forEach { item ->
            CartItemCard(
                item = item, 
                isFreeShipping = isFreeShipping,
                onRemove = { onRemove(item.product) },
                onUpdateQuantity = { delta -> onUpdateQuantity(item.product.id, delta) },
                onToggleSelection = { onToggleSelection(item.product.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem, 
    isFreeShipping: Boolean,
    onRemove: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onToggleSelection: () -> Unit
) {
    val product = item.product
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.isSelected) 1f else 0.5f), // Seçili değilse kartı soluklaştırıyoruz
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column {
            if (isFreeShipping && item.isSelected) {
                // Terminoloji: Dynamic Promotion Badge
                // Kargo bedava satırı: Gri arka plan, özel renkli metinler ve ikonlar.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp) // Tam kaplamasın diye padding
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFF25921f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "300 TL üzerine ", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    Text(text = "kargo bedava", fontSize = 10.sp, color = Color(0xFF25921f), fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF25921f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "uygulandı", fontSize = 10.sp, color = Color(0xFF25921f), fontWeight = FontWeight.Bold)
                }
            }
            
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.isSelected, 
                    onCheckedChange = { onToggleSelection() }, 
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                    AsyncImage(model = product.thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(PriceUtils.formatUsdAsTry(product.price), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Terminoloji: Quantity Picker / Stepper
                    // Miktar seçici alanı: Fiyatın altında, gri border'lı bir kutu içinde.
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        color = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            // Sol taraf: Çöp veya Eksi ikonu
                            IconButton(
                                onClick = { 
                                    if (item.quantity == 1) onRemove() else onUpdateQuantity(-1) 
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.quantity == 1) Icons.Outlined.DeleteOutline else Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = if (item.quantity == 1) Color.Black else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Text(
                                text = item.quantity.toString(),
                                modifier = Modifier.padding(horizontal = 12.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            
                            // Sağ taraf: Artı ikonu
                            IconButton(
                                onClick = { onUpdateQuantity(1) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
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
fun SuggestionsSection(
    suggestedProducts: List<ProductDto>, 
    favoriteIds: Set<Int>, 
    isFilterActive: Boolean,
    onFilterClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit, 
    onAddToCart: (ProductDto) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bunlar da ilgini çekebilir", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            // Fiyatı Düşenler Filtre Butonu
            Surface(
                onClick = onFilterClick,
                color = if (isFilterActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (isFilterActive) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fiyatı Düşenler",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilterActive) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (suggestedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("Ürün bulunamadı.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
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
    val shippingLimit = 300.0 // Kargo bedava sınırı 300 TL
    val shippingFee = 60.0 // 60 TL sabit kargo ücreti
    
    val subtotalInTry = totalPrice * PriceUtils.USD_TO_TRY_RATE
    val discountInTry = discount // Zaten TL birimiyle geliyor
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
                            Text("-${PriceUtils.formatTry(discountInTry)}", color = Color.Red, fontWeight = FontWeight.Bold)
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
