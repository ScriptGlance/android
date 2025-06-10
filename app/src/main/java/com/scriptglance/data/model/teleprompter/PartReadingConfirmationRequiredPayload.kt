package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class PartReadingConfirmationRequiredPayload(
    @SerializedName("part_id")
    val partId: Int,
    @SerializedName("time_to_confirm_seconds")
    val timeToConfirmSeconds: Int,
    @SerializedName("can_continue_from_last_position")
    val canContinueFromLastPosition: Boolean
)