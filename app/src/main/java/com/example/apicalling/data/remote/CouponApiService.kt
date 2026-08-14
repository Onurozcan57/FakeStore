package com.example.apicalling.data.remote

import com.example.apicalling.data.model.CouponDto
import retrofit2.http.*

/**
 * Firebase Realtime Database REST API for Coupons
 */
interface CouponApiService {

    companion object {
        const val FIREBASE_URL = "https://fakestore-f6cea-default-rtdb.firebaseio.com/"
    }

    // Belirli bir kullanıcıya ait kuponları klasöründen getirir
    @GET("coupons/{userId}.json")
    suspend fun getUserCoupons(@Path("userId") userId: Int): Map<String, CouponDto>?

    // Kupon durumunu günceller (Klasör yapısına uygun yol)
    @PATCH("coupons/{userId}/{couponId}.json")
    suspend fun updateCouponStatus(
        @Path("userId") userId: Int,
        @Path("couponId") couponId: String,
        @Body updates: Map<String, Boolean>
    )
}
