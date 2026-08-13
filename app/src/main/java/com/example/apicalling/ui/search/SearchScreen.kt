package com.example.apicalling.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.category.CategoryProductGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    favoriteIds: Set<Int>,
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onAddToCart: (ProductDto) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onSearch: (String) -> Unit // Yeni: Arama aksiyonu
) {
    val state by viewModel.state.collectAsState()
    var currentQuery by remember { mutableStateOf(state.query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .height(40.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = currentQuery,
                            onValueChange = { currentQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (currentQuery.isNotBlank()) {
                                        onSearch(currentQuery)
                                    }
                                }
                            ),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (currentQuery.isEmpty()) Text("Ara...", color = Color.Gray, fontSize = 14.sp)
                                    inner()
                                }
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Sonuç Özeti
            Text(
                text = buildAnnotatedString {
                    append("\"$currentQuery\" için ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("${state.products.size} sonuç")
                    }
                    append(" bulundu")
                },
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.products.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Üzgünüz, aradığınız kriterlere uygun ürün bulunamadı.")
                }
            } else {
                CategoryProductGrid(
                    products = state.products,
                    favoriteIds = favoriteIds,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onFavoriteClick = onFavoriteClick
                )
            }
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
