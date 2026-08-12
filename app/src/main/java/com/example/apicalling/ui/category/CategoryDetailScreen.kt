package com.example.apicalling.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.ui.components.ProductCardV2
import com.example.apicalling.ui.home.Category
import com.example.apicalling.ui.home.CategoryCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    favoriteIds: Set<Int>, // Yeni: Favori durumları
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onAddToCart: (ProductDto) -> Unit,
    onFavoriteClick: (Int) -> Unit // Yeni: Favori tıklama
) {
    val state = viewModel.state.value
    val categoryName = state.categoryName.replaceFirstChar { it.uppercase() }
    val scope = rememberCoroutineScope()
    
    val sortSheetState = rememberModalBottomSheetState()
    var showSortSheet by remember { mutableStateOf(false) }
    
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .height(48.dp)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("$categoryName ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                                    append("(${state.filteredProducts.size})")
                                }
                            },
                            fontSize = 14.sp,
                            color = Color.DarkGray
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                FilterSortRow(
                    onSortClick = { showSortSheet = true },
                    onFilterClick = { showFilterSheet = true }
                )

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    CategoryProductGrid(
                        products = state.filteredProducts,
                        favoriteIds = favoriteIds,
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart,
                        onFavoriteClick = onFavoriteClick
                    )
                }
                Spacer(modifier = Modifier.height(110.dp))
            }

            if (showSortSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSortSheet = false },
                    sheetState = sortSheetState,
                    containerColor = Color.White,
                    dragHandle = null
                ) {
                    SortSheetContent(
                        selectedOption = state.selectedSortOption,
                        onOptionSelected = { option ->
                            viewModel.onSortOptionSelected(option)
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

            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = filterSheetState,
                    containerColor = Color.White,
                    dragHandle = null
                ) {
                    FilterSheetContent(
                        state = state,
                        onPriceChange = { min, max -> viewModel.updatePriceRange(min, max) },
                        onBrandToggle = { viewModel.toggleBrandSelection(it) },
                        onApply = {
                            viewModel.applyFiltersAndSort()
                            scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                                if (!filterSheetState.isVisible) showFilterSheet = false
                            }
                        },
                        onReset = { viewModel.resetFilters() },
                        onClose = {
                            scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                                if (!filterSheetState.isVisible) showFilterSheet = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterSheetContent(
    state: CategoryDetailState,
    onPriceChange: (String, String) -> Unit,
    onBrandToggle: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(bottom = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Filtrele", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Black)
            }
        }
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Fiyat Aralığı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.minPrice,
                    onValueChange = { onPriceChange(it, state.maxPrice) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Min $") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state.maxPrice,
                    onValueChange = { onPriceChange(state.minPrice, it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Max $") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (state.availableBrands.isNotEmpty()) {
                Text("Markalar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.availableBrands.forEach { brand ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onBrandToggle(brand) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.selectedBrands.contains(brand),
                            onCheckedChange = { onBrandToggle(brand) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(brand, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Sıfırla")
            }
            Button(onClick = onApply, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Uygula")
            }
        }
    }
}

@Composable
fun SortSheetContent(selectedOption: SortOption, onOptionSelected: (SortOption) -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Sırala", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.align(Alignment.Center))
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Black)
            }
        }
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        SortOption.entries.forEach { option ->
            SortOptionItem(title = option.title, isSelected = selectedOption == option, onClick = { onOptionSelected(option) })
        }
    }
}

@Composable
fun SortOptionItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(20.dp).border(width = 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, shape = CircleShape).background(color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, shape = CircleShape), contentAlignment = Alignment.Center) {
            if (isSelected) { Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape)) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = if (isSelected) Color.Black else Color.DarkGray, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
fun FilterSortRow(onSortClick: () -> Unit, onFilterClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(modifier = Modifier.weight(1f).height(40.dp).clickable { onSortClick() }, shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sırala", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        Surface(modifier = Modifier.weight(1f).height(40.dp).clickable { onFilterClick() }, shape = RoundedCornerShape(8.dp), color = Color(0xFFF5F5F5)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filtrele", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CategoryProductGrid(
    products: List<ProductDto>, 
    favoriteIds: Set<Int>,
    onProductClick: (Int) -> Unit, 
    onAddToCart: (ProductDto) -> Unit,
    onFavoriteClick: (Int) -> Unit
) {
    val rows = products.chunked(2)
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        rows.forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                for (product in rowItems) {
                    ProductCardV2(
                        product = product, 
                        isFavorite = favoriteIds.contains(product.id),
                        onFavoriteClick = { onFavoriteClick(product.id) },
                        onAddToCart = { onAddToCart(product) }, 
                        onProductClick = onProductClick, 
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
