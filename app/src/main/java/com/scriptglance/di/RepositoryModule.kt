package com.scriptglance.di

import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.data.repository.AuthRepositoryImpl
import com.scriptglance.data.repository.ChatRepositoryImpl
import com.scriptglance.data.repository.PresentationsRepositoryImpl
import com.scriptglance.data.repository.SubscriptionRepositoryImpl
import com.scriptglance.data.repository.TeleprompterRepositoryImpl
import com.scriptglance.data.repository.UserRepositoryImpl
import com.scriptglance.domain.repository.ChatRepository
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.domain.repository.SubscriptionRepository
import com.scriptglance.domain.repository.TeleprompterRepository
import com.scriptglance.domain.repository.UserRepository
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
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTeleprompterRepository(
        impl: TeleprompterRepositoryImpl
    ): TeleprompterRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository
}