package com.example.apicalling.ui.product.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(ProductDetailState())
    val state: State<ProductDetailState> = _state

    init {
        // Navigasyondan gelen productId'yi alıyoruz
        savedStateHandle.get<String>("productId")?.let { id ->
            getProduct(id.toInt())
        }
    }

    private fun getProduct(id: Int) {
        viewModelScope.launch {
            _state.value = ProductDetailState(isLoading = true)
            try {
                val product = productRepository.getProduct(id)
                _state.value = ProductDetailState(product = product)
            } catch (e: Exception) {
                _state.value = ProductDetailState(error = "Ürün detayları yüklenemedi: ${e.localizedMessage}")
            }
        }
    }
}
