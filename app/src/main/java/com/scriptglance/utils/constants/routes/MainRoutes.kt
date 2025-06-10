package com.scriptglance.utils.constants.routes

sealed class MainRoutes(val route: String) {
    object Dashboard : MainRoutes("dashboard")
    object PresentationDetails : MainRoutes("presentation_details/{presentationId}") {
        fun createRoute(presentationId: Int): String {
            return "presentation_details/$presentationId"
        }
    }
    object Teleprompter : MainRoutes("presentation/{presentationId}/teleprompter") {
        fun createRoute(presentationId: Int): String {
            return "presentation/$presentationId/teleprompter"
        }
    }
    object PurchasePremium : MainRoutes("purchase_premium")
    object PremiumManagement : MainRoutes("premium_management")
    object Chat : MainRoutes("chat")
}