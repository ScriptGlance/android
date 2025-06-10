package com.scriptglance.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

class AuthDataStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val CONFIRM_EMAIL_KEY = stringPreferencesKey("confirmation_email")
        private val CONFIRM_SENT_AT_KEY = longPreferencesKey("confirmation_sent_at")
    }

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return context.authDataStore.data
            .map { it[TOKEN_KEY] }
            .first()
    }

    suspend fun removeToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun saveConfirmationInfo(email: String) {
        context.authDataStore.edit { prefs ->
            prefs[CONFIRM_EMAIL_KEY] = email
            prefs[CONFIRM_SENT_AT_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun getConfirmationInfo(): Pair<String?, Long?> {
        val prefs = context.authDataStore.data.first()
        return prefs[CONFIRM_EMAIL_KEY] to prefs[CONFIRM_SENT_AT_KEY]
    }

    suspend fun clearConfirmationInfo() {
        context.authDataStore.edit { prefs ->
            prefs.remove(CONFIRM_EMAIL_KEY)
            prefs.remove(CONFIRM_SENT_AT_KEY)
        }
    }

    suspend fun getSecondsLeft(email: String, limitSeconds: Int): Int {
        val (lastEmail, sentAt) = getConfirmationInfo()
        if (lastEmail != email || sentAt == null) return 0
        val secondsPassed = ((System.currentTimeMillis() - sentAt) / 1000).toInt()
        return (limitSeconds - secondsPassed).coerceAtLeast(0)
    }
}
