package com.example.apicalling.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.category.CategoryProductGrid
import com.example.apicalling.ui.category.SortOption
import com.example.apicalling.ui.category.SortSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel,
    onProductClick: (Int) -> Unit,
    onAddToCart: (ProductDto) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val sortSheetState = rememberModalBottomSheetState()
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Favorilerim",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        SearchBox(
                            query = state.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) }
                        )
                    }
                    item {
                        FilterButton(
                            text = "Sırala",
                            icon = Icons.Default.Sort,
                            onClick = { showSortSheet = true }
                        )
                    }
                    item {
                        FilterButton(
                            text = "Fiyatı Düşenler",
                            icon = Icons.Default.TrendingDown,
                            onClick = { }
                        )
                    }
                    item {
                        FilterButton(
                            text = "Stoktakiler",
                            icon = Icons.Default.Inventory,
                            onClick = { }
                        )
                    }
                    item {
                        FilterButton(
                            text = "Kategoriler",
                            icon = Icons.Default.Category,
                            onClick = { }
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.displayedProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Henüz favori ürününüz yok.", color = Color.Gray)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    CategoryProductGrid(
                        products = state.displayedProducts,
                        favoriteIds = state.favoriteIds,
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart,
                        onFavoriteClick = { viewModel.toggleFavorite(it) }
                    )
                    Spacer(modifier = Modifier.height(110.dp))
                }
            }
        }

        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                sheetState = sortSheetState,
                containerColor = Color.White,
                dragHandle = null
            ) {
                SortSheetContent(
                    selectedOption = state.sortOption,
                    onOptionSelected = { option ->
                        viewModel.onSortOptionChange(option)
                        scope.launch { sortSheetState.hide() }.invokeOnCompletion {
                            if (!sortSheetState.isVisible) showSortSheet = false
                        }
                    },
                    onClose = {
                        scope.launch { sortSheetState.hide() }.invokeOnCompletion {
                            if (!sortSheetState.isVisible) showSortSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBox(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .width(150.dp)
            .height(40.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
        color = Color(0xFFF9F9F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Ara...", color = Color.LightGray, fontSize = 12.sp)
                    inner()
                }
            )
        }
    }
}

@Composable
fun FilterButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable { onClick() }
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
