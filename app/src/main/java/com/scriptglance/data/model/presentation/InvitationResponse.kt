package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class InvitationResponse(
    @SerializedName("invitation_code")
    val invitationCode: String
)