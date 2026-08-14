package com.example.apicalling.domain.repository

import com.example.apicalling.data.model.PaymentRequest
import com.example.apicalling.data.model.PaymentResponse
import com.example.apicalling.data.model.ThreeDSRequest
import com.example.apicalling.domain.model.Order

interface PaymentRepository {
    suspend fun createPayment(request: PaymentRequest): PaymentResponse
    suspend fun verifyThreeDS(request: ThreeDSRequest): PaymentResponse
    suspend fun saveOrder(order: Order)
}
