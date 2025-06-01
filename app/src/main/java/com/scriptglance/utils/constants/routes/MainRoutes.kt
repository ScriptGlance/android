package com.scriptglance.utils.constants.routes

sealed class MainRoutes(val route: String) {
    object Dashboard : MainRoutes("dashboard")
}