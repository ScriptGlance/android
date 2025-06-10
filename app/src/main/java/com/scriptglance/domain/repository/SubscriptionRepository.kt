package com.scriptglance.domain.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.payment.CheckoutResponse
import com.scriptglance.data.model.payment.SubscriptionData
import com.scriptglance.data.model.payment.Transaction

interface SubscriptionRepository {
    suspend fun createSubscriptionCheckout(token: String): ApiResult<CheckoutResponse?>

    suspend fun getSubscription(token: String): ApiResult<SubscriptionData?>

    suspend fun cancelSubscription(token: String): ApiResult<Unit?>

    suspend fun getTransactions(token: String, offset: Int, limit: Int): ApiResult<List<Transaction>?>

    suspend fun updateCard(token: String): ApiResult<CheckoutResponse?>
}