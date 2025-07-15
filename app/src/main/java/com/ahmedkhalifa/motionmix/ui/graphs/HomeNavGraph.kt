package com.ahmedkhalifa.motionmix.ui.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ahmedkhalifa.motionmix.ui.screens.ScreenContent

fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.HOME,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = Graph.HomeRoutes.REEL) {
            ScreenContent(
                name = BottomBarScreen.Home.route,
                onClick = {
                    navController.navigate(Graph.DETAILS)
                }
            )
        }
        composable(route = Graph.HomeRoutes.PROFILE) {
            ScreenContent(
                name = BottomBarScreen.Profile.route,
                onClick = { }
            )
        }
        composable(route = Graph.HomeRoutes.POST_REEL) {
            ScreenContent(
                name = BottomBarScreen.PostReel.route,
                onClick = { }
            )
        }
        composable(route = Graph.HomeRoutes.FRIENDS) {
            ScreenContent(
                name = BottomBarScreen.Discover.route,
                onClick = { }
            )
        }
        composable(route = Graph.HomeRoutes.INBOX) {
            ScreenContent(
                name = BottomBarScreen.Inbox.route,
                onClick = { }
            )
        }
        detailsNavGraph(navController = navController)
    }
}

fun NavGraphBuilder.detailsNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.DETAILS,
        startDestination = DetailsScreen.Information.route
    ) {
        composable(route = DetailsScreen.Information.route) {
            ScreenContent(name = DetailsScreen.Information.route) {
                navController.navigate(DetailsScreen.Overview.route)
            }
        }
        composable(route = DetailsScreen.Overview.route) {
            ScreenContent(name = DetailsScreen.Overview.route) {
                navController.popBackStack(
                    route = DetailsScreen.Information.route,
                    inclusive = false
                )
            }
        }
    }
}

sealed class DetailsScreen(val route: String) {
    object Information : DetailsScreen(route = "INFORMATION")
    object Overview : DetailsScreen(route = "OVERVIEW")
}
