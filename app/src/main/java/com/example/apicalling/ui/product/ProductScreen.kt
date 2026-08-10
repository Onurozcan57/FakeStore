package com.example.apicalling.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.components.ProductCard

/**
 * Ürün listesi ekranı.
 * Artık bağımsız ProductCard bileşenini kullanıyor.
 */
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onAddToCart: (ProductDto) -> Unit
) {
    val state = viewModel.state.value

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                bottom = 100.dp // Floating bar için güvenli boşluk
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.products) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}
