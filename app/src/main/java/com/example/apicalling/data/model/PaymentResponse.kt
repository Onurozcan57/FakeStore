package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

data class PaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("paymentId") val paymentId: String?,
    @SerializedName("orderId") val orderId: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("message") val message: String
)
