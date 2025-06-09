package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class ParticipantVideoCount(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("videos_left")
    val videosLeft: Int?
)

