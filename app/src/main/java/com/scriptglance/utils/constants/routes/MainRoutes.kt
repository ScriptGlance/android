package com.scriptglance.utils.constants.routes

sealed class MainRoutes(val route: String) {
    object Dashboard : MainRoutes("dashboard")
    object PresentationDetails : MainRoutes("presentation_details/{presentationId}") {
        fun createRoute(presentationId: Int): String {
            return "presentation_details/$presentationId"
        }
    }
}