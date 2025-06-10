package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class PresentationActiveJoinedUser(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("isRecordingModeActive")
    val isRecordingModeActive: Boolean = false
)