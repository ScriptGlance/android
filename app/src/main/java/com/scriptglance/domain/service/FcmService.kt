package com.scriptglance.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.scriptglance.R
import com.scriptglance.data.model.profile.UserProfileUpdateData
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.UserRepository
import com.scriptglance.utils.constants.NOTIFICATION_CHANNEL_ID
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationTitle = remoteMessage.notification?.title ?: getString(R.string.app_name)
        val notificationBody = remoteMessage.notification?.body ?: ""

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo_microphone)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setAutoCancel(true)
            .setColor(getColor(R.color.green_5e))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
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