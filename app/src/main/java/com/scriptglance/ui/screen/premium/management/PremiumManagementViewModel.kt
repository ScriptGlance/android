package com.scriptglance.ui.screen.premium.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.payment.PaymentsEvent
import com.scriptglance.domain.manager.socket.PaymentsSocketManager
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.SubscriptionRepository
import com.scriptglance.utils.constants.PaymentEventType
import com.scriptglance.utils.constants.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.emitter.Emitter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class PremiumManagementViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository,
    private val paymentsSocketManager: PaymentsSocketManager
) : ViewModel() {

    companion object {
        private const val TAG = "PremiumManagementVM"
    }

    private val _state = MutableStateFlow(PremiumManagementState())
    val state: StateFlow<PremiumManagementState> = _state.asStateFlow()

    private var token: String? = null
    private var paymentsEventListener: Emitter.Listener? = null

    init {
        viewModelScope.launch {
            token = authRepository.getToken()
            if (token != null) {
                loadSubscriptionData()
                loadTransactions()
                setupPaymentSocket()
            } else {
                _state.update { it.copy(isLoading = false, error = "Authentication error") }
            }
        }
    }

    private fun setupPaymentSocket() {

        viewModelScope.launch {
            try {
                Log.d(TAG, "Setting up payment socket...")

                paymentsSocketManager.connect()

                val connected = withTimeoutOrNull(5000L) {
                    var attempts = 0
                    while (!paymentsSocketManager.isConnected() && attempts < 10) {
                        delay(500)
                        attempts++
                        Log.d(TAG, "Waiting for socket connection, attempt $attempts")
                    }
                    paymentsSocketManager.isConnected()
                } == true

                if (connected) {
                    Log.d(TAG, "Socket connected successfully, setting up listeners...")

                    if (paymentsEventListener == null) {
                        paymentsEventListener = paymentsSocketManager.onPaymentsEvent { event ->
                            Log.d(TAG, "Received payment event: ${event.eventType}")
                            handlePaymentEvent(event)
                        }
                    }

                    paymentsSocketManager.subscribePayments()
                    Log.d(TAG, "Subscribed to payments events")

                } else {
                    Log.e(TAG, "Failed to connect to payment socket")
                    _state.update { it.copy(error = "Socket connection failed") }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error setting up payment socket: ${e.message}", e)
                _state.update { it.copy(error = "Socket connection failed: ${e.message}") }
            }
        }
    }

    private fun handlePaymentEvent(event: PaymentsEvent) {
        viewModelScope.launch {
            when (event.eventType) {
                PaymentEventType.CARD_LINKED -> {
                    loadSubscriptionData()
                    _state.update {
                        it.copy(
                            checkoutUrl = null,
                            isUpdatingCard = false
                        )
                    }
                }
                PaymentEventType.TRANSACTION_UPDATED -> {
                    Log.d(TAG, "Transaction updated event received, reloading transactions...")
                    delay(500)
                    loadTransactions()
                }
            }
        }
    }

    private fun loadSubscriptionData() {
        val currentToken = token ?: return

        viewModelScope.launch {
            Log.d(TAG, "Loading subscription data...")
            when (val result = subscriptionRepository.getSubscription(currentToken)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Subscription data loaded successfully")
                    _state.update {
                        it.copy(
                            subscriptionData = result.data,
                            isLoading = false
                        )
                    }
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to load subscription data: $result")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load subscription data"
                        )
                    }
                }
            }
        }
    }

    private fun loadTransactions() {
        val currentToken = token ?: return

        viewModelScope.launch {
            Log.d(TAG, "Loading transactions...")
            _state.update { it.copy(isLoadingTransactions = true) }

            when (val result = subscriptionRepository.getTransactions(currentToken, 0, 10)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Transactions loaded successfully: ${result.data?.size ?: 0} items")
                    _state.update {
                        it.copy(
                            transactions = result.data ?: emptyList(),
                            isLoadingTransactions = false
                        )
                    }
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to load transactions: $result")
                    _state.update {
                        it.copy(
                            isLoadingTransactions = false,
                            error = "Failed to load transactions"
                        )
                    }
                }
            }
        }
    }

    fun updateCard() {
        val currentToken = token ?: return

        viewModelScope.launch {
            Log.d(TAG, "Updating card...")
            _state.update { it.copy(isUpdatingCard = true, error = null) }

            when (val result = subscriptionRepository.updateCard(currentToken)) {
                is ApiResult.Success -> {
                    result.data?.let { checkout ->
                        Log.d(TAG, "Card update checkout created: ${checkout.checkoutUrl}")
                        _state.update {
                            it.copy(
                                checkoutUrl = checkout.checkoutUrl
                            )
                        }
                    } ?: run {
                        Log.e(TAG, "No checkout URL in response")
                        _state.update {
                            it.copy(
                                isUpdatingCard = false,
                                error = "Failed to create checkout for card update"
                            )
                        }
                    }
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to update card: $result")
                    _state.update {
                        it.copy(
                            isUpdatingCard = false,
                            error = "Failed to update card"
                        )
                    }
                }
            }
        }
    }

    fun cancelSubscription() {
        val currentToken = token ?: return

        viewModelScope.launch {
            Log.d(TAG, "Cancelling subscription...")
            _state.update { it.copy(isCancelling = true, error = null) }

            when (val result = subscriptionRepository.cancelSubscription(currentToken)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Subscription cancelled successfully")
                    loadSubscriptionData()
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            showCancelDialog = false
                        )
                    }
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to cancel subscription: $result")
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            error = "Failed to cancel subscription"
                        )
                    }
                }
            }
        }
    }

    fun onCardUpdateCompleted() {
        Log.d(TAG, "Card update WebView completed")
        _state.update { it.copy(checkoutUrl = null) }


        setupPaymentSocket()

        viewModelScope.launch {
            delay(3000)
            if (state.value.isUpdatingCard) {
                Log.d(TAG, "No socket event received, force reloading data...")
                loadSubscriptionData()
                _state.update { it.copy(isUpdatingCard = false) }
            }
        }
    }

    fun showCancelDialog() {
        _state.update { it.copy(showCancelDialog = true) }
    }

    fun hideCancelDialog() {
        _state.update { it.copy(showCancelDialog = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun refreshData() {
        Log.d(TAG, "Manual data refresh triggered")
        loadSubscriptionData()
        loadTransactions()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Cleaning up...")
        paymentsEventListener?.let {
            paymentsSocketManager.offPaymentsEvent(it)
        }
        paymentsSocketManager.disconnect()
    }
}