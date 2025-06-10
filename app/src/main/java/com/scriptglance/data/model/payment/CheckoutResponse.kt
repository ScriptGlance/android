package com.scriptglance.data.model.payment

import com.google.gson.annotations.SerializedName

data class CheckoutResponse(
    @SerializedName("checkout_url")
    val checkoutUrl: String
)