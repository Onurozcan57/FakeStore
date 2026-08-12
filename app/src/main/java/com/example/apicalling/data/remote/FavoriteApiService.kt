package com.example.apicalling.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Firebase Realtime Database REST API Interface
 * (Terminoloji: Cloud-Sync API)
 */
interface FavoriteApiService {

    companion object {
        const val FIREBASE_URL = "https://fakestore-f6cea-default-rtdb.firebaseio.com/"
    }

    // Kullanıcının favori ID listesini getirir
    // Firebase REST API sonuna .json bekler
    @GET("favorites/{userId}.json")
    suspend fun getFavorites(@Path("userId") userId: Int): Set<Int>?

    // Kullanıcının favori ID listesini günceller/yazar
    @PUT("favorites/{userId}.json")
    suspend fun updateFavorites(
        @Path("userId") userId: Int,
        @Body favoriteIds: Set<Int>
    ): Set<Int>
}
