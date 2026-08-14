package com.example.apicalling.domain.usecase

import com.example.apicalling.domain.model.Order
import com.example.apicalling.domain.repository.PaymentRepository
import javax.inject.Inject

class SaveOrderUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(order: Order) {
        repository.saveOrder(order)
    }
}
