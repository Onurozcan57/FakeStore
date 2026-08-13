package com.example.apicalling.domain.usecase

import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.repository.ProductRepository
import com.example.apicalling.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<Resource<List<ProductDto>>> = flow {
        emit(Resource.Loading())
        try {
            val products = repository.getProducts()
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Beklenmedik bir hata oluştu."))
        }
    }
}
