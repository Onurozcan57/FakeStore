package com.example.apicalling.ui.login

import com.example.apicalling.data.model.UserDto

/**
 * Login ekranının o anki durumunu temsil eder.
 * Compose UI bu state'i dinleyerek ekranda ne gösterileceğine karar verir.
 */
data class LoginState(
    val isLoading: Boolean = false,      // Yükleme animasyonu gösterilsin mi?
    val successUser: UserDto? = null,    // Giriş başarılıysa kullanıcı bilgileri
    val error: String? = null            // Hata varsa hata mesajı
)
