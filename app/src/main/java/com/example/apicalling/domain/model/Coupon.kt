package com.example.apicalling.domain.model

enum class DiscountType {
    PERCENTAGE,
    FIXED
}

data class Coupon(
    val id: String,
    val userId: Int,
    val code: String,
    val title: String,
    val description: String,
    val discountType: DiscountType,
    val discountValue: Double,
    val minimumAmount: Double,
    val maxDiscount: Double?,
    val expiresAt: String,
    val isUsed: Boolean
)
