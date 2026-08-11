package com.example.apicalling.ui.product.detail

import com.example.apicalling.data.model.ProductDto

/**
 * Ürün detay ekranının durumunu temsil eder.
 */
data class ProductDetailState(
    val isLoading: Boolean = false,
    val product: ProductDto? = null,
    val error: String? = null
)
