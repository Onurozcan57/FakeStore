package com.example.apicalling.di

import com.example.apicalling.data.remote.ApiService
import com.example.apicalling.data.remote.FavoriteApiService
import com.example.apicalling.data.remote.CouponApiService
import com.example.apicalling.data.remote.PaymentApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RenderBackend

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirebaseBackend

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoriteApiService(okHttpClient: OkHttpClient): FavoriteApiService {
        return Retrofit.Builder()
            .baseUrl(FavoriteApiService.FIREBASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(FavoriteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCouponApiService(okHttpClient: OkHttpClient): CouponApiService {
        return Retrofit.Builder()
            .baseUrl(CouponApiService.FIREBASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CouponApiService::class.java)
    }

    @Provides
    @Singleton
    @RenderBackend
    fun providePaymentApiService(okHttpClient: OkHttpClient): PaymentApiService {
        return Retrofit.Builder()
            .baseUrl(PaymentApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(PaymentApiService::class.java)
    }

    @Provides
    @Singleton
    @FirebaseBackend
    fun provideFirebasePaymentApiService(okHttpClient: OkHttpClient): PaymentApiService {
        return Retrofit.Builder()
            .baseUrl(PaymentApiService.FIREBASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(PaymentApiService::class.java)
    }
}
