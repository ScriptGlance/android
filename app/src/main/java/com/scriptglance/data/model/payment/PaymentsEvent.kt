package com.scriptglance.data.model.payment

import com.google.gson.annotations.SerializedName
import com.scriptglance.utils.constants.PaymentEventType

data class PaymentsEvent(
    @SerializedName("event_type")
    val eventType: PaymentEventType
)