package com.scriptglance.ui.screen.premium.management

import com.scriptglance.data.model.payment.SubscriptionData
import com.scriptglance.data.model.payment.Transaction

data class PremiumManagementState(
    val isLoading: Boolean = true,
    val subscriptionData: SubscriptionData? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoadingTransactions: Boolean = false,
    val isCancelling: Boolean = false,
    val isUpdatingCard: Boolean = false,
    val error: String? = null,
    val checkoutUrl: String? = null,
    val showCancelDialog: Boolean = false
)