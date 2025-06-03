package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class AcceptInvitationsResponse(
    @SerializedName("presentation_id")
    val presentationId: Long
)