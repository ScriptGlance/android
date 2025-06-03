package com.scriptglance.ui.screen.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.R
import com.scriptglance.data.model.ApiResult
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel(), SocialAuthCallback {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(context: Context, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = authRepository.login(email, password)
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
}