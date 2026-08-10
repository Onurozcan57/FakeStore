package com.example.apicalling.ui.cart

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.apicalling.data.model.ProductDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _cartItems = mutableStateListOf<ProductDto>()
    val cartItems: List<ProductDto> get() = _cartItems

    fun addToCart(product: ProductDto) {
        _cartItems.add(product)
    }

    fun removeFromCart(product: ProductDto) {
        _cartItems.remove(product)
    }

    fun getTotalPrice(): Double {
        return _cartItems.sumOf { it.price }
    }

    fun clearCart() {
        _cartItems.clear()
    }
}
