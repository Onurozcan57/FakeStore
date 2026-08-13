package com.example.apicalling.domain.usecase

import com.example.apicalling.data.model.ProductDto
import javax.inject.Inject

class GetSuggestedProductsUseCase @Inject constructor() {
    operator fun invoke(
        cartItems: List<ProductDto>,
        allProducts: List<ProductDto>
    ): List<ProductDto> {
        return if (cartItems.isEmpty()) {
            allProducts.shuffled().take(10)
        } else {
            val cartCategories = cartItems.map { it.category }.toSet()
            allProducts.filter { 
                it.category in cartCategories && it.id !in cartItems.map { item -> item.id } 
            }.shuffled().take(10)
        }
    }
}
