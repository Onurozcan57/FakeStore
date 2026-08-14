package com.example.apicalling.domain.repository

import com.example.apicalling.domain.model.Coupon

interface CouponRepository {
    suspend fun getUserCoupons(userId: Int): List<Coupon>
    suspend fun getCouponByCode(userId: Int, code: String): Coupon?
    suspend fun markCouponAsUsed(couponId: String)
    fun validateCoupon(coupon: Coupon, cartTotal: Double): String? // null ise geçerli, değilse hata mesajı
    fun calculateDiscount(coupon: Coupon, cartTotal: Double): Double
}
