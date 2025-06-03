package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName
import com.scriptglance.data.model.profile.User

data class Participant(
    @SerializedName("participant_id")
    val participantId: Int,
    val color: String,
    val user: User
)