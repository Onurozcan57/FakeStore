package com.example.apicalling.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.apicalling.data.model.UserDto
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama genelinde oturum açmış kullanıcının bilgilerini tutar.
 * Singleton (@Singleton) olduğu için uygulama açık olduğu sürece veriyi korur.
 */
@Singleton
class UserSession @Inject constructor(
    @ApplicationContext context: Context
) {
    // Disk erişimi için SharedPreferences (Kalıcı Hafıza)
    private val prefs: SharedPreferences = context.getSharedPreferences("api_calling_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // RAM erişimi için State (Hızlı ve UI ile uyumlu)
    private val _user = mutableStateOf<UserDto?>(null)
    val user: State<UserDto?> = _user

    init {
        // Uygulama her açıldığında hafızadaki veriyi RAM'e yükle
        loadSessionFromDisk()
    }

    /**
     * Kullanıcıyı hem RAM'e hem de kalıcı hafızaya kaydeder.
     */
    fun saveSession(userData: UserDto) {
        _user.value = userData
        val userJson = gson.toJson(userData)
        prefs.edit().putString("user_data", userJson).apply()
    }

    /**
     * Uygulama açılışında hafızadan veriyi okur.
     */
    private fun loadSessionFromDisk() {
        val userJson = prefs.getString("user_data", null)
        if (userJson != null) {
            _user.value = gson.fromJson(userJson, UserDto::class.java)
        }
    }

    /**
     * Oturumu kapatır ve her iki yerden de siler.
     */
    fun clearSession() {
        _user.value = null
        prefs.edit().clear().apply()
    }

    /**
     * Kullanıcı giriş yapmış mı kontrolü.
     */
    fun isLoggedIn(): Boolean = _user.value != null
}
