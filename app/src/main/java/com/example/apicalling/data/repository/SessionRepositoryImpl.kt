package com.example.apicalling.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.apicalling.data.model.UserDto
import com.example.apicalling.domain.repository.SessionRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : SessionRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("api_calling_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _user = mutableStateOf<UserDto?>(null)
    override val user: State<UserDto?> = _user

    init {
        loadSessionFromDisk()
    }

    override fun saveSession(userData: UserDto) {
        _user.value = userData
        val userJson = gson.toJson(userData)
        prefs.edit().putString("user_data", userJson).apply()
    }

    private fun loadSessionFromDisk() {
        val userJson = prefs.getString("user_data", null)
        if (userJson != null) {
            _user.value = gson.fromJson(userJson, UserDto::class.java)
        }
    }

    override fun clearSession() {
        _user.value = null
        prefs.edit().clear().apply()
    }

    override fun isLoggedIn(): Boolean = _user.value != null
}
