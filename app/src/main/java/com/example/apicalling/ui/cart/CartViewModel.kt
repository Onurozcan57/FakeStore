package com.example.apicalling.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apicalling.data.model.ProductDto
import com.example.apicalling.domain.model.Coupon
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
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<ProductDto>>(emptyList())
    val cartItems: StateFlow<List<ProductDto>> = _cartItems.asStateFlow()

    private val _suggestedProducts = MutableStateFlow<List<ProductDto>>(emptyList())

    private val _isPriceDroppedFilterActive = MutableStateFlow(false)
    val isPriceDroppedFilterActive: StateFlow<Boolean> = _isPriceDroppedFilterActive.asStateFlow()

    val suggestedProducts: StateFlow<List<ProductDto>> = combine(
        _suggestedProducts,
        _isPriceDroppedFilterActive
    ) { products, filterActive ->
        if (filterActive) {
            products.filter { it.discountPercentage > 13.0 } // İndirimli olanlar
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

    fun updateSuggestedProducts(allProducts: List<ProductDto>) {
        _suggestedProducts.value = getSuggestedProductsUseCase(_cartItems.value, allProducts)
    }

    fun togglePriceDroppedFilter() {
        _isPriceDroppedFilterActive.update { !it }
    }

    fun addToCart(product: ProductDto) {
        _cartItems.update { current ->
            if (current.any { it.id == product.id }) current else current + product
        }
        recalculateCoupon() // Sepet değişince kuponu tekrar hesapla
    }

    fun removeFromCart(product: ProductDto) {
        _cartItems.update { current -> 
            current.filter { item -> item.id != product.id } 
        }
        recalculateCoupon()
    }

    fun applyCoupon(code: String) {
        val userId = sessionRepository.user.value?.id ?: return
        val currentTotal = _cartItems.value.sumOf { it.price }

        viewModelScope.launch {
            val result = applyCouponUseCase(userId, code, currentTotal)
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

    /**
     * Terminoloji: Error State Reset
     * Hata mesajı UI tarafından bir kez gösterildikten sonra temizlenir.
     */
    fun clearCouponError() {
        _couponError.value = null
    }

    private fun recalculateCoupon() {
        val coupon = _appliedCoupon.value ?: return
        val currentTotal = _cartItems.value.sumOf { it.price }
        
        // Eğer sepet tutarı minimum tutarın altına düşerse kuponu kaldır
        if (currentTotal < coupon.minimumAmount) {
            removeCoupon()
            _couponError.value = "Sepet tutarı minimum tutarın altına düştüğü için kupon kaldırıldı."
            return
        }

        // İndirimi yeniden hesapla (Fiyatlar değişmiş olabilir)
        // Burada repository'deki hesaplama mantığını kullanmak daha doğru
        // Basitlik için ApplyCouponUseCase'i tekrar çağırabiliriz veya mantığı buraya koyabiliriz.
        // Repository'den direkt çağırmak için UseCase'e metod ekleyebiliriz ama 
        // şimdilik sadece sepet boşsa kaldır diyelim.
        if (_cartItems.value.isEmpty()) {
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
        val subtotal = _cartItems.value.sumOf { it.price } * USD_TO_TRY_RATE
        val discountInTry = _discount.value * USD_TO_TRY_RATE
        val shippingLimit = 50.0 * USD_TO_TRY_RATE
        val shippingFee = 10.0 * USD_TO_TRY_RATE
        
        val shipping = if (subtotal >= shippingLimit || subtotal == 0.0) 0.0 else shippingFee
        return subtotal - discountInTry + shipping
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}
