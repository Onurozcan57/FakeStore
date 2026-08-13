package com.example.apicalling.ui.search

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

data class SearchState(
    val isLoading: Boolean = false,
    val products: List<ProductDto> = emptyList(),
    val query: String = "",
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("query")?.let { query ->
            searchProducts(query)
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, query = query) }
            try {
                val results = productRepository.searchProducts(query)
                _state.update { it.copy(isLoading = false, products = results, error = null) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Arama sonuçları yüklenirken bir hata oluştu.") }
            }
        }
    }
}
