package com.example.apicalling.domain.model

import com.example.apicalling.data.model.AddressDto

data class Order(
    val userId: Int,
    val orderId: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val discount: Double,
    val shipping: Double,
    val total: Double,
    val appliedCoupon: String?,
    val address: AddressDto,
    val paymentId: String?,
    val paymentStatus: String,
    val createdAt: Long
)

data class OrderItem(
    val productId: Int,
    val title: String,
    val price: Double,
    val quantity: Int,
    val image: String
)
