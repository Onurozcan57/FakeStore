package com.example.apicalling.domain.usecase

import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.repository.CouponRepository
import com.example.apicalling.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUserCouponsUseCase @Inject constructor(
    private val repository: CouponRepository
) {
    operator fun invoke(userId: Int): Flow<Resource<List<Coupon>>> = flow {
        emit(Resource.Loading())
        try {
            val coupons = repository.getUserCoupons(userId)
            emit(Resource.Success(coupons))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Kuponlar yüklenirken bir hata oluştu."))
        }
    }
}
