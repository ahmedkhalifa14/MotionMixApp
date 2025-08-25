package com.ahmedkhalifa.motionmix.ui.screens.home

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.ui.graphs.BottomBarScreen
import com.ahmedkhalifa.motionmix.ui.graphs.Graph
import com.ahmedkhalifa.motionmix.ui.graphs.detailsNavGraph
import com.ahmedkhalifa.motionmix.ui.main_activity.ScreenContent
import com.ahmedkhalifa.motionmix.ui.screens.post_reel.PostReelScreen
import com.ahmedkhalifa.motionmix.ui.screens.profile.UserProfileScreen
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

@OptIn(UnstableApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavHostController) {
    val homeNavController = rememberNavController()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route != Graph.HomeRoutes.POST_REEL

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    navController = homeNavController
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = homeNavController,
            startDestination = Graph.HomeRoutes.REEL,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(route = Graph.HomeRoutes.REEL) {
                ReelsScreen(navController = homeNavController)
            }
            composable(route = Graph.HomeRoutes.DISCOVER) {
                ScreenContent(
                    name = BottomBarScreen.Discover.route,
                    onClick = { }
                )
            }
            composable(route = Graph.HomeRoutes.POST_REEL) {
                PostReelScreen()
            }
            composable(route = Graph.HomeRoutes.INBOX) {
                ScreenContent(
                    name = BottomBarScreen.Inbox.route,
                    onClick = { }
                )
            }
            composable(route = Graph.HomeRoutes.PROFILE) {
                UserProfileScreen()
            }
            detailsNavGraph(navController = homeNavController)
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Discover,
        BottomBarScreen.PostReel,
        BottomBarScreen.Inbox,
        BottomBarScreen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier.height(64.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = NavigationBarDefaults.Elevation,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = when (screen) {
        BottomBarScreen.Home -> currentDestination?.route == Graph.HomeRoutes.REEL
        BottomBarScreen.Profile -> currentDestination?.route == Graph.HomeRoutes.PROFILE
        BottomBarScreen.Discover -> currentDestination?.route == Graph.HomeRoutes.DISCOVER
        BottomBarScreen.Inbox -> currentDestination?.route == Graph.HomeRoutes.INBOX
        BottomBarScreen.PostReel -> currentDestination?.route == Graph.HomeRoutes.POST_REEL
    }

    NavigationBarItem(
        selected = selected,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        icon = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = screen.title,
                    fontFamily = Montserrat,
                    fontSize = 12.sp,
                    color = if (selected) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        },
        label = null,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = Color.Unspecified,
            unselectedIconColor = Color.Unspecified
        )
    )



}