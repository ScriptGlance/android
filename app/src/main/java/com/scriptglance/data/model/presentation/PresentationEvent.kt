package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PresentationEvent(
    @SerializedName("event_type")
    val eventType: String,
)