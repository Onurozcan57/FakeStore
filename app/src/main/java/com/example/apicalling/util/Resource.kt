package com.example.apicalling.util

/**
 * API'den gelen verilerin durumunu (Yükleniyor, Başarılı, Hatalı) yönetmek için
 * kullanılan generic bir sarmalayıcı (wrapper) sınıftır.
 *
 * @param T Başarılı durumda dönecek verinin tipi
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
