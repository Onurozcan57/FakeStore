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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    onAddToCart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                AsyncImage(
                    model = product.thumbnail,
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize() 
                )

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

                // 3. Fiyat Alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(6.dp)) 
                        .background(Color(0xFFF3F3F3))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = PriceUtils.formatUsdAsTry(product.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

// Artık PriceUtils kullanılıyor.
