package com.example.apicalling.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface FavoriteRepository {
    val favoriteIds: StateFlow<Set<Int>>
    fun loadFavorites()
    fun toggleFavorite(productId: Int)
    fun isFavorite(productId: Int): Boolean
    fun clearData()
}
