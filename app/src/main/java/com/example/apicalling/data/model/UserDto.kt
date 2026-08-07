package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("users") val users: List<UserDto>
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("image") val image: String,
    @SerializedName("bank") val bank: BankDto? = null
)

data class BankDto(
    @SerializedName("cardNumber") val cardNumber: String,
    @SerializedName("cardExpire") val cardExpire: String,
    @SerializedName("cardType") val cardType: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("iban") val iban: String
)
