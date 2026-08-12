package com.example.apicalling.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.apicalling.data.remote.FavoriteApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Favori ürünlerin ID'lerini hem yerelde (UserId bazlı) hem de Bulutta (Firebase) saklayan yönetici.
 * (Terminoloji: Cloud-Synced Isolated Storage)
 */
@Singleton
class FavoriteManager @Inject constructor(
    @ApplicationContext context: Context,
    private val userSession: UserSession,
    private val favoriteApiService: FavoriteApiService
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    init {
        // Kullanıcı değiştikçe favorileri yeniden yükle (Terminoloji: Reactive Session Watcher)
        scope.launch {
            userSession.user.run { 
                // Not: userSession.user bir State nesnesi olduğu için .value ile dinliyoruz
                // Compose harici bir yer olduğu için manuel takip mekanizması kuruyoruz
            }
            // Basitlik için init'te yükleme yapalım, UserSession değişimini izlemek için bir flow daha iyi olurdu
            // Ama şimdilik LoginViewModel/MainActivity üzerinden tetikleyeceğiz
        }
    }

    /**
     * Aktif kullanıcıya göre favorileri yükler (Yerel + Bulut)
     */
    fun loadFavorites() {
        val userId = userSession.user.value?.id ?: return
        
        // 1. Önce yerelden hemen yükle (Hız için)
        val localFavs = getFavoritesFromDisk(userId)
        _favoriteIds.value = localFavs

        // 2. Buluttan getir ve güncelle (Senkronizasyon için)
        scope.launch {
            try {
                val cloudFavs = favoriteApiService.getFavorites(userId)
                if (cloudFavs != null) {
                    _favoriteIds.value = cloudFavs
                    saveFavoritesToDisk(userId, cloudFavs)
                }
            } catch (e: Exception) {
                // Sessizce geç, internet olmayabilir
            }
        }
    }

    private fun getFavoritesFromDisk(userId: Int): Set<Int> {
        val stringSet = prefs.getStringSet("fav_ids_$userId", emptySet()) ?: emptySet()
        return stringSet.map { it.toInt() }.toSet()
    }

    private fun saveFavoritesToDisk(userId: Int, ids: Set<Int>) {
        prefs.edit().putStringSet("fav_ids_$userId", ids.map { it.toString() }.toSet()).apply()
    }

    fun toggleFavorite(productId: Int) {
        val userId = userSession.user.value?.id ?: return
        val current = _favoriteIds.value.toMutableSet()
        
        if (current.contains(productId)) current.remove(productId) else current.add(productId)
        
        _favoriteIds.value = current
        
        // Yerel Kayıt
        saveFavoritesToDisk(userId, current)
        
        // Bulut Kayıt (Async)
        scope.launch {
            try {
                favoriteApiService.updateFavorites(userId, current)
            } catch (e: Exception) {
                // Hata durumunda yerel veri hala geçerli
            }
        }
    }

    fun isFavorite(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }

    fun clearData() {
        _favoriteIds.value = emptySet()
    }
}
