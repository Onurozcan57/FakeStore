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
import com.example.apicalling.ui.cart.CartItem
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

enum class PaymentMethod {
    CREDIT_CARD, CASH_ON_DELIVERY
}

data class CheckoutUiState(
    val addresses: List<AddressDto> = emptyList(),
    val selectedAddressId: String? = null,
    val addressForm: AddressDto = AddressDto(),
    val isAddingNewAddress: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val isUsingSavedCard: Boolean = false,
    val cardNumber: String = "",
    val cvv: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val otp: String = "",
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
        _state.update { it.copy(cardNumber = number, cvv = cvv, expiryMonth = month, expiryYear = year, isUsingSavedCard = false) }
    }

    /**
     * Terminoloji: Payment Method Switcher
     * Ödeme yöntemini değiştirir ve formu resetler.
     */
    fun setPaymentMethod(method: PaymentMethod) {
        _state.update { it.copy(paymentMethod = method) }
    }

    /**
     * Terminoloji: Autofill Profile Data
     * Kullanıcının profilindeki kayıtlı banka kartı bilgilerini forma doldurur.
     */
    fun useSavedCard() {
        val user = sessionRepository.user.value
        val bank = user?.bank
        if (bank != null) {
            val expireParts = bank.cardExpire.split("/")
            _state.update { 
                it.copy(
                    isUsingSavedCard = true,
                    cardNumber = bank.cardNumber.replace(" ", ""),
                    expiryMonth = expireParts.getOrNull(0) ?: "",
                    expiryYear = expireParts.getOrNull(1) ?: "",
                    cvv = "***" // CVV güvenlik gereği profil verisinde olmaz, sembolik doldurduk
                ) 
            }
        }
    }

    fun useNewCard() {
        _state.update { 
            it.copy(
                isUsingSavedCard = false,
                cardNumber = "",
                expiryMonth = "",
                expiryYear = "",
                cvv = ""
            ) 
        }
    }

    fun onOtpChange(newOtp: String) {
        _state.update { it.copy(otp = newOtp) }
    }

    fun isFormValid(cartItems: List<ProductDto>): Boolean {
        val s = _state.value
        if (s.selectedAddressId == null || cartItems.isEmpty()) return false
        
        return if (s.paymentMethod == PaymentMethod.CREDIT_CARD) {
            s.cardNumber.length >= 16 &&
            s.cvv.length >= 3 &&
            s.expiryMonth.isNotEmpty() &&
            s.expiryYear.isNotEmpty()
        } else {
            true // Kapıda ödeme için ek alan gerekmiyor
        }
    }

    fun confirmOrder(cartItems: List<CartItem>, discount: Double, appliedCoupon: Coupon?) {
        val userId = sessionRepository.user.value?.id ?: return
        val s = _state.value
        
        val subtotalInUsd = cartItems.sumOf { it.product.price * it.quantity }
        val subtotalInTry = subtotalInUsd * USD_TO_TRY_RATE
        
        val shippingLimitInTry = 300.0 // Kargo bedava sınırı 300 TL
        val shippingFeeInTry = 60.0
        
        val isFreeShipping = subtotalInTry >= shippingLimitInTry
        val actualShippingTry = if (isFreeShipping || subtotalInTry == 0.0) 0.0 else shippingFeeInTry
        
        val discountInTry = discount 
        val finalAmountInTry = subtotalInTry - discountInTry + actualShippingTry
        val finalAmountInUsd = finalAmountInTry / USD_TO_TRY_RATE
        
        val orderId = "ORDER-${System.currentTimeMillis()}"

        // Kapıda ödeme ise doğrudan başarıya git
        if (s.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            processOrderSuccess(userId, orderId, cartItems, subtotalInUsd, discount, actualShippingTry, finalAmountInUsd, appliedCoupon, "PENDING_CASH")
            return
        }

        // Kredi kartı süreci
        viewModelScope.launch {
            _state.update { it.copy(paymentStatus = PaymentStatus.LOADING, paymentErrorMessage = null) }

            val paymentRequest = PaymentRequest(
                userId = userId,
                orderId = orderId,
                amount = finalAmountInTry, 
                cardNumber = s.cardNumber,
                expireMonth = s.expiryMonth,
                expireYear = s.expiryYear,
                cvv = s.cvv
            )

            val result = createPaymentUseCase(paymentRequest)
            when (result) {
                is Resource.Success -> {
                    val response = result.data!!
                    processOrderSuccess(userId, orderId, cartItems, subtotalInUsd, discount, actualShippingTry, finalAmountInUsd, appliedCoupon, response.status, response.paymentId)
                }
                is Resource.Error -> {
                    _state.update { it.copy(paymentStatus = PaymentStatus.ERROR, paymentErrorMessage = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun processOrderSuccess(
        userId: Int,
        orderId: String,
        cartItems: List<CartItem>,
        subtotalInUsd: Double,
        discountInTry: Double,
        actualShippingTry: Double,
        finalAmountInUsd: Double,
        appliedCoupon: Coupon?,
        paymentStatus: String,
        paymentId: String? = null
    ) {
        val selectedAddress = _state.value.addresses.find { it.id == _state.value.selectedAddressId } ?: AddressDto()
        val order = Order(
            userId = userId,
            orderId = orderId,
            items = cartItems.map { OrderItem(it.product.id, it.product.title, it.product.price, it.quantity, it.product.thumbnail) },
            subtotal = subtotalInUsd,
            discount = discountInTry / USD_TO_TRY_RATE,
            shipping = actualShippingTry / USD_TO_TRY_RATE,
            total = finalAmountInUsd,
            appliedCoupon = appliedCoupon?.code,
            address = selectedAddress,
            paymentId = paymentId ?: "CASH_PAYMENT",
            paymentStatus = paymentStatus,
            createdAt = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            saveOrderUseCase(order)
            if (paymentStatus == "3DS_REQUIRED") {
                _state.update { it.copy(paymentStatus = PaymentStatus.THREE_DS_REQUIRED, paymentId = paymentId) }
            } else {
                _state.update { it.copy(paymentStatus = PaymentStatus.SUCCESS) }
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
