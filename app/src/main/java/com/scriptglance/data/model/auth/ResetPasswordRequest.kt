package com.scriptglance.data.model.auth

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val role: String = "user"
)