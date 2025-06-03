package com.scriptglance.data.model.auth

data class ForgotPasswordRequest(
    val email: String,
    val role: String = "user"
)