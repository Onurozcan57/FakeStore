package com.example.apicalling.ui.cart

import com.example.apicalling.data.model.ProductDto

/**
 * Terminoloji: UI Model / Wrapper
 * Sepetteki bir ürünü ve miktarını temsil eden veri sınıfı.
 */
data class CartItem(
    val product: ProductDto,
    val quantity: Int = 1,
    val isSelected: Boolean = true
)
