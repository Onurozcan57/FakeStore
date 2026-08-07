package com.example.apicalling.data.repository

import com.example.apicalling.data.model.LoginRequest
import com.example.apicalling.data.model.UserDto
import com.example.apicalling.data.remote.ApiService
import com.example.apicalling.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun login(request: LoginRequest): UserDto {
        return apiService.login(request)
    }

    override suspend fun getUsers(): List<UserDto> {
        return apiService.getUsers().users
    }
}
