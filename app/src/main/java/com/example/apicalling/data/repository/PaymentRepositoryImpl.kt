package com.example.apicalling.data.repository

import com.example.apicalling.data.model.PaymentRequest
import com.example.apicalling.data.model.PaymentResponse
import com.example.apicalling.data.model.ThreeDSRequest
import com.example.apicalling.data.remote.PaymentApiService
import com.example.apicalling.di.FirebaseBackend
import com.example.apicalling.di.RenderBackend
import com.example.apicalling.domain.model.Order
import com.example.apicalling.domain.repository.PaymentRepository
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    @RenderBackend private val paymentApiService: PaymentApiService,
    @FirebaseBackend private val firebasePaymentApiService: PaymentApiService
) : PaymentRepository {

    override suspend fun createPayment(request: PaymentRequest): PaymentResponse {
        return paymentApiService.createPayment(request)
    }

    override suspend fun verifyThreeDS(request: ThreeDSRequest): PaymentResponse {
        return paymentApiService.verifyThreeDS(request)
    }

    override suspend fun saveOrder(order: Order) {
        firebasePaymentApiService.saveOrder(order.userId, order.orderId, order)
    }
}
