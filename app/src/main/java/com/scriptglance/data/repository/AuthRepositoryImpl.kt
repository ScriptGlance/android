package com.scriptglance.data.repository

import com.scriptglance.data.local.AuthDataStore
import com.scriptglance.data.model.ApiResult
import com.scriptglance.data.model.MobileSocialLoginRequest
import com.scriptglance.data.model.auth.*
import com.scriptglance.data.remote.ApiService
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.utils.apiFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authDataStore: AuthDataStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<TokenResponse?> {
        val body = LoginRequest(email, password)
        return apiFlow(
            apiCall = { apiService.login(body) },
            onSuccess = { it?.token?.let { token -> authDataStore.saveToken(token) } }
        )
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): ApiResult<TokenResponse?> {
        val body = RegisterRequest(firstName, lastName, email, password)
        return apiFlow(
            apiCall = { apiService.register(body) },
            onSuccess = { it?.token?.let { token -> authDataStore.saveToken(token) } }
        )
    }

    override suspend fun sendVerificationEmail(email: String): ApiResult<Unit?> {
        val body = SendVerificationEmailRequest(email)
        return apiFlow(apiCall = { apiService.sendVerificationEmail(body) })
    }

    override suspend fun verifyEmailCode(email: String, code: String): ApiResult<Unit?> {
        val body = VerifyEmailRequest(email, code)
        return apiFlow(apiCall = { apiService.verifyEmail(body) })
    }

    override suspend fun forgotPassword(email: String): ApiResult<Unit?> {
        val body = ForgotPasswordRequest(email)
        return apiFlow(apiCall = { apiService.forgotPassword(body) })
    }

    override suspend fun resetPassword(token: String, newPassword: String): ApiResult<Unit?> {
        val body = ResetPasswordRequest(token, newPassword)
        return apiFlow(apiCall = { apiService.resetPassword(body) })
    }

    override suspend fun mobileSocialLogin(
        provider: String,
        token: String
    ): ApiResult<TokenResponse?> {
        val request = MobileSocialLoginRequest(provider, token)
        return apiFlow(
            apiCall = { apiService.mobileSocialLogin(request) },
            onSuccess = { it?.token?.let { token -> authDataStore.saveToken(token) } }
        )
    }

    override suspend fun saveToken(token: String) {
        authDataStore.saveToken(token)
    }

    override suspend fun getToken(): String? {
        return authDataStore.getToken()
    }

    override suspend fun removeToken() {
        authDataStore.removeToken()
    }

    override suspend fun isAuthenticated(): Boolean {
        return getToken() != null
    }
}
