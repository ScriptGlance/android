package com.scriptglance.ui.common.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.scriptglance.ui.screen.presentation.presentationDetails.PresentationDetailsScreen
import com.scriptglance.ui.screen.presentation.userDashboard.UserDashboardScreenRoot
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

                }
            )
        }
    }
}
