package com.example.apicalling.ui.product.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("productId")?.let { id ->
            getProduct(id.toInt())
        }
    }

    private fun getProduct(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val product = productRepository.getProduct(id)
                _state.update { it.copy(isLoading = false, product = product, error = null) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Ürün detayları yüklenemedi.") }
            }
        }
    }
}
