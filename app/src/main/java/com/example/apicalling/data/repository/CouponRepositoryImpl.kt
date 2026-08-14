package com.example.apicalling.data.repository

import com.example.apicalling.data.remote.CouponApiService
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.model.DiscountType
import com.example.apicalling.domain.repository.CouponRepository
import com.example.apicalling.domain.repository.SessionRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CouponRepositoryImpl @Inject constructor(
    private val couponApiService: CouponApiService,
    private val sessionRepository: SessionRepository
) : CouponRepository {

    override suspend fun getUserCoupons(userId: Int): List<Coupon> {
        // Direkt kullanıcının klasörünü çekiyoruz
        val userCouponsMap = couponApiService.getUserCoupons(userId) ?: return emptyList()
        return userCouponsMap.map { (id, dto) -> 
            dto.toDomain(id, userId) 
        }
    }

    override suspend fun getCouponByCode(userId: Int, code: String): Coupon? {
        val userCoupons = getUserCoupons(userId)
        return userCoupons.find { it.code.equals(code, ignoreCase = true) }
    }

    override suspend fun markCouponAsUsed(couponId: String) {
        val userId = sessionRepository.user.value?.id ?: return
        couponApiService.updateCouponStatus(userId, couponId, mapOf("isUsed" to true))
    }

    override fun validateCoupon(coupon: Coupon, cartTotal: Double): String? {
        if (coupon.isUsed) return "Bu kupon daha önce kullanılmış."
        
        if (cartTotal < coupon.minimumAmount) {
            return "Bu kupon için minimum sepet tutarı ${String.format("%.0f", coupon.minimumAmount)}$ olmalıdır."
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val expiryDate = sdf.parse(coupon.expiresAt)
            if (expiryDate != null && expiryDate.before(Date())) {
                return "Bu kuponun süresi dolmuş."
            }
        } catch (e: Exception) {}

        return null
    }

    override fun calculateDiscount(coupon: Coupon, cartTotal: Double): Double {
        var discount = when (coupon.discountType) {
            DiscountType.PERCENTAGE -> (cartTotal * coupon.discountValue) / 100.0
            DiscountType.FIXED -> coupon.discountValue
        }

        coupon.maxDiscount?.let { max ->
            if (discount > max) discount = max
        }

        return if (discount > cartTotal) cartTotal else discount
    }
}
