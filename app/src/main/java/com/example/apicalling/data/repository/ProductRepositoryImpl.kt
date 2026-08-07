package com.example.apicalling.data.repository

import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.data.remote.ApiService
import com.example.apicalling.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {
    override suspend fun getProducts(): List<ProductDto> {
        return apiService.getProducts().products
    }
}
