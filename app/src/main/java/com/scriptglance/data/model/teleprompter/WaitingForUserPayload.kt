package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class WaitingForUserPayload(
    @SerializedName("user_id")
    val userId: Int
)