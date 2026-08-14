package com.example.apicalling.domain.usecase

import com.example.apicalling.domain.repository.CouponRepository
import javax.inject.Inject

class MarkCouponAsUsedUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    suspend operator fun invoke(couponId: String) {
        repository.markCouponAsUsed(couponId)
    }
}
