package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PresentationsConfig(
    @SerializedName("words_per_minute_min")
    val wordsPerMinuteMin: Int,
    @SerializedName("words_per_minute_max")
    val wordsPerMinuteMax: Int,
    @SerializedName("premium_config")
    val premiumConfig: PremiumConfig
)