package com.scriptglance.data.model.auth

data class MobileSocialLoginRequest(
    val provider: String,
    val token: String,
    val role: String = "user"
)