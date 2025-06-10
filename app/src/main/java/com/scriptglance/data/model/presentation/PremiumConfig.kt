package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PremiumConfig(
    @SerializedName("max_free_recording_time_seconds")
    val maxFreeRecordingTimeSeconds: Int,
    @SerializedName("max_free_participants_count")
    val maxFreeParticipantsCount: Int,
    @SerializedName("max_free_video_count")
    val maxFreeVideoCount: Int,
    @SerializedName("premium_price_cents")
    val premiumPriceCents: Int
)