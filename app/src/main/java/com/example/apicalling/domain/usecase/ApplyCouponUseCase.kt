package com.example.apicalling.domain.usecase

import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.repository.CouponRepository
import com.example.apicalling.util.Resource
import javax.inject.Inject

class ApplyCouponUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    suspend operator fun invoke(userId: Int, code: String, cartTotal: Double): Resource<Pair<Coupon, Double>> {
        val coupon = repository.getCouponByCode(userId, code) 
            ?: return Resource.Error("Böyle bir kupon bulunamadı veya size ait değil.")

        val validationError = repository.validateCoupon(coupon, cartTotal)
        if (validationError != null) {
            return Resource.Error(validationError)
        }

        val discount = repository.calculateDiscount(coupon, cartTotal)
        return Resource.Success(Pair(coupon, discount))
    }
}
