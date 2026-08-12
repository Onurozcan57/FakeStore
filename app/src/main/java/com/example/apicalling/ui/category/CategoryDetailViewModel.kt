package com.example.apicalling.ui.category

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption(val title: String) {
    RECOMMENDED("Önerilen sıralama"),
    PRICE_LOW_TO_HIGH("En düşük fiyat"),
    PRICE_HIGH_TO_LOW("En yüksek fiyat"),
    BEST_RATING("En yüksek puanlılar")
}

data class CategoryDetailState(
    val isLoading: Boolean = false,
    val products: List<ProductDto> = emptyList(),
    val filteredProducts: List<ProductDto> = emptyList(), // Filtrelenmiş liste
    val categoryName: String = "",
    val error: String? = null,
    val selectedSortOption: SortOption = SortOption.RECOMMENDED,
    // Filtreleme State'leri
    val minPrice: String = "",
    val maxPrice: String = "",
    val selectedBrands: Set<String> = emptySet(),
    val availableBrands: List<String> = emptyList()
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(CategoryDetailState())
    val state: State<CategoryDetailState> = _state

    private var originalProducts: List<ProductDto> = emptyList()

    init {
        savedStateHandle.get<String>("categorySlug")?.let { slug ->
            getProductsByCategory(slug)
        }
    }

    private fun getProductsByCategory(slug: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, categoryName = slug)
            try {
                val products = productRepository.getProductsByCategory(slug)
                originalProducts = products
                val brands = products.mapNotNull { it.brand }.distinct().sorted()
                _state.value = _state.value.copy(
                    isLoading = false,
                    products = products,
                    filteredProducts = products,
                    availableBrands = brands,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ürünler yüklenirken bir hata oluştu."
                )
            }
        }
    }

    fun onSortOptionSelected(option: SortOption) {
        _state.value = _state.value.copy(selectedSortOption = option)
        applyFiltersAndSort()
    }

    fun updatePriceRange(min: String, max: String) {
        _state.value = _state.value.copy(minPrice = min, maxPrice = max)
    }

    fun toggleBrandSelection(brand: String) {
        val current = _state.value.selectedBrands.toMutableSet()
        if (current.contains(brand)) current.remove(brand) else current.add(brand)
        _state.value = _state.value.copy(selectedBrands = current)
    }

    fun applyFiltersAndSort() {
        val currentState = _state.value
        var list = originalProducts

        // 1. Marka Filtresi
        if (currentState.selectedBrands.isNotEmpty()) {
            list = list.filter { currentState.selectedBrands.contains(it.brand) }
        }

        // 2. Fiyat Filtresi
        val min = currentState.minPrice.toDoubleOrNull() ?: 0.0
        val max = currentState.maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
        list = list.filter { it.price in min..max }

        // 3. Sıralama
        val sortedList = when (currentState.selectedSortOption) {
            SortOption.RECOMMENDED -> list
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            SortOption.BEST_RATING -> list.sortedByDescending { it.rating }
        }

        _state.value = currentState.copy(filteredProducts = sortedList)
    }

    fun resetFilters() {
        _state.value = _state.value.copy(
            minPrice = "",
            maxPrice = "",
            selectedBrands = emptySet(),
            selectedSortOption = SortOption.RECOMMENDED,
            filteredProducts = originalProducts
        )
    }
}
