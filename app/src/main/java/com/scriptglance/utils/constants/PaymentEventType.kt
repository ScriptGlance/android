package com.scriptglance.utils.constants

import com.google.gson.annotations.SerializedName

enum class PaymentEventType(val value: String) {
    @SerializedName("card_linked")
    CARD_LINKED("card_linked"),
    @SerializedName("transaction_updated")
    TRANSACTION_UPDATED("transaction_updated")
}