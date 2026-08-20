package com.example.apicalling.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.util.PriceUtils
import java.util.*

/**
 * Hepsiburada Tasarımı (Terminoloji: Vertical Squeezed Card)
 * Genişlik azaltıldı, boy uzatıldı ve tüm iç boşluklar görselle kapatıldı.
 */
@Composable
fun ProductCardV1(
    product: ProductDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onAddToCart: () -> Unit = {},
) {
    val formattedPrice = remember(product.price) { PriceUtils.formatUsdAsTry(product.price) }
    val isDiscounted = product.discountPercentage > 13.0
    val originalPriceFormatted = remember(product.price, product.discountPercentage) {
        if (isDiscounted) {
            val originalPrice = product.price / (1 - product.discountPercentage / 100.0)
            PriceUtils.formatUsdAsTry(originalPrice)
        } else ""
    }
    val discountPercent = remember(product.discountPercentage) { product.discountPercentage.toInt() }

    Card(
        modifier = modifier
            .height(205.dp)
            .padding(2.dp)
            .clickable { onProductClick(product.id) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, Color(0xFFEFEFEF)), 
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
                    .weight(1f) 
                    .clip(RoundedCornerShape(6.dp)) 
                    .background(Color(0xFFF9F9F9)),
                contentAlignment = Alignment.Center
            ) {
                OptimizedProductImage(
                    imageModel = product.thumbnail,
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize() 
                )

                // 🌟 Kampanya Rozeti (Sol Üst - Dinamik)
                if (product.discountPercentage >= 13.0) {
                    val badgeUrl = remember(product.discountPercentage) {
                        if (product.discountPercentage >= 15.0) "https://images.hepsiburada.net/banners/s/1/104-105/app134056176801991801.png"
                        else "https://images.hepsiburada.net/banners/s/1/104-105/app3134056183170135731.png"
                    }

                    OptimizedProductImage(
                        imageModel = badgeUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                }

                // Favori Butonu
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp),
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

            // 2. Alt Bilgi Grubu (Resimle fiyat arası minimal)
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White)
            ) {
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp).padding(top = 2.dp, bottom = 6.dp)
                )

                // 3. Fiyat Alanı (Dinamik İndirimli Yapı)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp) 
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF3F3F3))
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isDiscounted) {
                        Column(verticalArrangement = Arrangement.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Eski Fiyat
                                Text(
                                    text = originalPriceFormatted,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                // İndirim Oranı
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF228912),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .padding(horizontal = 3.dp)
                                ) {
                                    Text(
                                        text = "%$discountPercent",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                            // İndirimli Yeni Fiyat
                            Text(
                                text = formattedPrice,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF228912),
                                modifier = Modifier.offset(y = (-8).dp ,x= (-2).dp),
                                maxLines = 1
                            )
                        }
                    } else {
                        // İndirim Yoksa Normal Görünüm
                        Text(
                            text = formattedPrice,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

// Artık PriceUtils kullanılıyor.
