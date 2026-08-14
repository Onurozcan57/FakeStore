package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("cardNumber") val cardNumber: String,
    @SerializedName("expireMonth") val expireMonth: String,
    @SerializedName("expireYear") val expireYear: String,
    @SerializedName("cvv") val cvv: String
)
