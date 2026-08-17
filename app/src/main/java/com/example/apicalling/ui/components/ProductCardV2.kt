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

/**
 * Yenilenmiş 2. Nesil Ürün Kartı (V1 ile aynı modern tasarım dili)
 */
@Composable
fun ProductCardV2(
    product: ProductDto,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onAddToCart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ProductCardV1(
        product = product,
        isFavorite = isFavorite,
        onFavoriteClick = onFavoriteClick,
        onProductClick = onProductClick,
        onAddToCart = onAddToCart,
        modifier = modifier
    )
}
