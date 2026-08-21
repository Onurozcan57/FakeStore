package com.example.apicalling.domain.repository

import com.example.apicalling.ui.cart.CartItem

/**
 * Terminoloji: Data Persistence Interface
 * Sepet verilerinin kalıcı olarak saklanması ve yüklenmesi işlemlerini tanımlar.
 */
interface CartRepository {
    fun saveCart(userId: Int, items: List<CartItem>)
    fun getCart(userId: Int): List<CartItem>
    fun clearCart(userId: Int)
}
