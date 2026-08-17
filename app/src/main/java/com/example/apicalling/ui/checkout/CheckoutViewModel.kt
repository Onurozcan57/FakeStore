package com.example.apicalling.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.AddressDto
import com.example.apicalling.data.model.PaymentRequest
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.model.Order
import com.example.apicalling.domain.model.OrderItem
import com.example.apicalling.domain.repository.AddressRepository
import com.example.apicalling.domain.repository.SessionRepository
import com.example.apicalling.domain.usecase.CreatePaymentUseCase
import com.example.apicalling.domain.usecase.SaveOrderUseCase
import com.example.apicalling.domain.usecase.VerifyThreeDSUseCase
import com.example.apicalling.util.PriceUtils.USD_TO_TRY_RATE
import com.example.apicalling.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PaymentStatus {
    IDLE, LOADING, THREE_DS_REQUIRED, VERIFYING_OTP, SUCCESS, ERROR
}

data class CheckoutUiState(
    val addresses: List<AddressDto> = emptyList(),
    val selectedAddressId: String? = null,
    val addressForm: AddressDto = AddressDto(),
    val isAddingNewAddress: Boolean = false,
    val cardNumber: String = "",
    val cvv: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val otp: String = "", // Yeni
    val paymentStatus: PaymentStatus = PaymentStatus.IDLE,
    val paymentErrorMessage: String? = null,
    val paymentId: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val sessionRepository: SessionRepository,
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val verifyThreeDSUseCase: VerifyThreeDSUseCase, // Yeni
    private val saveOrderUseCase: SaveOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    init {
        loadAddresses()
    }

    private fun loadAddresses() {
        val savedAddresses = addressRepository.getAddresses()
        _state.update { 
            it.copy(
                addresses = savedAddresses,
                selectedAddressId = savedAddresses.firstOrNull()?.id,
                isAddingNewAddress = savedAddresses.isEmpty()
            ) 
        }
    }

    fun onAddressFormChange(newAddress: AddressDto) {
        _state.update { it.copy(addressForm = newAddress) }
    }

    fun saveAddress() {
        val newAddress = _state.value.addressForm
        if (newAddress.title.isBlank() || newAddress.city.isBlank()) return

        addressRepository.addAddress(newAddress)
        loadAddresses()
        _state.update { it.copy(isAddingNewAddress = false, addressForm = AddressDto(), successMessage = "Adres başarıyla kaydedildi!") }
    }

    fun selectAddress(id: String) {
        _state.update { it.copy(selectedAddressId = id) }
    }

    fun addNewAddressMode() {
        _state.update { it.copy(isAddingNewAddress = true, addressForm = AddressDto()) }
    }
    
    fun cancelAddNewAddress() {
        if (_state.value.addresses.isNotEmpty()) {
            _state.update { it.copy(isAddingNewAddress = false) }
        }
    }

    fun updateCardInfo(number: String, cvv: String, month: String, year: String) {
        _state.update { it.copy(cardNumber = number, cvv = cvv, expiryMonth = month, expiryYear = year) }
    }

    fun onOtpChange(newOtp: String) {
        _state.update { it.copy(otp = newOtp) }
    }

    fun isFormValid(cartItems: List<ProductDto>): Boolean {
        val s = _state.value
        return s.selectedAddressId != null &&
                s.cardNumber.length >= 16 &&
                s.cvv.length >= 3 &&
                s.expiryMonth.isNotEmpty() &&
                s.expiryYear.isNotEmpty() &&
                cartItems.isNotEmpty()
    }

    fun confirmOrder(cartItems: List<ProductDto>, discount: Double, appliedCoupon: Coupon?) {
        val userId = sessionRepository.user.value?.id ?: return
        val subtotalInUsd = cartItems.sumOf { it.price }
        val shippingInUsd = if (subtotalInUsd >= 50.0) 0.0 else 10.0
        val finalAmountInUsd = subtotalInUsd - discount + shippingInUsd
        
        // Ödeme için TRY karşılığını hesapla
        val finalAmountInTry = finalAmountInUsd * USD_TO_TRY_RATE
        val orderId = "ORDER-${System.currentTimeMillis()}"

        viewModelScope.launch {
            _state.update { it.copy(paymentStatus = PaymentStatus.LOADING, paymentErrorMessage = null) }

            val paymentRequest = PaymentRequest(
                userId = userId,
                orderId = orderId,
                amount = finalAmountInTry, // Backend'e TL tutarı gönderiyoruz
                cardNumber = _state.value.cardNumber,
                expireMonth = _state.value.expiryMonth,
                expireYear = _state.value.expiryYear,
                cvv = _state.value.cvv
            )

            val result = createPaymentUseCase(paymentRequest)
            when (result) {
                is Resource.Success -> {
                    val response = result.data!!
                    val paymentId = response.paymentId
                    val paymentStatus = response.status

                    // Siparişi Firebase'e kaydet
                    val selectedAddress = _state.value.addresses.find { it.id == _state.value.selectedAddressId } ?: AddressDto()
                    val order = Order(
                        userId = userId,
                        orderId = orderId,
                        items = cartItems.map { OrderItem(it.id, it.title, it.price, 1, it.thumbnail) },
                        subtotal = subtotalInUsd,
                        discount = discount,
                        shipping = shippingInUsd,
                        total = finalAmountInUsd,
                        appliedCoupon = appliedCoupon?.code,
                        address = selectedAddress,
                        paymentId = paymentId,
                        paymentStatus = paymentStatus,
                        createdAt = System.currentTimeMillis()
                    )
                    
                    saveOrderUseCase(order)

                    if (paymentStatus == "3DS_REQUIRED") {
                        _state.update { it.copy(paymentStatus = PaymentStatus.THREE_DS_REQUIRED, paymentId = paymentId) }
                    } else {
                        _state.update { it.copy(paymentStatus = PaymentStatus.SUCCESS) }
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(paymentStatus = PaymentStatus.ERROR, paymentErrorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun verifyOTP() {
        val paymentId = _state.value.paymentId ?: return
        val otp = _state.value.otp

        viewModelScope.launch {
            _state.update { it.copy(paymentStatus = PaymentStatus.VERIFYING_OTP, paymentErrorMessage = null) }
            
            val result = verifyThreeDSUseCase(paymentId, otp)
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(paymentStatus = PaymentStatus.SUCCESS) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(paymentStatus = PaymentStatus.THREE_DS_REQUIRED, paymentErrorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
