package com.example.apicalling.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.domain.repository.ProductRepository
import com.example.apicalling.domain.usecase.GetProductsUseCase
import com.example.apicalling.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.apicalling.ui.home.Category
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        getProducts()
    }

    /**
     * Terminoloji: UseCase Integration
     * Veri çekme işlemini UseCase üzerinden yönetiyoruz.
     */
    fun getProducts() {
        getProductsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    val products = result.data ?: emptyList()
                    val categories = products
                        .groupBy { it.category }
                        .map { (name, list) ->
                            Category(
                                title = name.replaceFirstChar { it.uppercase() },
                                imageUrl = list.firstOrNull()?.thumbnail ?: "",
                                slug = name
                            )
                        }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            products = products, 
                            randomProducts = products.shuffled().take(26),
                            categories = categories,
                            error = null
                        ) 
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Terminoloji: Debounced Search Suggestions
     * Kullanıcı yazarken API'yi yormadan önerileri getirir.
     */
    fun onSearchQueryChanged(query: String) {
        if (query.length < 2) {
            _state.update { it.copy(searchSuggestions = emptyList(), isSearching = false) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            delay(400)
            
            try {
                val suggestions = productRepository.searchProducts(query)
                _state.update { it.copy(searchSuggestions = suggestions, isSearching = true) }
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }

    fun clearSuggestions() {
        _state.update { it.copy(searchSuggestions = emptyList(), isSearching = false) }
    }
}
