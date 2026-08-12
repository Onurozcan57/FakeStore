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

data class PromoItem(val title: String, val imageUrl: String)

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
    onFavoriteClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val randomProducts = remember(productState.products) {
        productState.products.shuffled().take(26)
    }

    val dynamicCategories = remember(productState.products) {
        productState.products
            .groupBy { it.category }
            .map { (categoryName, products) ->
                Category(
                    title = categoryName.replaceFirstChar { it.uppercase() },
                    imageUrl = products.firstOrNull()?.thumbnail ?: "",
                    slug = categoryName
                )
            }
    }

    val discountedProducts = productState.products
        .filter { it.category == "beauty" }
        .take(10)
    
    val campaignImages = listOf(
        R.drawable.kampanya_2,
        R.drawable.kampanya_1,
        R.drawable.kampanya_3,
        R.drawable.kampanya_4
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
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

        CampaignSlider(images = campaignImages)

        if (dynamicCategories.isNotEmpty()) {
            CategorySection(categories = dynamicCategories, onCategoryClick = onCategoryClick)
        }

        if (discountedProducts.isNotEmpty()) {
            HorizontalProductSection(
                title = "Kampanyadaki Ürünler",
                products = discountedProducts,
                favoriteIds = favoriteIds,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick,
                onFavoriteClick = onFavoriteClick
            )
        }

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

        if (randomProducts.isNotEmpty()) {
            GridProductSection(
                title = "Günün Fırsatları",
                products = randomProducts,
                favoriteIds = favoriteIds,
                onAddToCart = onAddToCart,
                onProductClick = onProductClick,
                onFavoriteClick = onFavoriteClick
            )
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun CategorySection(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = "Kategoriler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black)
        LazyRow(modifier = Modifier.height(100.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                CategoryCard(category = category, onClick = { onCategoryClick(category.slug) })
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(modifier = Modifier.width(75.dp).fillMaxHeight().clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.65f).padding(6.dp), contentAlignment = Alignment.Center) {
                AsyncImage(model = category.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
            }
            Box(modifier = Modifier.fillMaxWidth().weight(0.35f).padding(bottom = 4.dp, start = 4.dp, end = 4.dp), contentAlignment = Alignment.Center) {
                Text(text = category.title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(modifier = Modifier.height(360.dp), contentPadding = PaddingValues(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(products) { product ->
                ProductCardV2(
                    product = product, 
                    isFavorite = favoriteIds.contains(product.id),
                    onFavoriteClick = { onFavoriteClick(product.id) },
                    onAddToCart = { onAddToCart(product) }, 
                    onProductClick = onProductClick, 
                    modifier = Modifier.width(220.dp)
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
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black)
        val rows = products.chunked(2)
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (product in rowItems) {
                    ProductCardV1(
                        product = product, 
                        isFavorite = favoriteIds.contains(product.id),
                        onFavoriteClick = { onFavoriteClick(product.id) },
                        onAddToCart = { onAddToCart(product) }, 
                        onProductClick = onProductClick, 
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PromoSection(title: String, promos: List<PromoItem>) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(110.dp)) {
            items(promos) { promo ->
                PromoCard(promo = promo)
            }
        }
    }
}

@Composable
fun PromoCard(promo: PromoItem) {
    Card(modifier = Modifier.width(110.dp).height(95.dp).clickable { }, shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.6f).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                AsyncImage(model = promo.imageUrl, contentDescription = null, modifier = Modifier.padding(6.dp).fillMaxSize(), contentScale = ContentScale.Fit)
            }
            Box(modifier = Modifier.fillMaxWidth().weight(0.4f).background(Color(0xFFEEEEEE)).padding(horizontal = 4.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                Text(text = promo.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, lineHeight = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    APIcallingTheme {
        HomeScreen(productState = ProductState(), favoriteIds = emptySet(), onAddToCart = {}, onProductClick = {}, onCategoryClick = {}, onFavoriteClick = {})
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
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 10.dp), pageSpacing = 10.dp, modifier = Modifier.fillMaxWidth().height(150.dp)) { page ->
            Card(modifier = Modifier.fillMaxSize().clickable { }, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                AsyncImage(model = images[page], contentDescription = "Kampanya ${page + 1}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Row(Modifier.height(20.dp).fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            repeat(images.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(50)).background(color).size(8.dp))
            }
        }
    }
}
