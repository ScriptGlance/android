package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class SetRecordingModeRequest(
    @SerializedName("is_active")
    val isActive: Boolean
)