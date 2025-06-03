package com.scriptglance.utils.constants.routes

sealed class AuthRoutes(val route: String) {
    object Login : AuthRoutes("login")
    object Register : AuthRoutes("register")
    object ForgotPassword : AuthRoutes("forgot_password")
}