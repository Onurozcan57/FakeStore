package com.example.apicalling.data.remote

import com.example.apicalling.data.model.PaymentRequest
import com.example.apicalling.data.model.PaymentResponse
import com.example.apicalling.data.model.ThreeDSRequest
import com.example.apicalling.domain.model.Order
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PaymentApiService {

    companion object {
        const val BASE_URL = "https://fakestorepaymentbackend.onrender.com/"
        const val FIREBASE_URL = "https://fakestore-f6cea-default-rtdb.firebaseio.com/"
    }

    @POST("api/payment/create")
    suspend fun createPayment(
        @Body request: PaymentRequest
    ): PaymentResponse

    @POST("api/payment/3ds")
    suspend fun verifyThreeDS(
        @Body request: ThreeDSRequest
    ): PaymentResponse

    // Siparişi Firebase'e kaydetmek için
    @PUT("orders/{userId}/{orderId}.json")
    suspend fun saveOrder(
        @Path("userId") userId: Int,
        @Path("orderId") orderId: String,
        @Body order: Order
    ): Order
}
