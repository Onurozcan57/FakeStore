package com.example.apicalling.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
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
import kotlin.random.Random

/**
 * 2. Nesil Ürün Kartı Tasarımı (Terminoloji: Premium Product Card)
 */
@Composable
fun ProductCardV2(
    product: ProductDto,
    onAddToCart: () -> Unit,
    isFavorite: Boolean, // Terminoloji: Hoisted State
    onFavoriteClick: () -> Unit, // Terminoloji: State Event
    onProductClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val reviewCount = product.reviews?.size ?: 0

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onProductClick(product.id) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.thumbnail,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .clickable { onFavoriteClick() },
                    shape = CircleShape,
                    color = Color.White, // Tam beyaz yapıldı
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2, // 2 satırda tutuyoruz
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.rating.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($reviewCount yorum)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${product.price} $",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ekle", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
