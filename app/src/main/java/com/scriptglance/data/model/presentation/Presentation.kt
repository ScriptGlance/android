package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class Presentation(
    @SerializedName("presentation_id")
    val presentationId: Long,
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("modified_at")
    val modifiedAt: String,
    val owner: PresentationOwner,
    @SerializedName("participants_count")
    val participantCount: Int
)