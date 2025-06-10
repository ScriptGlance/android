package com.scriptglance.ui.screen.auth.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState: StateFlow<ForgotPasswordState> = _uiState

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            email = newEmail,
            isError = false
        )
    }

    fun sendForgotPassword() {
        val email = _uiState.value.email.trim()
        _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(isSuccess = true, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isError = true,
                    isLoading = false
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordState()
    }
}
