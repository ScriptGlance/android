package com.scriptglance.utils.constants

import com.google.gson.annotations.SerializedName

enum class SubscriptionStatus(val value: String) {
    @SerializedName("active")
    ACTIVE("active"),
    @SerializedName("past_due")
    PAST_DUE("past_due"),
    @SerializedName("cancelled")
    CANCELLED("cancelled"),
    @SerializedName("created")
    CREATED("created")
}