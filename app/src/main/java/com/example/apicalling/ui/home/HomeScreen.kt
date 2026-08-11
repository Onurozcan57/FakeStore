package com.example.apicalling.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.ui.theme.APIcallingTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.components.ProductCardV2
import com.example.apicalling.ui.product.ProductState

data class Category(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color = Color.White
)

/**
 * Yeni Ana Sayfa tasarımı.
 */
@Composable
fun HomeScreen(
    productState: ProductState,
    onAddToCart: (ProductDto) -> Unit,
    onProductClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Kampanyalı ürünler için filtreleme (Örn: "beauty" kategorisinden ilk 10 ürün)
    val discountedProducts = productState.products
        .filter { it.category == "beauty" }
        .take(10)
    
    // Kampanya resimleri listesi
    val campaignImages = listOf(
        "https://via.placeholder.com/600x200/FF5722/FFFFFF?text=Kampanya+1",
        "https://via.placeholder.com/600x200/2196F3/FFFFFF?text=Kampanya+2",
        "https://via.placeholder.com/600x200/4CAF50/FFFFFF?text=Kampanya+3"
    )

    val categories = listOf(
        Category("Tüm\nKampanyalar", Icons.Default.Percent, Color(0xFFFF9800)),
        Category("Kategoriler", Icons.Default.Category, Color(0xFF2196F3)),
        Category("Giyim\nAyakkabı", Icons.Default.Checkroom, Color(0xFFE91E63)),
        Category("Telefon", Icons.Default.Devices, Color(0xFF9C27B0)),
        Category("Ev Yaşam", Icons.Default.Home, Color(0xFF4CAF50)),
        Category("Beyaz Eşya", Icons.Default.Kitchen, Color(0xFF607D8B))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Mavi Arka Planlı Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 16.dp, top = 0.dp, start = 16.dp, end = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ürün, kategori veya marka ara...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Ara", tint = Color.Gray)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
            }
        }

        // Kampanya Slider
        CampaignSlider(images = campaignImages)

        // Kategoriler Alanı
        CategorySection(categories = categories)

        // Kampanyadaki Ürünler Bölümü
        if (discountedProducts.isNotEmpty()) {
            HorizontalProductSection(
                title = "Kampanyadaki Ürünler",
                products = discountedProducts,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick
            )
        }

        // Sana Özel Kampanyalar Bölümü (Yeni - Promo Section)
        PromoSection(
            title = "Sana Özel Kampanyalar",
            promos = listOf(
                PromoItem("500 TL İndirim\nKaçırma", "https://dummyjson.com/public/img/products/1/thumbnail.jpg"),
                PromoItem("Anne & Çocuk\n%10 Net İndirim", "https://dummyjson.com/public/img/products/2/thumbnail.jpg"),
                PromoItem("Oyuncaklar\n%10 Net İndirim", "https://dummyjson.com/public/img/products/3/thumbnail.jpg"),
                PromoItem("Teknoloji\nUygun Fırsat", "https://dummyjson.com/public/img/products/4/thumbnail.jpg"),
                PromoItem("Spor & Outdoor\n%15 İndirim", "https://dummyjson.com/public/img/products/5/thumbnail.jpg")
            )
        )

        // Alt tarafa güvenli boşluk
        Spacer(modifier = Modifier.height(110.dp))
    }
}

data class PromoItem(val title: String, val imageUrl: String)

@Composable
fun PromoSection(title: String, promos: List<PromoItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.Black
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(110.dp) // Toplam yükseklik azaltıldı
        ) {
            items(promos) { promo ->
                PromoCard(promo = promo)
            }
        }
    }
}

@Composable
fun PromoCard(promo: PromoItem) {
    Card(
        modifier = Modifier
            .width(90.dp) // Genişlik artırıldı
            .height(95.dp) // Yükseklik azaltıldı
            .clickable { /* Kampanya tıklandı */ },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Üst Kısım: Resim ve Mavi Arka Plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f) // Oran dengelendi
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = promo.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp).fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Alt Kısım: Metin ve Gri Arka Plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(Color(0xFFEEEEEE))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = promo.title,
                    fontSize = 9.sp, // Daha geniş alan olduğu için font biraz büyütüldü
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HorizontalProductSection(
    title: String,
    products: List<ProductDto>,
    onAddToCart: (ProductDto) -> Unit,
    onProductClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.height(360.dp), // V2 kartlar için yükseklik artırıldı (Overflow düzeltildi)
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(products) { product ->
                ProductCardV2(
                    product = product,
                    onAddToCart = { onAddToCart(product) },
                    onProductClick = onProductClick,
                    modifier = Modifier.width(220.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySection(categories: List<Category>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryItem(category = category)
        }
    }
}

@Composable
fun CategoryItem(category: Category) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(category.backgroundColor.copy(alpha = 0.15f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                tint = category.backgroundColor,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.height(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.2,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun BrandSection(brands: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Popüler Markalar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            color = Color.Black
        )
        
        LazyRow(
            modifier = Modifier.height(70.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(brands) { logoUrl ->
                BrandItem(logoUrl = logoUrl)
            }
        }
    }
}

@Composable
fun BrandItem(logoUrl: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = "Marka Logosu",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    APIcallingTheme {
        HomeScreen(
            productState = ProductState(),
            onAddToCart = {},
            onProductClick = {}
        )
    }
}

@Composable
fun CampaignSlider(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = pagerState.currentPage) {
        delay(4000)
        val nextPage = (pagerState.currentPage + 1) % images.size
        scope.launch {
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 10.dp),
            pageSpacing = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { page ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = images[page],
                    contentDescription = "Kampanya ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Row(
            Modifier
                .height(20.dp)
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}
