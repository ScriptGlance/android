package com.scriptglance.ui.screen.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.scriptglance.R
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.profile.UserProfileUpdateData
import com.scriptglance.domain.callback.SocialAuthCallback
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.UserRepository
import com.scriptglance.utils.constants.ErrorCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel(), SocialAuthCallback {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(context: Context, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = authRepository.login(email, password)
            if (result is ApiResult.Success) {
                updateFcmToken()
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

    private suspend fun updateFcmToken() {
        runCatching {
            getFcmToken()?.let { fcmToken ->
                val authToken = authRepository.getToken() ?: return@let
                userRepository.updateProfile(
                    authToken,
                    UserProfileUpdateData(fcmToken = fcmToken)
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getFcmToken(): String? = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    continuation.resume(null)
                } else {
                    continuation.resume(task.result)
                }
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
                updateFcmToken()
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