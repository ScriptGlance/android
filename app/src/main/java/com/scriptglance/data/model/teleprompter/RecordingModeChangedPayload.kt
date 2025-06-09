package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class RecordingModeChangedPayload(
    val userId: Int,
    @SerializedName("is_recording_mode_active")
    val isRecordingModeActive: Boolean
)