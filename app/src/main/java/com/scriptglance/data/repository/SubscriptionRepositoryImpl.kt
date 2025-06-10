package com.scriptglance.data.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.payment.CheckoutResponse
import com.scriptglance.data.model.payment.SubscriptionData
import com.scriptglance.data.model.payment.Transaction
import com.scriptglance.data.remote.ApiService
import com.scriptglance.domain.repository.SubscriptionRepository
import com.scriptglance.utils.apiFlow
import com.scriptglance.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : SubscriptionRepository {

    override suspend fun createSubscriptionCheckout(token: String): ApiResult<CheckoutResponse?> =
        apiFlow { apiService.createSubscriptionCheckout(bearer(token)) }

    override suspend fun getSubscription(token: String): ApiResult<SubscriptionData?> =
        apiFlow { apiService.getSubscription(bearer(token)) }

    override suspend fun cancelSubscription(token: String): ApiResult<Unit?> =
        safeApiCall { apiService.cancelSubscription(bearer(token)) }

    override suspend fun getTransactions(token: String, offset: Int, limit: Int): ApiResult<List<Transaction>?> =
        apiFlow { apiService.getTransactions(bearer(token), offset, limit) }

    override suspend fun updateCard(token: String): ApiResult<CheckoutResponse?> =
        apiFlow { apiService.updateCard(bearer(token)) }

    private fun bearer(token: String) = "Bearer $token"
}