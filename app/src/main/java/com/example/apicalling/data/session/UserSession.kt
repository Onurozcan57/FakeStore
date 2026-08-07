package com.example.apicalling.data.session

import com.example.apicalling.data.model.UserDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama genelinde oturum açmış kullanıcının bilgilerini tutar.
 * Singleton (@Singleton) olduğu için uygulama açık olduğu sürece veriyi korur.
 */
@Singleton
class UserSession @Inject constructor() {
    // Oturum açan kullanıcı bilgisi (null ise giriş yapılmamış demektir)
    var user: UserDto? = null
        private set

    /**
     * Kullanıcı giriş yaptığında bilgilerini kaydeder.
     */
    fun saveSession(userDto: UserDto) {
        user = userDto
    }

    /**
     * Kullanıcı çıkış yaptığında bilgileri temizler.
     */
    fun clearSession() {
        user = null
    }

    /**
     * Oturumun aktif olup olmadığını döner.
     */
    fun isUserLoggedIn(): Boolean = user != null
}
