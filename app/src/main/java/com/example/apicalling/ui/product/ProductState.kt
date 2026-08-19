package com.example.apicalling.ui.product

import com.example.apicalling.data.model.ProductDto

import com.example.apicalling.ui.home.Category

/**
 * Ürün listesi ekranının o anki durumunu temsil eder.
 */
data class ProductState(
    val isLoading: Boolean = false,
    val products: List<ProductDto> = emptyList(),
    val randomProducts: List<ProductDto> = emptyList(),
    val fragrancesProducts: List<ProductDto> = emptyList(),
    val chunkedRandomProducts: List<List<ProductDto>> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchSuggestions: List<ProductDto> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)
