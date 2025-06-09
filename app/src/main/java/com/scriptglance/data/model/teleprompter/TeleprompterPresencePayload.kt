package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName
import com.scriptglance.utils.constants.PresenceEventType

data class TeleprompterPresencePayload(
    @SerializedName("user_id")
    val userId: Int,
    val type: String
)