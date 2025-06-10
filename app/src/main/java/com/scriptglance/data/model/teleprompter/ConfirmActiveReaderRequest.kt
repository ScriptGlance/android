package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class ConfirmActiveReaderRequest(
    @SerializedName("is_from_start_position")
    val isFromStartPosition: Boolean
)