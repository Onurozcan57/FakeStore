package com.example.apicalling.ui.product.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.data.model.ProductDto
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    cartItemCount: Int, // Terminoloji: State-driven Counter
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCart: (ProductDto) -> Unit
) {
    val state = viewModel.state.value
    var isFavorite by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Terminoloji: Detail App Bar
            TopAppBar(
                title = {
                    // Arama Çubuğu (Terminoloji: Embedded Search Bar)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ara...", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        // Terminoloji: Badged Action Icon
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge {
                                        Text(text = cartItemCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Sepet")
                        }
                    }
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Terminoloji: Floating Detail Action Bar
            state.product?.let { product ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp), // Havada durma etkisi (Floating)
                    shape = RoundedCornerShape(28.dp), // Yuvarlak hatlar
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp), // Daha kompakt/küçük boyut
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fiyat (Terminoloji: Price Label)
                        Text(
                            text = "${product.price} $",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Sepete Ekle Butonu
                        Button(
                            onClick = { onAddToCart(product) },
                            modifier = Modifier
                                .height(44.dp) // Buton yüksekliği küçültüldü
                                .width(180.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Sepete Ekle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.error?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }

            state.product?.let { product ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Resim Kısmı (Terminoloji: Product Carousel)
                    // Sayfanın %40'ını kaplayacak şekilde
                    ProductImageCarousel(
                        images = product.images,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.4f)
                            .background(MaterialTheme.colorScheme.surface) // Temadaki yüzey rengi
                    )

                    // 2. Ürün Bilgileri (Terminoloji: Product Info Section)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))

                            // Puan Yıldızları Kutusu (Terminoloji: Compact Rating Box)
                            RatingBox(rating = product.rating)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        // Fiyat ve Ekle butonu ileride eklenebilir
                    }
                }
            }
        }
    }
}

@Composable
fun ProductImageCarousel(images: List<String>, modifier: Modifier = Modifier) {
    // Toplam 5 resim olacak şekilde ayarla (Eksikse tekrarla)
    val displayImages = remember(images) {
        if (images.isEmpty()) emptyList()
        else List(5) { images[it % images.size] }
    }
    
    val pagerState = rememberPagerState(pageCount = { displayImages.size })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = displayImages[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        // Sayfa Göstergesi (Opsiyonel: Dots)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(displayImages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}

@Composable
fun RatingBox(rating: Double) {
    // Rastgele yorum sayısı
    val reviewCount = remember { Random.nextInt(10, 1000) }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.width(70.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Üst Kısım: Yıldız ve Puan
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            
            // Alt Kısım: Yorum Sayısı (Gri Arka Font)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$reviewCount yorum",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
