package com.scriptglance.utils.constants

import com.google.gson.annotations.SerializedName

enum class InvoiceStatus(val value: String) {
    @SerializedName("processing")
    PROCESSING("processing"),
    @SerializedName("success")
    SUCCESS("success"),
    @SerializedName("failure")
    FAILURE("failure")
}