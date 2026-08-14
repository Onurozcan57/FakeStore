package com.example.apicalling.domain.usecase

import com.example.apicalling.data.model.PaymentRequest
import com.example.apicalling.data.model.PaymentResponse
import com.example.apicalling.domain.repository.PaymentRepository
import com.example.apicalling.util.Resource
import javax.inject.Inject

class CreatePaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(request: PaymentRequest): Resource<PaymentResponse> {
        return try {
            val response = repository.createPayment(request)
            if (response.success || response.status == "3DS_REQUIRED") {
                Resource.Success(response)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error("Ödeme servisine ulaşılamadı. Lütfen internet bağlantınızı kontrol edin.")
        }
    }
}
