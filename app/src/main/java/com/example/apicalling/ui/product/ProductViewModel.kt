package com.example.apicalling.ui.product

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProductState())
    val state: State<ProductState> = _state

    init {
        getProducts()
    }

    /**
     * API'den ürün listesini çeker ve state'i günceller.
     */
    fun getProducts() {
        viewModelScope.launch {
            _state.value = ProductState(isLoading = true)
            try {
                val products = productRepository.getProducts()
                _state.value = ProductState(products = products)
            } catch (e: Exception) {
                _state.value = ProductState(error = "Ürünler yüklenirken hata oluştu: ${e.localizedMessage}")
            }
        }
    }
}
