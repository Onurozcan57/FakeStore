package com.example.apicalling.domain.repository

import com.example.apicalling.data.model.ProductDto

interface ProductRepository {
    suspend fun getProducts(): List<ProductDto>
    suspend fun getProduct(id: Int): ProductDto
    suspend fun getProductsByCategory(category: String): List<ProductDto>
    suspend fun searchProducts(query: String): List<ProductDto>
}
