package com.scriptglance.data.remote

import com.scriptglance.data.model.ApiResponse
import com.scriptglance.data.model.MobileSocialLoginRequest
import com.scriptglance.data.model.auth.ForgotPasswordRequest
import com.scriptglance.data.model.auth.LoginRequest
import com.scriptglance.data.model.auth.RegisterRequest
import com.scriptglance.data.model.auth.ResetPasswordRequest
import com.scriptglance.data.model.auth.SendVerificationEmailRequest
import com.scriptglance.data.model.auth.TokenResponse
import com.scriptglance.data.model.auth.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<TokenResponse?>>

    @POST("/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<TokenResponse?>>

    @POST("/auth/mobile-social-login")
    suspend fun mobileSocialLogin(@Body body: MobileSocialLoginRequest): Response<ApiResponse<TokenResponse?>>

    @POST("/auth/send-verification-email")
    suspend fun sendVerificationEmail(@Body body: SendVerificationEmailRequest): Response<ApiResponse<Unit?>>

    @POST("/auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<ApiResponse<Unit?>>

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiResponse<Unit?>>

    @POST("/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ApiResponse<Unit?>>
}