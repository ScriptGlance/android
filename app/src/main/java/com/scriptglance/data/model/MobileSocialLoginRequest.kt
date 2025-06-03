package com.scriptglance.data.model

data class MobileSocialLoginRequest(
    val provider: String,
    val token: String,
    val role: String = "user"
)
