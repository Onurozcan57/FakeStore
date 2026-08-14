package com.example.apicalling.ui.coupon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.repository.SessionRepository
import com.example.apicalling.domain.usecase.GetUserCouponsUseCase
import com.example.apicalling.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class CouponUiState(
    val isLoading: Boolean = false,
    val coupons: List<Coupon> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val getUserCouponsUseCase: GetUserCouponsUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CouponUiState())
    val state: StateFlow<CouponUiState> = _state.asStateFlow()

    init {
        loadCoupons()
    }

    fun loadCoupons() {
        val userId = sessionRepository.user.value?.id ?: return
        getUserCouponsUseCase(userId).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.value = CouponUiState(isLoading = true)
                is Resource.Success -> _state.value = CouponUiState(coupons = result.data ?: emptyList())
                is Resource.Error -> _state.value = CouponUiState(error = result.message)
            }
        }.launchIn(viewModelScope)
    }
}
