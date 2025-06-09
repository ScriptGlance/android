package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class PresentationPartFull(
    @SerializedName("part_id")
    val partId: Int,
    @SerializedName("part_name")
    val partName: String,
    @SerializedName("part_text")
    val partText: String,
    @SerializedName("part_order")
    val partOrder: Int,
    @SerializedName("assignee_participant_id")
    val assigneeParticipantId: Int,
    @SerializedName("part_text_version")
    val partTextVersion: Int? = null,
    @SerializedName("part_name_version")
    val partNameVersion: Int? = null
)