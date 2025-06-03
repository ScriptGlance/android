package com.scriptglance.ui.common.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(
    startDestination: String = "auth"
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}
