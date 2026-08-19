package com.example.apicalling.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.util.PriceUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 2. Nesil Ürün Kartı Tasarımı (Terminoloji: Interactive Action Card)
 * Özellikler: Dar-uzun form, interaktif sepet animasyonu ve "Sepete Özel" indirim dili.
 */
@Composable
fun ProductCardV2(
    product: ProductDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdded by remember { mutableStateOf(false) } // ID bağlantısı kaldırıldı
    var isAnimating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🕒 6 Saniye Sonra Fiyata Geri Dönme Mantığı (Terminoloji: Transient State Reset)
    LaunchedEffect(isAdded) {
        if (isAdded) {
            delay(6000)
            isAdded = false
        }
    }

    // Dönme Animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .width(90.dp) // V1'den daha dar
            .height(350.dp) // V1'den daha uzun
            .padding(2.dp)
            .clickable { onProductClick(product.id) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFEFEFEF)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // 1. Ürün Görseli Alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF9F9F9)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.thumbnail,
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )

                // 🌟 Kampanya Rozeti (Sol Üst - Dinamik)
                if (product.discountPercentage >= 13.0) {
                    val badgeUrl = if (product.discountPercentage >= 15.0) "https://images.hepsiburada.net/banners/s/1/104-105/app134056176801991801.png" // 3 Yıldız
                    else
                    "https://images.hepsiburada.net/banners/s/1/104-105/app3134056183170135731.png"  // Tek Yıldız

                    AsyncImage(
                        model = badgeUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }

                // Favori Butonu
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(26.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 2. Bilgi Alanı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                val titleWords = product.title.split(" ")
                val annotatedTitle = buildAnnotatedString {
                    if (titleWords.isNotEmpty()) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append(titleWords[0])
                        }
                        if (titleWords.size > 1) {
                            append(" ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                append(titleWords.drop(1).joinToString(" "))
                            }
                        }
                    }
                }
                
                Text(
                    text = annotatedTitle,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )

                // ⭐ Yıldızlı Değerlendirme (Açıklamanın sol altında)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.rating.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF444444)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviews?.size ?: 0})",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // 3. İnteraktif Fiyat ve Sepet Alanı
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Sepete Özel" Rozeti (Şartlı Görünüm: Sadece indirim > 13.0 ise)
                if (product.discountPercentage > 13.0) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(44.dp) // Fiyat kutusuyla aynı yükseklik
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                            Text(
                                text = "Sepete\nÖzel",
                                color = Color(0xFF228912),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(1.dp)) // İstediğin 1dp boşluk
                }

                // Fiyat Kutusu
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAdded) Color(0xFFE8F5E9) else Color(0xFFF3F3F3))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = isAdded,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "cart_state"

                    ) { added ->
                        if (added) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF228912), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eklendi", color = Color(0xFF228912), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // V1'deki İndirim Yapısının Aynısı
                                if (product.discountPercentage > 13.0) {
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val originalPrice = product.price / (1 - product.discountPercentage / 100.0)
                                            Text(
                                                text = PriceUtils.formatUsdAsTry(originalPrice),
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                textDecoration = TextDecoration.LineThrough,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = Color(0xFF228912),
                                                        shape = RoundedCornerShape(2.dp)
                                                    )
                                                    .padding(horizontal = 2.dp, vertical = 0.dp)
                                            ) {
                                                Text(
                                                    text = "%${product.discountPercentage.toInt()}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    lineHeight = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = PriceUtils.formatUsdAsTry(product.price),
                                            fontSize = 13.sp, 
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF228912),
                                            modifier = Modifier.offset(y = (-8).dp ,x= (-2).dp),
                                            maxLines = 1
                                        )
                                    }
                                } else {
                                    Text(
                                        text = PriceUtils.formatUsdAsTry(product.price),
                                        fontSize = 13.sp, 
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }

                                // Sepet Butonu (Arka plansız, siyah ikon)
                                if (isAnimating) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF228912), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.AddShoppingCart,
                                        contentDescription = null,
                                        tint = Color.Black, // Siyah ikon
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                if (!isAnimating) {
                                                    isAnimating = true
                                                    scope.launch {
                                                        delay(1000)
                                                        isAnimating = false
                                                        isAdded = true
                                                        onAddToCart()
                                                    }
                                                }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
