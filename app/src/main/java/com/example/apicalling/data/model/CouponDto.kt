package com.example.apicalling.data.model

import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.model.DiscountType
import com.google.gson.annotations.SerializedName

data class CouponDto(
    @SerializedName("code") val code: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("discountType") val discountType: String?,
    @SerializedName("discountValue") val discountValue: Double?,
    @SerializedName("minimumAmount") val minimumAmount: Double?,
    @SerializedName("maxDiscount") val maxDiscount: Double?,
    @SerializedName("expiresAt") val expiresAt: String?,
    @SerializedName("isUsed") val isUsed: Boolean?
) {
    fun toDomain(id: String, userId: Int): Coupon = Coupon(
        id = id,
        userId = userId,
        code = code ?: "ERROR",
        title = title ?: "Kupon",
        description = description ?: "",
        discountType = if (discountType == "PERCENTAGE") DiscountType.PERCENTAGE else DiscountType.FIXED,
        discountValue = discountValue ?: 0.0,
        minimumAmount = minimumAmount ?: 0.0,
        maxDiscount = maxDiscount,
        expiresAt = expiresAt ?: "2099-01-01",
        isUsed = isUsed ?: false
    )
}
