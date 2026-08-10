package com.example.apicalling.ui.product

import com.example.apicalling.data.model.ProductDto

/**
 * Ürün listesi ekranının o anki durumunu temsil eder.
 */
data class ProductState(
    val isLoading: Boolean = false,
    val products: List<ProductDto> = emptyList(),
    val error: String? = null
)
