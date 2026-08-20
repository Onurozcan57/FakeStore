package com.example.apicalling.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.repository.FavoriteRepository
import com.example.apicalling.domain.repository.ProductRepository
import com.example.apicalling.ui.category.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteState(
    val isLoading: Boolean = false,
    val allFavoriteProducts: List<ProductDto> = emptyList(),
    val displayedProducts: List<ProductDto> = emptyList(),
    val favoriteIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.RECOMMENDED,
    val isPriceDroppedFilterActive: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoriteState())
    val state: StateFlow<FavoriteState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRepository.favoriteIds.collectLatest { ids ->
                _state.update { it.copy(favoriteIds = ids) }
                loadFavoriteProducts(ids)
            }
        }
    }

    private suspend fun loadFavoriteProducts(ids: Set<Int>) {
        if (ids.isEmpty()) {
            _state.update { it.copy(allFavoriteProducts = emptyList(), displayedProducts = emptyList()) }
            return
        }

        _state.update { it.copy(isLoading = true) }
        try {
            val allProducts = productRepository.getProducts()
            val favorites = allProducts.filter { ids.contains(it.id) }
            
            _state.update { it.copy(isLoading = false, allFavoriteProducts = favorites) }
            applyFiltersAndSort()
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = "Favoriler yüklenemedi.") }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun onSortOptionChange(option: SortOption) {
        _state.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    fun togglePriceDroppedFilter() {
        _state.update { it.copy(isPriceDroppedFilterActive = !it.isPriceDroppedFilterActive) }
        applyFiltersAndSort()
    }

    fun toggleFavorite(productId: Int) {
        favoriteRepository.toggleFavorite(productId)
    }

    private fun applyFiltersAndSort() {
        val current = _state.value
        var list = current.allFavoriteProducts

        if (current.searchQuery.isNotBlank()) {
            list = list.filter { it.title.contains(current.searchQuery, ignoreCase = true) }
        }

        if (current.isPriceDroppedFilterActive) {
            list = list.filter { it.discountPercentage > 13.0 }
        }

        val sortedList = when (current.sortOption) {
            SortOption.RECOMMENDED -> list
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            SortOption.BEST_RATING -> list.sortedByDescending { it.rating }
        }

        _state.update { it.copy(displayedProducts = sortedList) }
    }
}
