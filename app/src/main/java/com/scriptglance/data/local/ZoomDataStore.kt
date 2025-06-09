package com.scriptglance.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.zoomDataStore by preferencesDataStore(name = "teleprompter_prefs")

class ZoomDataStore(private val context: Context) {
    companion object {
        private val ZOOM_KEY = floatPreferencesKey("font_size_em")
    }

    suspend fun saveZoom(value: Float) {
        context.zoomDataStore.edit { prefs ->
            prefs[ZOOM_KEY] = value
        }
    }

    suspend fun getZoom(): Float? {
        return context.zoomDataStore.data
            .map { it[ZOOM_KEY] }
            .first()
    }
}