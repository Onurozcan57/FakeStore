package com.example.apicalling.ui.product

import com.example.apicalling.data.model.ProductDto

import com.example.apicalling.ui.home.Category

/**
 * Ürün listesi ekranının o anki durumunu temsil eder.
 */
data class ProductState(
    val isLoading: Boolean = false,
    val products: List<ProductDto> = emptyList(),
    val randomProducts: List<ProductDto> = emptyList(), // Yeni
    val categories: List<Category> = emptyList(), // Yeni
    val searchSuggestions: List<ProductDto> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)
