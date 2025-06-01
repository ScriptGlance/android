package com.scriptglance.ui.common.nav

import LoginScreen
import com.scriptglance.ui.screen.auth.registration.RegisterScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.scriptglance.ui.screen.auth.forgotPassword.ForgotPasswordScreen
import com.scriptglance.utils.constants.routes.AuthRoutes

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthRoutes.Login.route,
        route = "auth"
    ) {
        composable(AuthRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(AuthRoutes.Register.route) },
                onForgotPassword = { navController.navigate(AuthRoutes.ForgotPassword.route) },
            )
        }
        composable(AuthRoutes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onLogin = { navController.navigate(AuthRoutes.Login.route) }
            )
        }
        composable(AuthRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.navigate(AuthRoutes.Login.route) }
            )
        }
    }
}
