package com.example.apicalling.di

import com.example.apicalling.data.repository.FavoriteRepositoryImpl
import com.example.apicalling.data.repository.ProductRepositoryImpl
import com.example.apicalling.data.repository.UserRepositoryImpl
import com.example.apicalling.domain.repository.FavoriteRepository
import com.example.apicalling.domain.repository.ProductRepository
import com.example.apicalling.domain.repository.UserRepository
import com.example.apicalling.data.repository.SessionRepositoryImpl
import com.example.apicalling.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository
}
