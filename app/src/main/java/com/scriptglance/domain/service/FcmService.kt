package com.scriptglance.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.scriptglance.R
import com.scriptglance.data.model.profile.UserProfileUpdateData
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToServer(token)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            "notification_channel",
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendTokenToServer(fcmToken: String) {
        MainScope().launch {
            runCatching {
                val authToken = authRepository.getToken() ?: return@launch
                userRepository.updateProfile(
                    authToken,
                    UserProfileUpdateData(fcmToken = fcmToken)
                )
            }
        }
    }
}