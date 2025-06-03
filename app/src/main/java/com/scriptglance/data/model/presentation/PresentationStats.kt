package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PresentationStats(
    @SerializedName("presentation_count")
    val presentationCount: Int,
    @SerializedName("invited_participants")
    val invitedParticipants: Int,
    @SerializedName("recordings_made")
    val recordingsMade: Int
)

