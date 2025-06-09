package com.scriptglance.ui.screen.auth.registration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.R
import com.scriptglance.data.local.AuthDataStore
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.domain.callback.SocialAuthCallback
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.utils.constants.ErrorCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val authDataStore: AuthDataStore,
) : ViewModel(), SocialAuthCallback {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        context: Context,
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = authRepository.register(firstName, lastName, email, password)
            if (result is ApiResult.Success) {
                onSuccess()
            } else {
                val errorMessage = when (result) {
                    is ApiResult.Error -> {
                        ErrorCode.fromCode(result.code)
                            ?.let { code -> context.getString(code.messageResId) }
                            ?: context.getString(R.string.error)
                    }

                    else -> context.getString(R.string.error)
                }
                _uiState.update { it.copy(error = errorMessage) }
            }
            _uiState.update { it.copy(loading = false) }
        }
    }

    override fun socialLogin(
        context: Context,
        provider: String,
        token: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = authRepository.mobileSocialLogin(provider, token)
            if (result is ApiResult.Success) {
                onSuccess()
            } else {
                val errorMessage = when (result) {
                    is ApiResult.Error -> {
                        ErrorCode.fromCode(result.code)
                            ?.let { code -> context.getString(code.messageResId) }
                            ?: context.getString(R.string.error)
                    }

                    else -> context.getString(R.string.error)
                }
                _uiState.update { it.copy(error = errorMessage) }
            }
            _uiState.update { it.copy(loading = false) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun sendVerificationEmail(
        context: Context,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFinally: () -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.sendVerificationEmail(email)
            if (result is ApiResult.Success) {
                onSuccess()
            } else {
                val msg = (result as? ApiResult.Error)?.let { err ->
                    ErrorCode.fromCode(err.code)
                        ?.let { code -> context.getString(code.messageResId) }
                } ?: context.getString(R.string.error)
                onError(msg)
            }
            onFinally()
        }
    }

    fun verifyEmailCode(
        context: Context,
        email: String,
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFinally: () -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.verifyEmailCode(email, code)
            if (result is ApiResult.Success) {
                onSuccess()
            } else {
                val msg = (result as? ApiResult.Error)?.let { err ->
                    ErrorCode.fromCode(err.code)
                        ?.let { code -> context.getString(code.messageResId) }
                } ?: context.getString(R.string.error)
                onError(msg)
            }
            onFinally()
        }
    }
}
