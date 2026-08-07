package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

/**
 * Login isteği için sunucuya gönderilecek verileri temsil eder.
 */
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("expiresInMins") val expiresInMins: Int = 30 // Opsiyonel: Token süresi
)
