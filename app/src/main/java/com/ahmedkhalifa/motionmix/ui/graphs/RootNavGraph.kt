package com.ahmedkhalifa.motionmix.ui.graphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ahmedkhalifa.motionmix.ui.screens.splashscreen.SplashScreen


@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.SPLASH
    ) {
        composable(route=Graph.SPLASH){
            SplashScreen(navController = navController)
        }
        homeNavGraph(navController=navController)
        authNavGraph(navController = navController)

    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val HOME = "home_graph"
    const val DETAILS = "details_graph"
    const val SPLASH = "splash"
    object HomeRoutes{
        const val REEL ="reel"
        const val POST_REEL ="post_reel"
        const val FRIENDS ="friends"
        const val INBOX ="inbox"
        const val PROFILE ="profile"
    }

}