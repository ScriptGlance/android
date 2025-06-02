package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class Participant(
    @SerializedName("participant_id")
    val participantId: Long,
    val color: String,
    val user: PresentationOwner
)