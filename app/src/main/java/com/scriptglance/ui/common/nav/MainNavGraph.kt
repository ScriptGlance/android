package com.scriptglance.ui.common.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.scriptglance.ui.screen.chat.UserChatScreen
import com.scriptglance.ui.screen.premium.management.PremiumManagementScreen
import com.scriptglance.ui.screen.premium.purachase.PremiumPurchaseScreen
import com.scriptglance.ui.screen.presentation.presentationDetails.PresentationDetailsScreen
import com.scriptglance.ui.screen.presentation.teleprompter.TeleprompterScreen
import com.scriptglance.ui.screen.userDashboard.UserDashboardScreenRoot
import com.scriptglance.utils.constants.routes.MainRoutes

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = MainRoutes.Dashboard.route,
        route = "main"
    ) {
        composable(MainRoutes.Dashboard.route) {
            UserDashboardScreenRoot(
                onPresentationClick = {
                    navController.navigate(
                        MainRoutes.PresentationDetails.createRoute(it)
                    )
                },
                onPurchasePremiumClick = {
                    navController.navigate(MainRoutes.PurchasePremium.route)
                },
                onManageSubscriptionClick = {
                    navController.navigate(MainRoutes.PremiumManagement.route)
                },
                onChatClick = {
                    navController.navigate(MainRoutes.Chat.route)
                },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = MainRoutes.PresentationDetails.route,
            arguments = listOf(
                navArgument("presentationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            PresentationDetailsScreen(
                goBack = {
                    navController.popBackStack()
                },
                goToTeleprompter = {
                    navController.navigate(
                        "presentation/${backStackEntry.arguments?.getInt("presentationId")}/teleprompter"
                    )
                }
            )
        }

        composable(
            route = MainRoutes.Teleprompter.route,
            arguments = listOf(
                navArgument("presentationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            TeleprompterScreen(
                goBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(MainRoutes.PurchasePremium.route) {
            PremiumPurchaseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(MainRoutes.PremiumManagement.route) {
            PremiumManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(MainRoutes.Chat.route) {
             UserChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
             )
        }
    }
}
