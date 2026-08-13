package com.example.apicalling.data.repository

import com.example.apicalling.data.remote.FavoriteApiService
import com.example.apicalling.domain.repository.FavoriteRepository
import com.example.apicalling.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val favoriteApiService: FavoriteApiService
) : FavoriteRepository {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    override val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    override fun loadFavorites() {
        val userId = sessionRepository.user.value?.id ?: return
        
        scope.launch {
            try {
                val cloudFavs = favoriteApiService.getFavorites(userId)
                _favoriteIds.value = cloudFavs ?: emptySet()
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    override fun toggleFavorite(productId: Int) {
        val userId = sessionRepository.user.value?.id ?: return
        val current = _favoriteIds.value.toMutableSet()
        
        if (current.contains(productId)) current.remove(productId) else current.add(productId)
        
        _favoriteIds.value = current
        
        scope.launch {
            try {
                favoriteApiService.updateFavorites(userId, current)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    override fun isFavorite(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }

    override fun clearData() {
        _favoriteIds.value = emptySet()
    }
}
