package com.example.apicalling.ui.cart

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.components.ProductCardV1
import com.example.apicalling.ui.components.ProductCardV2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<ProductDto>,
    favoriteProducts: List<ProductDto>,
    suggestedProducts: List<ProductDto>,
    favoriteIds: Set<Int>,
    onProductClick: (Int) -> Unit,
    onRemoveFromCart: (ProductDto) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onAddToCart: (ProductDto) -> Unit
) {
    var isDetailExpanded by remember { mutableStateOf(false) }

    // Terminoloji: Pure Transparent Layout
    // Arka plan karmaşasını önlemek için Scaffold'u kaldırdık, Box yapısına geçtik
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Özel Header (Scaffold TopBar yerine)
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Text(
                    text = "Sepetim",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cartItems.isEmpty()) {
                EmptyCartView()
            } else {
                CartContentView(cartItems = cartItems, onRemove = onRemoveFromCart)
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

        // Kapsül Ödeme Barı (Terminoloji: Anchored Checkout Bar)
        if (cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp) // Nav barın hemen üzerinde, zarif bir boşlukla
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                CartBottomBar(
                    totalPrice = cartItems.sumOf { it.price },
                    isExpanded = isDetailExpanded,
                    onExpandClick = { isDetailExpanded = !isDetailExpanded }
                )
            }
        }
    }
}

@Composable
fun EmptyCartView() {
    BoxWithConstraints {
        val height = maxHeight * 0.35f
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
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
}

@Composable
fun CartContentView(cartItems: List<ProductDto>, onRemove: (ProductDto) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF9F9F9),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kuponlarım", fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
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
                Text("${product.price} $", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            val rows = favoriteProducts.chunked(2)
            rows.forEach { rowItems ->
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
        val rows = suggestedProducts.chunked(2)
        rows.forEach { rowItems ->
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
fun CartBottomBar(totalPrice: Double, isExpanded: Boolean, onExpandClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ürünler", color = Color.Gray, fontSize = 14.sp)
                        Text("${String.format("%.2f", totalPrice)} $", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kargo", color = Color.Gray, fontSize = 14.sp)
                        Text("Ücretsiz", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Surface(
            shadowElevation = 16.dp,
            color = Color.White,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${String.format("%.2f", totalPrice)} $", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = onExpandClick) {
                        Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Button(
                    onClick = { },
                    modifier = Modifier.height(50.dp).width(170.dp), // Genişlik ve yükseklik artırıldı
                    shape = RoundedCornerShape(25.dp), // Buton daha yuvarlak yapıldı
                    contentPadding = PaddingValues(horizontal = 8.dp), // İç boşluk optimize edildi
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Ödemeye Geç",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp, // Okunabilirlik için hafif büyütüldü
                        maxLines = 1
                    )
                }
            }
        }
    }
}
