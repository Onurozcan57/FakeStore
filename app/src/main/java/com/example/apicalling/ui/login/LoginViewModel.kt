package com.example.apicalling.ui.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.LoginRequest
import com.example.apicalling.data.session.UserSession
import com.example.apicalling.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userSession: UserSession
) : ViewModel() {

    // UI'ın dinleyeceği state (Encapsulation: dışarıdan değiştirilemez)
    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    /**
     * Kullanıcı girişi işlemini başlatıyorum.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)
            
            try {
                // Sunucuya login isteği gönderiyorum
                val user = userRepository.login(LoginRequest(username, password))
                
                // İstek başarılı olduysa kullanıcı bilgileri dönecektir
                userSession.saveSession(user)
                _state.value = LoginState(successUser = user)
            } catch (e: Exception) {
                // Hatalı şifre veya ağ hatası durumunda buraya düşer
                _state.value = LoginState(error = "Giriş başarısız: Bilgilerinizi kontrol edin.")
            }
        }
    }
}
