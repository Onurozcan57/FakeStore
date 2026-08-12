package com.example.apicalling.ui.favorites

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.data.session.FavoriteManager
import com.example.apicalling.domain.repository.ProductRepository
import com.example.apicalling.ui.category.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteState(
    val isLoading: Boolean = false,
    val allFavoriteProducts: List<ProductDto> = emptyList(), // Tüm favoriler
    val displayedProducts: List<ProductDto> = emptyList(), // Filtrelenmiş/Sıralanmış liste
    val favoriteIds: Set<Int> = emptySet(), // Yeni: ID takibi için
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.RECOMMENDED,
    val error: String? = null
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoriteManager: FavoriteManager
) : ViewModel() {

    private val _state = mutableStateOf(FavoriteState())
    val state: State<FavoriteState> = _state

    init {
        // Favori ID'leri değiştikçe listeyi güncelle (Terminoloji: Reactive Data Sync)
        viewModelScope.launch {
            favoriteManager.favoriteIds.collectLatest { ids ->
                _state.value = _state.value.copy(favoriteIds = ids)
                loadFavoriteProducts(ids)
            }
        }
    }

    private suspend fun loadFavoriteProducts(ids: Set<Int>) {
        if (ids.isEmpty()) {
            _state.value = _state.value.copy(allFavoriteProducts = emptyList(), displayedProducts = emptyList())
            return
        }

        _state.value = _state.value.copy(isLoading = true)
        try {
            // API'den tüm ürünleri alıp sadece favori olanları süzüyoruz
            // Not: Normalde API'den sadece seçili ID'leri getiren bir endpoint istenir.
            val allProducts = productRepository.getProducts()
            val favorites = allProducts.filter { ids.contains(it.id) }
            
            _state.value = _state.value.copy(
                isLoading = false,
                allFavoriteProducts = favorites
            )
            applyFiltersAndSort()
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = "Favoriler yüklenemedi.")
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }

    fun onSortOptionChange(option: SortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFiltersAndSort()
    }

    fun toggleFavorite(productId: Int) {
        favoriteManager.toggleFavorite(productId)
    }

    private fun applyFiltersAndSort() {
        val current = _state.value
        var list = current.allFavoriteProducts

        // 1. Arama Filtresi
        if (current.searchQuery.isNotBlank()) {
            list = list.filter { it.title.contains(current.searchQuery, ignoreCase = true) }
        }

        // 2. Sıralama
        val sortedList = when (current.sortOption) {
            SortOption.RECOMMENDED -> list
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            SortOption.BEST_RATING -> list.sortedByDescending { it.rating }
        }

        _state.value = _state.value.copy(displayedProducts = sortedList)
    }
}
