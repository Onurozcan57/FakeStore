package com.example.apicalling.data.remote

import com.example.apicalling.data.model.LoginRequest
import com.example.apicalling.data.model.ProductListResponse
import com.example.apicalling.data.model.UserDto
import com.example.apicalling.data.model.UserListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): UserDto

    @GET("users")
    suspend fun getUsers(): UserListResponse

    @GET("products")
    suspend fun getProducts(): ProductListResponse

    companion object {
        const val BASE_URL = "https://dummyjson.com/"
    }
}
