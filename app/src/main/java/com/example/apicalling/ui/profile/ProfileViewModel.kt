package com.example.apicalling.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.apicalling.data.model.UserDto
import com.example.apicalling.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _user = mutableStateOf<UserDto?>(sessionRepository.user.value)
    val user: State<UserDto?> = _user

    /**
     * Oturumu kapatır.
     */
    fun logout() {
        sessionRepository.clearSession()
        _user.value = null
    }
}
