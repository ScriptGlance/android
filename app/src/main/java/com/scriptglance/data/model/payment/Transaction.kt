package com.scriptglance.data.model.payment

import com.google.gson.annotations.SerializedName
import com.scriptglance.utils.constants.InvoiceStatus

data class Transaction(
    val id: Int,
    val date: String,
    val amount: Int,
    val currency: Int,
    val status: InvoiceStatus?
)
