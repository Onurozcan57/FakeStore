package com.example.apicalling.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.R
import com.example.apicalling.ui.theme.APIcallingTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.components.ProductCardV1
import com.example.apicalling.ui.components.ProductCardV2
import com.example.apicalling.ui.product.ProductState

data class Category(
    val title: String,
    val imageUrl: String,
    val slug: String
)

data class PromoItem(val imageUrl: String)

/**
 * Yeni Ana Sayfa tasarımı.
 */
@Composable
fun HomeScreen(
    productState: ProductState,
    favoriteIds: Set<Int>,
    onAddToCart: (ProductDto) -> Unit,
    onProductClick: (Int) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val campaignImages = listOf(
        R.drawable.kampanya_2,
        R.drawable.kampanya_1,
        R.drawable.kampanya_3,
        R.drawable.kampanya_4
    )

    // 🏗️ ANA KONTEYNER (Overlay-First Architecture)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                enabled = productState.isSearching,
                onClick = { 
                    focusManager.clearFocus() // Klavyeyi ve odaklanmayı kapat
                    onSearchQueryChange("") // Arama modundan çık (ViewModel üzerinden isSearching=false)
                }
            )
    ) {
        // 📜 1. KATMAN: KAYDIRILABİLİR İÇERİK (Arka planda kayar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header yüksekliği kadar boşluk bırakıyoruz ki içerik Header'ın altında kalmasın
            Spacer(modifier = Modifier.height(100.dp))

            CampaignSlider(images = campaignImages)

            if (productState.categories.isNotEmpty()) {
                CategorySection(categories = productState.categories, onCategoryClick = onCategoryClick)
            }

            if (productState.products.isNotEmpty()) {
                val fragrancesProducts = productState.products.filter { it.category == "fragrances" }.take(10)
                if (fragrancesProducts.isNotEmpty()) {
                    HorizontalProductSection(
                        title = "Son Gezdiğin Ürünler",
                        products = fragrancesProducts,
                        favoriteIds = favoriteIds,
                        onAddToCart = onAddToCart,
                        onProductClick = onProductClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }

            PromoSection(
                title = "Sana Özel Kampanyalar",
                promos = listOf(
                    PromoItem( "https://images.hepsiburada.net/banners/s/1/180-180/gra-213166-tr_(1)134314451170274296.png"),
                    PromoItem( "https://images.hepsiburada.net/banners/s/1/180-180/hepsipay_masrafsiz_kredi-tr134166645835646551.png"),
                    PromoItem( "https://images.hepsiburada.net/banners/s/1/180-180/gra-208444-elektronik-jenerik-tr134178755376258479.png"),
                    PromoItem( "https://images.hepsiburada.net/banners/s/1/180-180/teknoloji_urunleri-tr134166457786456878134245119221869584.png"),
                    PromoItem( "https://images.hepsiburada.net/banners/s/1/180-180/flas_teklif_flas_fiyat-tr-1134283971375217370.png")
                )
            )
            CampaignSlider(images = campaignImages)
            if (productState.randomProducts.isNotEmpty()) {
                GridProductSection(
                    title = "Günün Fırsatları",
                    products = productState.randomProducts,
                    favoriteIds = favoriteIds,
                    onAddToCart = onAddToCart,
                    onProductClick = onProductClick,
                    onFavoriteClick = onFavoriteClick
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }

        // 🏛️ 2. KATMAN: SABİT MAVİ ÜST BAR (Terminoloji: Solid Sticky Header)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape( bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .statusBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Arama Çubuğu
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Ara", tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    onSearchQueryChange(it)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { if (searchQuery.isNotBlank()) onSearch(searchQuery) }),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) Text(text = "Ürün, kategori veya marka ara", color = Color.DarkGray, fontSize = 13.sp)
                                    innerTextField()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 🏛️ 3. KATMAN: ÖNERİ LİSTESİ (Terminoloji: Solid Floating Suggestions)
        if (productState.isSearching && productState.searchSuggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 98.dp) // AppBar'ın hemen altından başlasın
                    .background(Color.White) // Solid beyaz arka plan istedin, yaptık kral
                    .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                    .wrapContentHeight()
            ) {
                productState.searchSuggestions.take(6).forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSuggestionClick(suggestion.title)
                                searchQuery = suggestion.title
                                focusManager.clearFocus() // Seçim yapınca da odağı kapat
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = suggestion.title, fontSize = 14.sp, color = Color.DarkGray)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun CampaignSlider(images: List<Any>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = pagerState.currentPage) {
        delay(4000)
        val nextPage = (pagerState.currentPage + 1) % images.size
        scope.launch { pagerState.animateScrollToPage(page = nextPage, animationSpec = tween(durationMillis = 1000)) }
    }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            HorizontalPager(
                state = pagerState, 
                contentPadding = PaddingValues(horizontal = 10.dp), 
                pageSpacing = 10.dp, 
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Card(
                    modifier = Modifier.fillMaxSize().clickable { }, 
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


            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 4.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${images.size}",
                    color = Color.White,
                   // fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                )
            }

        }
    }
}

@Composable
fun CategorySection(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        LazyRow(modifier = Modifier.height(76.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            items(categories) { category ->
                CategoryCard(category = category, onClick = { onCategoryClick(category.slug) })
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(modifier = Modifier.width(72.dp).height(72.dp).clickable { onClick() }, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.65f).padding(0.dp), contentAlignment = Alignment.Center) {
                AsyncImage(model = category.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
            }
            Box(modifier = Modifier.fillMaxWidth().weight(0.35f).padding(bottom = 4.dp, start = 4.dp, end = 4.dp), contentAlignment = Alignment.Center) {
                Text(text = category.title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun HorizontalProductSection(
    title: String, 
    products: List<ProductDto>, 
    favoriteIds: Set<Int>,
    onAddToCart: (ProductDto) -> Unit, 
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 0.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge,fontSize= 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(
            modifier = Modifier.wrapContentHeight(), 
            contentPadding = PaddingValues(horizontal = 12.dp), 
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(products) { product ->
                ProductCardV1(
                    product = product, 
                    isFavorite = favoriteIds.contains(product.id),
                    onFavoriteClick = { onFavoriteClick(product.id) },
                    onAddToCart = { onAddToCart(product) }, 
                    onProductClick = onProductClick,
                    modifier = Modifier.width(125.dp)
                )
            }
        }
    }
}

@Composable
fun GridProductSection(
    title: String, 
    products: List<ProductDto>, 
    favoriteIds: Set<Int>,
    onAddToCart: (ProductDto) -> Unit, 
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 0.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black)
        val rows = products.chunked(2)
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                for (product in rowItems) {
                    ProductCardV2(
                        product = product, 
                        isFavorite = favoriteIds.contains(product.id),
                        onFavoriteClick = { onFavoriteClick(product.id) },
                        onAddToCart = { onAddToCart(product) }, 
                        onProductClick = onProductClick, 
                        modifier = Modifier.weight(1f) // height(240.dp) kaldırıldı, V2 kendi boyunu yönetecek
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PromoSection(title: String, promos: List<PromoItem>) {
    Column(modifier = Modifier.padding(top = 0.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(80.dp)) {
            items(promos) { promo ->
                PromoCard(promo = promo)
            }
        }
    }
}

@Composable
fun PromoCard(promo: PromoItem) {
    Card(
        modifier = Modifier.width(80.dp).height(75.dp).clickable { },
        shape = RoundedCornerShape(10.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        AsyncImage(
            model = promo.imageUrl, 
            contentDescription = null, 
            modifier = Modifier.fillMaxSize(), 
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    APIcallingTheme {
        HomeScreen(
            productState = ProductState(), 
            favoriteIds = emptySet(), 
            onAddToCart = {}, 
            onProductClick = {}, 
            onCategoryClick = {}, 
            onFavoriteClick = {},
            onSearch = {},
            onSearchQueryChange = {},
            onSuggestionClick = {}
        )
    }
}
