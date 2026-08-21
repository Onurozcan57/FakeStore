package com.example.apicalling.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.apicalling.domain.repository.CartRepository
import com.example.apicalling.ui.cart.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Terminoloji: User-Specific Storage Implementation
 * Sepet verilerini SharedPreferences kullanarak her kullanıcıya özel anahtarlarla saklar.
 */
@Singleton
class CartRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : CartRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun saveCart(userId: Int, items: List<CartItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(getCartKey(userId), json).apply()
    }

    override fun getCart(userId: Int): List<CartItem> {
        val json = prefs.getString(getCartKey(userId), null) ?: return emptyList()
        val type = object : TypeToken<List<CartItem>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun clearCart(userId: Int) {
        prefs.edit().remove(getCartKey(userId)).apply()
    }

    /**
     * Terminoloji: Dynamic Key Generation
     * Kullanıcıya özel anahtar üreterek verilerin birbirine karışmasını engeller.
     */
    private fun getCartKey(userId: Int): String = "cart_items_$userId"
}
