package com.example.apicalling.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.LoginRequest
import com.example.apicalling.domain.repository.SessionRepository
import com.example.apicalling.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    // UI'ın dinleyeceği state (Encapsulation: dışarıdan değiştirilemez)
    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    /**
     * Kullanıcı girişi işlemini başlatıyorum.
     * Terminoloji: Input Sanitization
     * Kullanıcının girdiği verileri temizleyerek (trim) gönderiyoruz.
     */
    fun login(username: String, password: String) {
        val cleanUsername = username.trim()
        val cleanPassword = password.trim()

        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)
            
            try {
                // Sunucuya login isteği gönderiyorum
                val loginResponse = userRepository.login(LoginRequest(cleanUsername, cleanPassword))
                
                // Login yanıtında banka bilgileri eksik olduğu için full profili çekiyoruz
                val fullUser = userRepository.getUser(loginResponse.id)
                
                // İstek başarılı olduysa kullanıcı bilgileri dönecektir
                sessionRepository.saveSession(fullUser)
                _state.value = LoginState(successUser = fullUser)
            } catch (e: Exception) {
                // Hatalı şifre veya ağ hatası durumunda buraya düşer
                _state.value = LoginState(error = "Giriş başarısız: Bilgilerinizi kontrol edin.")
            }
        }
    }
}
