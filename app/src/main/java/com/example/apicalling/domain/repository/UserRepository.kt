package com.example.apicalling.domain.repository

import com.example.apicalling.data.model.LoginRequest
import com.example.apicalling.data.model.UserDto

interface UserRepository {
    suspend fun login(request: LoginRequest): UserDto
    suspend fun getUsers(): List<UserDto>
}
