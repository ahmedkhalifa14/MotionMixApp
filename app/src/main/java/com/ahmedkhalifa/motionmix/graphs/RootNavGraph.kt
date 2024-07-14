package com.ahmedkhalifa.motionmix.graphs

import android.util.SparseLongArray
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ahmedkhalifa.motionmix.screens.home.HomeScreen
import com.ahmedkhalifa.motionmix.screens.splashscreen.SplashScreen


@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.AUTHENTICATION
    ) {
        composable(route=Graph.SPLASH){
            SplashScreen(navController = navController)
        }
        authNavGraph(navController = navController)
        composable(route = Graph.HOME) {
            HomeScreen()
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val HOME = "home_graph"
    const val DETAILS = "details_graph"
    const val SPLASH = "splash"
}