package com.scriptglance.di

import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.data.repository.AuthRepositoryImpl
import com.scriptglance.data.repository.PresentationsRepositoryImpl
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.domain.repository.SubscriptionRepository
import com.scriptglance.domain.repository.TeleprompterRepository
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
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPresentationsRepository(
        impl: PresentationsRepositoryImpl
    ): PresentationsRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: com.scriptglance.data.repository.UserRepositoryImpl
    ): com.scriptglance.domain.repository.UserRepository

    @Binds
    @Singleton
    abstract fun bindTeleprompterRepository(
        impl: com.scriptglance.data.repository.TeleprompterRepositoryImpl
    ): TeleprompterRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: com.scriptglance.data.repository.SubscriptionRepositoryImpl
    ): SubscriptionRepository
}