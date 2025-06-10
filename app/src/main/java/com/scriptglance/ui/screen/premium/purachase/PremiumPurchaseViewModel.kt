package com.scriptglance.ui.screen.premium.purachase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumPurchaseViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val presentationsRepository: PresentationsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PremiumPurchaseState())
    val state: StateFlow<PremiumPurchaseState> = _state.asStateFlow()

    private var token: String? = null

    init {
        viewModelScope.launch {
            token = authRepository.getToken()
            if (token != null) {
                loadConfig()
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Authentication error"
                    )
                }
            }
        }
    }

    private fun loadConfig() {
        val currentToken = token ?: return

        viewModelScope.launch {
            when (val result = presentationsRepository.getConfig(currentToken)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            config = result.data,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load configuration"
                        )
                    }
                }
            }
        }
    }

    fun purchasePremium() {
        val currentToken = token ?: return

        viewModelScope.launch {
            _state.update { it.copy(isPurchasing = true, error = null) }

            when (val result = subscriptionRepository.createSubscriptionCheckout(currentToken)) {
                is ApiResult.Success -> {
                    result.data?.let { checkout ->
                        _state.update {
                            it.copy(
                                isPurchasing = false,
                                checkoutUrl = checkout.checkoutUrl
                            )
                        }
                    } ?: run {
                        _state.update {
                            it.copy(
                                isPurchasing = false,
                                error = "Failed to create checkout"
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isPurchasing = false,
                            error = "Purchase failed"
                        )
                    }
                }
            }
        }
    }

    fun onPaymentCompleted() {
        _state.update {
            it.copy(
                checkoutUrl = null,
                purchaseCompleted = true
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetState() {
        _state.update {
            PremiumPurchaseState(isLoading = false)
        }
    }
}