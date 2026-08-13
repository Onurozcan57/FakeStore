package com.example.apicalling.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val filteredProducts: List<ProductDto> = emptyList(),
    val categoryName: String = "",
    val error: String? = null,
    val selectedSortOption: SortOption = SortOption.RECOMMENDED,
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

    private val _state = MutableStateFlow(CategoryDetailState())
    val state: StateFlow<CategoryDetailState> = _state.asStateFlow()

    private var originalProducts: List<ProductDto> = emptyList()

    init {
        savedStateHandle.get<String>("categorySlug")?.let { slug ->
            getProductsByCategory(slug)
        }
    }

    private fun getProductsByCategory(slug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, categoryName = slug) }
            try {
                val products = productRepository.getProductsByCategory(slug)
                originalProducts = products
                val brands = products.mapNotNull { it.brand }.distinct().sorted()
                _state.update { 
                    it.copy(
                        isLoading = false,
                        products = products,
                        filteredProducts = products,
                        availableBrands = brands,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Ürünler yüklenirken bir hata oluştu.") }
            }
        }
    }

    fun onSortOptionSelected(option: SortOption) {
        _state.update { it.copy(selectedSortOption = option) }
        applyFiltersAndSort()
    }

    fun updatePriceRange(min: String, max: String) {
        _state.update { it.copy(minPrice = min, maxPrice = max) }
    }

    fun toggleBrandSelection(brand: String) {
        _state.update { currentState ->
            val current = currentState.selectedBrands.toMutableSet()
            if (current.contains(brand)) current.remove(brand) else current.add(brand)
            currentState.copy(selectedBrands = current)
        }
    }

    fun applyFiltersAndSort() {
        val currentState = _state.value
        var list = originalProducts

        if (currentState.selectedBrands.isNotEmpty()) {
            list = list.filter { currentState.selectedBrands.contains(it.brand) }
        }

        val min = currentState.minPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
        val max = currentState.maxPrice.replace(",", ".").toDoubleOrNull() ?: Double.MAX_VALUE
        list = list.filter { it.price in min..max }

        val sortedList = when (currentState.selectedSortOption) {
            SortOption.RECOMMENDED -> list
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            SortOption.BEST_RATING -> list.sortedByDescending { it.rating }
        }

        _state.update { it.copy(filteredProducts = sortedList) }
    }

    fun resetFilters() {
        _state.update { 
            it.copy(
                minPrice = "",
                maxPrice = "",
                selectedBrands = emptySet(),
                selectedSortOption = SortOption.RECOMMENDED,
                filteredProducts = originalProducts
            )
        }
    }
}
