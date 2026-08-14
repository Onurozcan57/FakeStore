package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

data class ThreeDSRequest(
    @SerializedName("paymentId") val paymentId: String,
    @SerializedName("otp") val otp: String
)
