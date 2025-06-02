package com.scriptglance.ui.common.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.scriptglance.ui.screen.presentation.UserDashboardScreenRoot
import com.scriptglance.utils.constants.routes.MainRoutes

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        startDestination = MainRoutes.Dashboard.route,
        route = "main"
    ) {
        composable(MainRoutes.Dashboard.route) {
            UserDashboardScreenRoot(
                onPresentationClick = {

                },
                onCreatePresentation = {

                }
            )
        }
    }
}
