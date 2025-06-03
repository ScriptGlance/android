package com.scriptglance.data.model.auth

data class LoginRequest(
    val email: String,
    val password: String,
    val role: String = "user"
)

