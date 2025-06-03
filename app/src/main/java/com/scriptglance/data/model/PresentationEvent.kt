package com.scriptglance.data.model

import com.google.gson.annotations.SerializedName
import com.scriptglance.utils.constants.PresentationEventType

data class PresentationEvent(
    @SerializedName("event_type")
    val eventType: String,
)