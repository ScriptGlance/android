package com.scriptglance.domain.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.auth.TokenResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<TokenResponse?>

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): ApiResult<TokenResponse?>

    suspend fun sendVerificationEmail(email: String): ApiResult<Unit?>

    suspend fun verifyEmailCode(email: String, code: String): ApiResult<Unit?>

    suspend fun forgotPassword(email: String): ApiResult<Unit?>

    suspend fun resetPassword(token: String, newPassword: String): ApiResult<Unit?>

    suspend fun mobileSocialLogin(provider: String, token: String): ApiResult<TokenResponse?>

    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    suspend fun removeToken()

    suspend fun isAuthenticated(): Boolean
}