package com.scriptglance.ui.screen.premium.purachase

import com.scriptglance.data.model.presentation.PresentationsConfig

data class PremiumPurchaseState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPurchasing: Boolean = false,
    val purchaseCompleted: Boolean = false,
    val checkoutUrl: String? = null,
    val config: PresentationsConfig? = null
)