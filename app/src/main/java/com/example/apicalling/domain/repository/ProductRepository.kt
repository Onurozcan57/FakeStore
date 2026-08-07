package com.example.apicalling.domain.repository

import com.example.apicalling.data.model.ProductDto

interface ProductRepository {
    suspend fun getProducts(): List<ProductDto>
}
