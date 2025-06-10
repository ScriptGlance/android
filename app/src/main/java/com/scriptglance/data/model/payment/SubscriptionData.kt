package com.scriptglance.data.model.payment

import com.google.gson.annotations.SerializedName
import com.scriptglance.utils.constants.SubscriptionStatus

data class SubscriptionData(
    @SerializedName("status")
    val status: SubscriptionStatus,
    @SerializedName("next_payment_date")
    val nextPaymentDate: String?,
    @SerializedName("payment_card")
    val paymentCard: PaymentCard?
)