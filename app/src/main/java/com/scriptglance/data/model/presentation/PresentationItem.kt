package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName
import com.scriptglance.data.model.profile.User

data class PresentationItem(
    @SerializedName("presentation_id")
    val presentationId: Int,
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("modified_at")
    val modifiedAt: String,
    val owner: User,
    @SerializedName("participants_count")
    val participantCount: Int
)