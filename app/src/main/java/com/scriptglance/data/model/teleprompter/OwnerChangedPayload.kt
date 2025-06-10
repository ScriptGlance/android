package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class OwnerChangedPayload(
    @SerializedName("current_owner_change_id")
    val currentOwnerChangeId: Int
)