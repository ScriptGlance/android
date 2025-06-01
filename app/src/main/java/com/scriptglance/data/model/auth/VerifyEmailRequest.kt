package com.scriptglance.data.model.auth

data class VerifyEmailRequest(
    val email: String,
    val code: String
)