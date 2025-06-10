package com.scriptglance.data.model.payment

import com.google.gson.annotations.SerializedName

data class PaymentCard(
    @SerializedName("masked_number")
    val maskedNumber: String,
    @SerializedName("payment_system")
    val paymentSystem: String
)