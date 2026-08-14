package com.example.apicalling.domain.usecase

import com.example.apicalling.data.model.PaymentResponse
import com.example.apicalling.data.model.ThreeDSRequest
import com.example.apicalling.domain.repository.PaymentRepository
import com.example.apicalling.util.Resource
import javax.inject.Inject

class VerifyThreeDSUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(paymentId: String, otp: String): Resource<PaymentResponse> {
        return try {
            val response = repository.verifyThreeDS(ThreeDSRequest(paymentId, otp))
            if (response.success && response.status == "PAID") {
                Resource.Success(response)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error("Ödeme doğrulaması sırasında bağlantı sorunu oluştu. Lütfen tekrar deneyin.")
        }
    }
}
