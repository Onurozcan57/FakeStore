package com.example.apicalling.ui.cart

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.model.Coupon
import com.example.apicalling.domain.repository.CartRepository
import com.example.apicalling.domain.repository.SessionRepository
import com.example.apicalling.domain.usecase.ApplyCouponUseCase
import com.example.apicalling.domain.usecase.GetSuggestedProductsUseCase
import com.example.apicalling.domain.usecase.MarkCouponAsUsedUseCase
import com.example.apicalling.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.apicalling.util.PriceUtils.USD_TO_TRY_RATE
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getSuggestedProductsUseCase: GetSuggestedProductsUseCase,
    private val applyCouponUseCase: ApplyCouponUseCase,
    private val markCouponAsUsedUseCase: MarkCouponAsUsedUseCase,
    private val sessionRepository: SessionRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _suggestedProducts = MutableStateFlow<List<ProductDto>>(emptyList())

    private val _isPriceDroppedFilterActive = MutableStateFlow(false)
    val isPriceDroppedFilterActive: StateFlow<Boolean> = _isPriceDroppedFilterActive.asStateFlow()

    val suggestedProducts: StateFlow<List<ProductDto>> = combine(
        _suggestedProducts,
        _isPriceDroppedFilterActive
    ) { products, filterActive ->
        if (filterActive) {
            products.filter { it.discountPercentage > 13.0 }
        } else {
            products
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError.asStateFlow()

    init {
        // Terminoloji: Session-Driven Data Loading
        // Kullanıcı değiştiğinde (Giriş/Çıkış), sepet verisini diskten yükler veya temizleriz.
        snapshotFlow { sessionRepository.user.value }
            .onEach { user ->
                if (user != null) {
                    val savedCart = cartRepository.getCart(user.id)
                    _cartItems.value = savedCart
                } else {
                    _cartItems.value = emptyList()
                    removeCoupon()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun persistCart() {
        val userId = sessionRepository.user.value?.id ?: return
        cartRepository.saveCart(userId, _cartItems.value)
    }

    fun updateSuggestedProducts(allProducts: List<ProductDto>) {
        _suggestedProducts.value = getSuggestedProductsUseCase(_cartItems.value.map { it.product }, allProducts)
    }

    fun togglePriceDroppedFilter() {
        _isPriceDroppedFilterActive.update { !it }
    }

    fun addToCart(product: ProductDto) {
        _cartItems.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map { 
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it 
                }
            } else {
                current + CartItem(product)
            }
        }
        persistCart()
        recalculateCoupon()
    }

    fun removeFromCart(product: ProductDto) {
        _cartItems.update { current -> 
            current.filter { it.product.id != product.id } 
        }
        persistCart()
        recalculateCoupon()
    }

    fun updateQuantity(productId: Int, delta: Int) {
        _cartItems.update { current ->
            current.mapNotNull { item ->
                if (item.product.id == productId) {
                    val newQuantity = item.quantity + delta
                    if (newQuantity <= 0) null else item.copy(quantity = newQuantity)
                } else {
                    item
                }
            }
        }
        persistCart()
        recalculateCoupon()
    }

    fun toggleSelection(productId: Int) {
        _cartItems.update { current ->
            current.map { item ->
                if (item.product.id == productId) item.copy(isSelected = !item.isSelected) else item
            }
        }
        persistCart()
        recalculateCoupon()
    }

    fun applyCoupon(code: String) {
        val userId = sessionRepository.user.value?.id ?: return
        val currentTotalTry = _cartItems.value
            .filter { it.isSelected }
            .sumOf { it.product.price * it.quantity } * USD_TO_TRY_RATE

        viewModelScope.launch {
            val result = applyCouponUseCase(userId, code, currentTotalTry)
            when (result) {
                is Resource.Success -> {
                    _appliedCoupon.value = result.data?.first
                    _discount.value = result.data?.second ?: 0.0
                    _couponError.value = null
                }
                is Resource.Error -> {
                    _couponError.value = result.message
                }
                else -> {}
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _discount.value = 0.0
        _couponError.value = null
    }

    fun clearCouponError() {
        _couponError.value = null
    }

    private fun recalculateCoupon() {
        val coupon = _appliedCoupon.value ?: return
        val currentTotalTry = _cartItems.value
            .filter { it.isSelected }
            .sumOf { it.product.price * it.quantity } * USD_TO_TRY_RATE
        
        if (currentTotalTry < coupon.minimumAmount) {
            removeCoupon()
            _couponError.value = "Sepet tutarı minimum tutarın altına düştüğü için kupon kaldırıldı."
            return
        }

        if (_cartItems.value.none { it.isSelected }) {
            removeCoupon()
        }
    }

    fun finalizePayment() {
        viewModelScope.launch {
            _appliedCoupon.value?.let { coupon ->
                markCouponAsUsedUseCase(coupon.id)
            }
            clearCart()
            removeCoupon()
        }
    }

    fun getTotalPrice(): Double {
        val subtotalTry = _cartItems.value
            .filter { it.isSelected }
            .sumOf { it.product.price * it.quantity } * USD_TO_TRY_RATE
            
        val discountTry = _discount.value 
        val shippingLimitTry = 300.0 
        val shippingFeeTry = 60.0 
        
        val shipping = if (subtotalTry >= shippingLimitTry || subtotalTry == 0.0) 0.0 else shippingFeeTry
        return subtotalTry - discountTry + shipping
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        val userId = sessionRepository.user.value?.id ?: return
        cartRepository.clearCart(userId)
    }
}
