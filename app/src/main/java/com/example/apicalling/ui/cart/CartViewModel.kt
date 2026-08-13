package com.example.apicalling.ui.cart

import androidx.lifecycle.ViewModel
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.usecase.GetSuggestedProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getSuggestedProductsUseCase: GetSuggestedProductsUseCase
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<ProductDto>>(emptyList())
    val cartItems: StateFlow<List<ProductDto>> = _cartItems.asStateFlow()

    private val _suggestedProducts = MutableStateFlow<List<ProductDto>>(emptyList())
    val suggestedProducts: StateFlow<List<ProductDto>> = _suggestedProducts.asStateFlow()

    fun updateSuggestedProducts(allProducts: List<ProductDto>) {
        _suggestedProducts.value = getSuggestedProductsUseCase(_cartItems.value, allProducts)
    }

    fun addToCart(product: ProductDto) {
        _cartItems.update { current ->
            if (current.any { it.id == product.id }) current else current + product
        }
    }

    fun removeFromCart(product: ProductDto) {
        _cartItems.update { current -> 
            current.filter { item -> item.id != product.id } 
        }
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { it.price }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}
