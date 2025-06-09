package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class RecordedVideosCountChangePayload(
    val userId: Int,
    @SerializedName("recorded_videos_count")
    val recordedVideosCount: Int
)