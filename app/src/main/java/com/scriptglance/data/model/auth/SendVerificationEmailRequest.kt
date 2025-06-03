package com.scriptglance.data.model.auth

data class SendVerificationEmailRequest(
    val email: String,
    val role: String = "user"
)