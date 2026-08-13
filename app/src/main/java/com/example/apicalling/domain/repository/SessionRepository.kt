package com.example.apicalling.domain.repository

import androidx.compose.runtime.State
import com.example.apicalling.data.model.UserDto

interface SessionRepository {
    val user: State<UserDto?>
    fun saveSession(userData: UserDto)
    fun clearSession()
    fun isLoggedIn(): Boolean
}
